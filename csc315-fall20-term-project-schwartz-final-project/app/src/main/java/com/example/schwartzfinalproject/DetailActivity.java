package com.example.schwartzfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    public static final String GAME_INDEX = "game_index";
    public static final String GAME = "game";
    private static final String TAG = "DetailActivity";
    private FirebaseAuth mAuth;

    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    List<String> list;
    private Game game;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();

        int gameIndex = intent.getIntExtra(GAME_INDEX, -1);
        this.game = (Game) getIntent().getSerializableExtra(GAME);

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

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void addToCollection(View view){
        list = new ArrayList<String>();
        mDb.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("allCollections")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        Collection newCollect = document.toObject(Collection.class);
                        Log.d(TAG, "Name: " + newCollect.getName());
                        list.add(newCollect.getName());
                    }

                    final PersonalGame newPGame = new PersonalGame(game.getTitle(), game.getImageFile(),
                            game.getConsole(),game.getDescription(),game.getReleaseDate());

                    String selection;
                    list.add("Create New Collection");
                    String[] collects = list.toArray(new String[list.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                    builder.setTitle("Pick a collection to add this game to:");
                    builder.setItems(collects, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selection = list.get(which);
                            if(selection.equals("Create New Collection")){
                                AlertDialog.Builder alert = new AlertDialog.Builder(DetailActivity.this);
                                final EditText edittext = new EditText(DetailActivity.this);
                                alert.setMessage("Please input this new collection's name:");
                                alert.setTitle("Create New Collection");
                                alert.setView(edittext);

                                alert.setPositiveButton("Create and Add", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        final String newCollectionName = edittext.getText().toString();
                                        Collection newCollectionObject = new Collection(newCollectionName);
                                        mDb.collection("users").document(mAuth.getCurrentUser().getUid())
                                                .collection("allCollections").add(newCollectionObject)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d(TAG, newCollectionName + " Created");
                                                        final String finalNewCollectionName = newCollectionName;
                                                        String finalCollectionName = Character.toLowerCase(newCollectionName.charAt(0)) + newCollectionName.substring(1);
                                                        finalCollectionName = finalCollectionName.replace(" ", "");
                                                        mDb.collection("users").document(mAuth.getCurrentUser().getUid())
                                                                .collection(finalCollectionName)
                                                                .add(newPGame).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                CharSequence addedMsg = "Added a copy to " + finalNewCollectionName + "!";
                                                                int duration = Snackbar.LENGTH_SHORT;
                                                                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator), addedMsg, duration);
                                                                snackbar.show();
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                });
                                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Entire operation is cancelled.
                                    }
                                });

                                alert.show();

                            } else {
                                final String colName = selection;
                                selection = Character.toLowerCase(selection.charAt(0)) + selection.substring(1);
                                selection = selection.replace(" ", "");
                                mDb.collection("users").document(mAuth.getCurrentUser().getUid())
                                        .collection(selection)
                                        .add(newPGame).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        CharSequence addedMsg = "Added a copy to " + colName + "!";
                                        int duration = Snackbar.LENGTH_SHORT;
                                        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator), addedMsg, duration);
                                        snackbar.show();
                                    }
                                });
                            }
                        }
                    });
                    builder.show();

                } else {
                    Log.d(TAG, "Error retrieving documents: ", task.getException());
                }
            }
        });





    }



}