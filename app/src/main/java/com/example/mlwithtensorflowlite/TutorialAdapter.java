package com.example.mlwithtensorflowlite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    private final int[] images;
    private final String[] captions;

    public TutorialAdapter(int[] images, String[] captions) {
        this.images = images;
        this.captions = captions;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView tutorialImage;
        TextView tutorialText;

        public ViewHolder(View itemView) {
            super(itemView);
            tutorialImage = itemView.findViewById(R.id.tutorialImageView);
            tutorialText = itemView.findViewById(R.id.tutorialTextView);
        }
    }

    @NonNull
    @Override
    public TutorialAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutorial_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorialAdapter.ViewHolder holder, int position) {
        holder.tutorialImage.setImageResource(images[position]);
        holder.tutorialText.setText(captions[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }
}
