package com.example.schwartzfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Person;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersonalCollectionActivity extends AppCompatActivity {

    private static final String TAG = "PCollectionActivity";
    public static final String CURRENT_COLLECTION = "current collection";
    private FirebaseAuth mAuth;
    private RecyclerView personalGameRecycler;

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private String prevSpinnerText = "Default";

    private List<PersonalGame> personalGameList = new ArrayList<>();
    private PersonalGamesCaptionedImagesAdapter adapter;

    List<String> list;
    String[] collects = new String[0];

    private String currentCollection;

    private boolean first = true;

    private Toast loadingToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_collection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentCollection = getIntent().getStringExtra(CURRENT_COLLECTION);
        Log.d(TAG, "Current Collection at creation: " + currentCollection);

        this.personalGameRecycler = this.findViewById(R.id.personal_game_recycler);
        personalGameRecycler.setNestedScrollingEnabled(false);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        this.personalGameRecycler.setLayoutManager(layoutManager);

        this.adapter = new PersonalGamesCaptionedImagesAdapter();
        this.personalGameRecycler.setAdapter(this.adapter);
        list = new ArrayList<String>();
        mDb.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("allCollections")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                         if (task.isSuccessful()) {
                             for (QueryDocumentSnapshot document : task.getResult()) {
                                 Collection newCollect = document.toObject(Collection.class);
                                 Log.d(TAG, "Name: " + newCollect.getName());
                                 list.add(newCollect.getName());
                             }
                             collects = list.toArray(new String[list.size()]);
                             first = false;
                             Log.d(TAG, "array: " + list.toString());
                             Spinner spinner = findViewById(R.id.collectionSelector);
                             spinner.setAdapter(new ArrayAdapter<String>(PersonalCollectionActivity.this,
                                     android.R.layout.simple_spinner_item, collects));

                             Log.d(TAG, "Current Collection: " + currentCollection);
                             if(currentCollection != null && !currentCollection.isEmpty()){
                                 Log.d(TAG, "Made it to not null check: " + currentCollection);
                                 int collectionIndex = Arrays.asList(collects).indexOf(currentCollection);
                                 spinner.setSelection(collectionIndex);
                                 prevSpinnerText = currentCollection;
                             } else {
                                 spinner.setSelection(0);
                                 prevSpinnerText = collects[0];
                                 currentCollection = collects[0];
                             }

                             loadingToast = Toast.makeText(PersonalCollectionActivity.this,
                                     "Loading " + currentCollection + ", Please wait...", Toast.LENGTH_LONG);
                             loadingToast.show();

                             mDb.collection("users")
                                     .document(mAuth.getCurrentUser().getUid())
                                     .collection(Character.toLowerCase(currentCollection.charAt(0)) + currentCollection.substring(1).replace(" ", ""))
                                     .get()
                                     .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                         @Override
                                         public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                             if (task.isSuccessful()) {
                                                 personalGameList = new ArrayList<>();
                                                 for (QueryDocumentSnapshot document : task.getResult()) {
                                                     PersonalGame game = document.toObject(PersonalGame.class);
                                                     personalGameList.add(game);
                                                 }
                                                 PersonalGame[] personalGameArray = new PersonalGame[personalGameList.size()];
                                                 for(int i = 0; i < personalGameList.size(); i++){
                                                     personalGameArray[i] = personalGameList.get(i);
                                                 }
                                                 updateUI(personalGameArray);
                                                 loadingToast.cancel();


                                             } else {
                                                 Log.w(TAG, "Error getting documents: ", task.getException());
                                             }

                                         }
                                     });

                         }
                     }
        });



        this.adapter.setListener(new PersonalGamesCaptionedImagesAdapter.Listener() {
            @Override
            public void onClick(CardView v, int position) {
                Intent intent = new Intent(v.getContext(), PersonalDetailActivity.class);
                intent.putExtra(PersonalDetailActivity.GAME_INDEX, position);
                intent.putExtra(PersonalDetailActivity.CURRENT_COLLECTION, currentCollection);
                intent.putExtra(PersonalDetailActivity.GAME, personalGameList.get(position));
                startActivity(intent);
            }
        });

        runSpinnerWatch();
    }

    private void runSpinnerWatch(){
    final Spinner locationSelector = findViewById(R.id.collectionSelector);
    final Handler handler = new Handler();
    handler.post(new Runnable(){
        @Override
        public void run(){
            String spinnerText = String.valueOf(locationSelector.getSelectedItem());
            if (!(spinnerText.equals(prevSpinnerText))) {
                prevSpinnerText = spinnerText;
                onSpinnerClick(spinnerText);
            }
            handler.postDelayed(this, 100);
        }
    });
    }


    public void onSpinnerClick(String spinnerText) {
        //the first check is necessary because upon creation, the spinner will otherwise always set
        // the current collection to null, even if coming from another activity.
        if(!first) {
            currentCollection = spinnerText;
            Log.d(TAG, "Current Collection at Spinner click: " + currentCollection);
            list = new ArrayList<String>();
            loadingToast = Toast.makeText(PersonalCollectionActivity.this,
                    "Loading " + currentCollection + ", Please wait...", Toast.LENGTH_LONG);
            loadingToast.show();
            mDb.collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .collection(Character.toLowerCase(currentCollection.charAt(0)) + currentCollection.substring(1).replace(" ", ""))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                personalGameList = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    PersonalGame game = document.toObject(PersonalGame.class);
                                    personalGameList.add(game);
                                }
                                PersonalGame[] personalGameArray = new PersonalGame[personalGameList.size()];
                                for (int i = 0; i < personalGameList.size(); i++) {
                                    personalGameArray[i] = personalGameList.get(i);
                                }
                                updateUI(personalGameArray);
                                loadingToast.cancel();
                            } else {
                                Log.w(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
        first = false;
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(!first) {
//            this.currentCollection = getIntent().getStringExtra(CURRENT_COLLECTION);
//            Spinner spinner = findViewById(R.id.collectionSelector);
//            int collectionIndex = 0;
//            Log.d(TAG, "collects: " + collects.toString());
//            if (currentCollection != null && !currentCollection.isEmpty()) {
//                collectionIndex = Arrays.asList(collects).indexOf(currentCollection);
//                spinner.setSelection(collectionIndex);
//            } else {
//                currentCollection = collects[0];
//            }
//            spinner.setSelection(collectionIndex);
//            prevSpinnerText = currentCollection;
//            mDb.collection("users")
//                    .document(mAuth.getCurrentUser().getUid())
//                    .collection(currentCollection)
//                    .get()
//                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                            if (task.isSuccessful()) {
//                                personalGameList = new ArrayList<>();
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    PersonalGame game = document.toObject(PersonalGame.class);
//                                    personalGameList.add(game);
//                                }
//                                PersonalGame[] personalGameArray = new PersonalGame[personalGameList.size()];
//                                for (int i = 0; i < personalGameList.size(); i++) {
//                                    personalGameArray[i] = personalGameList.get(i);
//                                }
//                                updateUI(personalGameArray);
//
//                            } else {
//                                Log.w(TAG, "Error getting documents: ", task.getException());
//                            }
//
//                        }
//                    });
//        }
//    }

    private void updateUI(PersonalGame[] personalGameArray) {
        this.adapter.updateData(personalGameArray);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_without_pc, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomepageActivity.class);
        startActivity(intent);
    }
}