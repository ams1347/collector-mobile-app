package com.example.schwartzfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Person;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PersonalDetailActivity extends AppCompatActivity {

    public static final String GAME_INDEX = "game_index";
    public static final String GAME = "game";
    private static final String TAG = "DetailActivity";
    public static final String CURRENT_COLLECTION = "current collection";
    private FirebaseAuth mAuth;

    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private PersonalGame game;
    private String currentCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();

        int gameIndex = intent.getIntExtra(GAME_INDEX, -1);
        this.game = (PersonalGame) getIntent().getSerializableExtra(GAME);
        this.currentCollection = getIntent().getStringExtra(CURRENT_COLLECTION);

        String gameTitle = game.getTitle();
        TextView titleTextView = findViewById(R.id.title);
        titleTextView.setText(gameTitle);
        actionBar.setTitle(gameTitle);

        ImageView imageView = findViewById(R.id.game_image);
        StorageReference image = mStorageRef.child(game.getImageFile());
        GlideApp.with(this)
                .load(image)
                .into(imageView);

        String description = game.getDescription();
        TextView descriptionTextView = findViewById(R.id.describer);
        descriptionTextView.setText(description);

        String releaseDate = game.getReleaseDate();
        TextView releaseDateTextView = findViewById(R.id.title_input);
        releaseDateTextView.setText(releaseDate);

        String console = game.getConsole();
        TextView consoleTextView = findViewById(R.id.console);
        consoleTextView.setText(console);

        String condition = game.getCondition();
        TextView conditionTextView = findViewById(R.id.condition_input);
        conditionTextView.setText(condition);

        String completion = game.getCompletion();
        TextView completionTextView = findViewById(R.id.completion_input);
        completionTextView.setText(completion);

        String notes = game.getNotes();
        TextView notesTextView = findViewById(R.id.notes_input);
        notesTextView.setText(notes);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_personal_game, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_edit_listing:
                Intent intent = new Intent(this, EditPersonalGameActivity.class);
                intent.putExtra(EditPersonalGameActivity.GAME, this.game);
                intent.putExtra(PersonalDetailActivity.CURRENT_COLLECTION, currentCollection);
                startActivity(intent);
                return true;
            case R.id.action_delete_listing:
                this.deleteGame();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void deleteGame() {

        mDb.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection(Character.toLowerCase(currentCollection.charAt(0)) + currentCollection.substring(1).replace(" ", ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentReference gameToDeleteRef = null;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PersonalGame tempGame = document.toObject(PersonalGame.class);
                                Log.d(TAG, "tempGame: " + tempGame);
                                Log.d(TAG, "game: " + game);
                                Log.d(TAG, "equals: " + (tempGame.equals(game)));
                                if(tempGame.equals(game)){
                                    gameToDeleteRef = document.getReference();
                                    break;
                                }
                            }

                            if(gameToDeleteRef != null){
                                gameToDeleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast toast = Toast.makeText(PersonalDetailActivity.this,
                                                "Game removed from " + currentCollection, Toast.LENGTH_SHORT);
                                        toast.show();
                                        Intent intent = new Intent(PersonalDetailActivity.this, PersonalCollectionActivity.class);
                                        intent.putExtra(PersonalCollectionActivity.CURRENT_COLLECTION, currentCollection);
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                Log.d(TAG, "Could not find game :(");
                            }


                        } else {
                            Log.w(TAG, "Could not retrieve game to delete: ", task.getException());
                        }

                    }
                });


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PersonalCollectionActivity.class);
        intent.putExtra(PersonalCollectionActivity.CURRENT_COLLECTION, currentCollection);
        startActivity(intent);
    }

}