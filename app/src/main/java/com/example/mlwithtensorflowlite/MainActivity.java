package com.example.mlwithtensorflowlite;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mlwithtensorflowlite.ml.Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseNavigationActivity {

    Uri imageUri;
    ImageView imageView;
    Button picture;
    final int imageSize = 128;
    final int REQUEST_CAMERA = 1;
    final int PERMISSION_CAMERA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNavigation(R.id.navHome);

        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);

        picture.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            } else {
                openCamera();
            }
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(scaledImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(
                    new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat((val >> 16) & 0xFF);
                    byteBuffer.putFloat((val >> 8) & 0xFF);
                    byteBuffer.putFloat(val & 0xFF);
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            float[] logits = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();

            float sumExp = 0f;
            float[] confidences = new float[logits.length];
            for (int i = 0; i < logits.length; i++) {
                confidences[i] = (float) Math.exp(logits[i]);
                sumExp += confidences[i];
            }
            for (int i = 0; i < confidences.length; i++) {
                confidences[i] /= sumExp;
            }

            int maxPos = 0;
            float maxConfidence = confidences[0];
            for (int i = 1; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"cardboard", "glass", "metal", "paper", "plastic"};
            String label = maxConfidence < 0.5f ? "Uncertain" : classes[maxPos];

            StringBuilder s = new StringBuilder();
            for (int i = 0; i < classes.length; i++) {
                s.append(String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100));
            }

            model.close();

            if (label.equals("Uncertain")) {
                Toast.makeText(this, "Classification is uncertain. Try again.", Toast.LENGTH_SHORT).show();
                return; // Do not proceed
            }

            // Update classification count
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                for (String c : classes) {
                    if (label.equals(c)) {
                        db.collection("users")
                                .document(uid)
                                .update(label, FieldValue.increment(1))
                                .addOnFailureListener(e -> {
                                    Map<String, Object> data = new HashMap<>();
                                    for (String cls : classes) data.put(cls, 0);
                                    data.put(label, 1);
                                    db.collection("users").document(uid).set(data);
                                });
                        break;
                    }
                }
            }

            // Start result screen (will save to Firestore there)
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("label", label);
            intent.putExtra("confidenceInfo", s.toString());
            intent.putExtra("imageUri", imageUri.toString());
            startActivity(intent);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Model error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
