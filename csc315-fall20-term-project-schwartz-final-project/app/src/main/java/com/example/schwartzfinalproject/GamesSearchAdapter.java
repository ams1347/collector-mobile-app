package com.example.schwartzfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GamesSearchAdapter extends FirestoreRecyclerAdapter<Game, GamesSearchAdapter.GameViewHolder> {

    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private GamesSearchAdapter.Listener listener;
    private Game[] games;

    interface Listener{
        void onClick(CardView v, int position);
    }

    GamesSearchAdapter(FirestoreRecyclerOptions<Game> options) {
        super(options);
        this.listener = null;
        this.games = new Game[0];
    }

    public void setListener(GamesSearchAdapter.Listener listener){
        this.listener = listener;
    }

    public void updateData(Game[] games){
        this.games = games;
    }

    class GameViewHolder extends RecyclerView.ViewHolder {
        final CardView view;
        final TextView gameTitle;
        final ImageView gameImage;

        GameViewHolder(CardView v) {
            super(v);
            view = v;
            gameTitle = v.findViewById(R.id.info_text);
            gameImage = v.findViewById(R.id.info_image);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final GameViewHolder holder, @NonNull final int position, @NonNull final Game game) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final CardView cardView = holder.view;
        ImageView imageView = cardView.findViewById(R.id.info_image);
        TextView textView = cardView.findViewById(R.id.info_text);
        textView.setText(game.getTitle());
        StorageReference image = mStorageRef.child(game.getImageFile());

        GlideApp.with(cardView.getContext())
                .load(image)
                .into(imageView);

        cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (listener != null){
                    listener.onClick(cardView, position);
                }
            }
        });

//        if (listener != null) {
//            holder.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    listener.onClick(holder.getAdapterPosition());
//                }
//            });
//        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_captioned_image, parent, false);

        return new GameViewHolder(v);
    }

}
