package com.example.mlwithtensorflowlite

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Date

class ResultActivity : AppCompatActivity() {

    private val TAG = "ResultActivity"
    private val GEMINI_API_KEY = "AIzaSyC6aym3SKFotZMvucNgcQBjS4H6iX8G9a0"
    private val GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$GEMINI_API_KEY"

    private lateinit var detectedImage: ImageView
    private lateinit var itemName: TextView
    private lateinit var guideTitle: TextView
    private lateinit var tvWhatToDo: TextView
    private lateinit var tvWhatNotToDo: TextView
    private lateinit var tvProTip: TextView
    private lateinit var btnBack: ImageView

    private val client = OkHttpClient()

    private var label: String? = null
    private var imageUriString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        detectedImage = findViewById(R.id.detectedImage)
        itemName = findViewById(R.id.item_name)
        guideTitle = findViewById(R.id.tvDisposalRecommendations)
        tvWhatToDo = findViewById(R.id.tvWhatToDo)
        tvWhatNotToDo = findViewById(R.id.tvWhatNotToDo)
        tvProTip = findViewById(R.id.tvProTip)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        label = intent.getStringExtra("label")
        imageUriString = intent.getStringExtra("imageUri")

        if (label == null || imageUriString == null) {
            Toast.makeText(this, "Missing classification data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        itemName.text = label
        guideTitle.text = "Smart guide to disposing of \"$label\""
        detectedImage.setImageURI(Uri.parse(imageUriString))

        sendPromptToGemini(label!!, imageUriString!!)
    }

    private fun sendPromptToGemini(label: String, imageUri: String) {
        val promptText = """
            The item is classified as "$label".

            Give:
            - A 5-word summary of the correct disposal or recycling method for $label.
            - A 5-word summary of common mistakes or incorrect disposal methods for $label.
            - A specific and helpful tip that is unique to $label (not just "take to recycling center"). Mention correct preparation, safety, or local policy if applicable.

            Format the response as pure JSON:
            {
              "do": "5-word summary",
              "dont": "5-word summary",
              "proTip": "One helpful and specific sentence about $label"
            }
            Only return the JSON. Do not include explanations or markdown formatting.
        """.trimIndent()

        val part = JSONObject().put("text", promptText)
        val contents = JSONObject().put("parts", JSONArray().put(part))
        val requestJson = JSONObject().put("contents", JSONArray().put(contents))

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), requestJson.toString())
        val request = Request.Builder().url(GEMINI_URL).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Gemini API call failed", e)
                runOnUiThread {
                    Toast.makeText(this@ResultActivity, "AI call failed.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = response.body?.string() ?: ""
                Log.d(TAG, "Gemini response: $raw")
                try {
                    val content = JSONObject(raw)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()

                    val parsed = JSONObject(content)

                    val doText = parsed.optString("do", "N/A")
                    val dontText = parsed.optString("dont", "N/A")
                    val tipText = parsed.optString("proTip", "N/A")

                    runOnUiThread {
                        tvWhatToDo.text = doText
                        tvWhatNotToDo.text = dontText
                        tvProTip.text = tipText
                    }

                    saveToFirestore(label, imageUri, doText, dontText, tipText)

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Gemini response", e)
                    runOnUiThread {
                        Toast.makeText(this@ResultActivity, "Error parsing AI response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun saveToFirestore(label: String, imageUri: String, doText: String, dontText: String, proTip: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val data = hashMapOf(
            "label" to label,
            "imageUri" to imageUri,
            "doSummary" to doText,
            "dontSummary" to dontText,
            "proTip" to proTip,
            "timestamp" to Timestamp(Date())
        )
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("history")
            .add(data)
            .addOnSuccessListener { Log.d(TAG, "Saved to Firestore") }
            .addOnFailureListener { e -> Log.e(TAG, "Failed to save", e) }
    }
}
