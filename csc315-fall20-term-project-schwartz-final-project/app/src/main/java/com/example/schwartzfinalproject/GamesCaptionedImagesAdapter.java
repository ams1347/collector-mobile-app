package com.example.schwartzfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class GamesCaptionedImagesAdapter extends RecyclerView.Adapter<GamesCaptionedImagesAdapter.ViewHolder>{

    private Game[] games;
    private Listener listener;

    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

    interface Listener{
        void onClick(CardView v, int position);
    }

    public GamesCaptionedImagesAdapter(){
        this.games = new Game[0];
    }

    public GamesCaptionedImagesAdapter(Game[] games){
        this.games = games;
    }

    @Override
    public int getItemCount() {
        return games.length;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void updateData(Game[] games){
        this.games = games;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_captioned_image, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final CardView cardView = holder.cardView;
        ImageView imageView = cardView.findViewById(R.id.info_image);

        StorageReference image = mStorageRef.child(this.games[position].getImageFile());

        GlideApp.with(cardView.getContext())
                .load(image)
                .into(imageView);

        imageView.setContentDescription(this.games[position].getTitle());

        TextView textView = cardView.findViewById(R.id.info_text);
        textView.setText(this.games[position].getTitle());

        cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (listener != null){
                    listener.onClick(cardView, position);
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;

        public ViewHolder(CardView v){
            super(v);
            cardView = v;
        }

    }

}
