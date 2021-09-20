package com.example.schwartzfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    private static final String TAG = "HomepageActivity";
    public static final String FIRST_TIME = "first time";
    private FirebaseAuth mAuth;
    private boolean first;

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private List<Game> gameList = new ArrayList<>();
//    private GamesCaptionedImagesAdapter adapter;
    private GamesSearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        Intent createdIntent = getIntent();
        this.first = createdIntent.getBooleanExtra(FIRST_TIME, false);
        Log.d(TAG, "first value" + first);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView gameRecycler = this.findViewById(R.id.personal_game_recycler);
        gameRecycler.setNestedScrollingEnabled(false);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        gameRecycler.setLayoutManager(layoutManager);

        //Adding search functionality, old adapter commented out
        Query query = mDb.collection("games")
                .orderBy("title", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Game> options = new FirestoreRecyclerOptions.Builder<Game>()
                .setQuery(query, Game.class)
                .build();

//        this.adapter = new GamesCaptionedImagesAdapter();
        this.adapter = new GamesSearchAdapter(options);
        gameRecycler.setAdapter(this.adapter);

        query = mDb.collection("games")
                .orderBy("title", Query.Direction.ASCENDING);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            gameList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Game game = document.toObject(Game.class);
                                gameList.add(game);
                            }
                            Game[] gameArray = new Game[gameList.size()];
                            for(int i = 0; i < gameList.size(); i++){
                                gameArray[i] = gameList.get(i);
                            }
                            updateUI(gameArray);

                        } else {
                            Log.w(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });

        final EditText searchBox = findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "Searchbox has changed to " + editable.toString());
                Query query;
                if(editable.toString().isEmpty()){
                    query = mDb.collection("games")
                            .orderBy("title", Query.Direction.ASCENDING);
                    query.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        gameList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Game game = document.toObject(Game.class);
                                            gameList.add(game);
                                        }
                                        Game[] gameArray = new Game[gameList.size()];
                                        for(int i = 0; i < gameList.size(); i++){
                                            gameArray[i] = gameList.get(i);
                                        }
                                        updateUI(gameArray);

                                    } else {
                                        Log.w(TAG, "Error getting documents: ", task.getException());
                                    }

                                }
                            });
                }
                else{
                    query = mDb.collection("games")
                            .whereEqualTo("title",editable.toString());
                    Log.d(TAG, "I've gotten the query");
                    query.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    Log.d(TAG, "I've gotten to the onComplete");
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "The task was successful");
                                        gameList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Game game = document.toObject(Game.class);
                                            gameList.add(game);
                                        }
                                        Game[] gameArray = new Game[gameList.size()];
                                        for(int i = 0; i < gameList.size(); i++){
                                            gameArray[i] = gameList.get(i);
                                        }
                                        Log.d(TAG, "I've gotten the array");
                                        updateUI(gameArray);

                                    } else {
                                        Log.w(TAG, "Error getting documents: ", task.getException());
                                    }

                                }
                            });
                }
                FirestoreRecyclerOptions<Game> options = new FirestoreRecyclerOptions.Builder<Game>()
                        .setQuery(query, Game.class)
                        .build();
                adapter.updateOptions(options);

            }
        });




        this.adapter.setListener(new GamesSearchAdapter.Listener() {
            @Override
            public void onClick(CardView v, int position) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.GAME_INDEX, position);
                intent.putExtra(DetailActivity.GAME, gameList.get(position));
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            first = savedInstanceState.getBoolean("first");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("first", first);
    }

    private void updateUI(Game[] gameArray) {
        this.adapter.updateData(gameArray);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        if(this.first){
            CharSequence addedMsg = "Welcome, " + this.mAuth.getCurrentUser().getEmail() + "!";
            int duration = Snackbar.LENGTH_SHORT;
            Snackbar snackbar = Snackbar.make(findViewById(R.id.homepage_temp_text), addedMsg, duration);
            snackbar.setAction("Log Out", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    signOut();
                    Toast toast = Toast.makeText(HomepageActivity.this,
                            "Logged Out", Toast.LENGTH_SHORT);
                    toast.show();

                }
            });
            snackbar.show();
            this.first = false;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_personal_collection:
                Intent intent = new Intent(this, PersonalCollectionActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                this.signOut();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }

    public void onClickCreateGame(View view){
        Intent intent = new Intent(this, CreateGameActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

}