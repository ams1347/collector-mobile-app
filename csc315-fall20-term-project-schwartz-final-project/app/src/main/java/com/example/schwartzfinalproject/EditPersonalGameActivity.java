package com.example.schwartzfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditPersonalGameActivity extends AppCompatActivity {

    private static final String TAG = "EditPGameActivity";
    public static final String CURRENT_COLLECTION = "current collection";
    public static final String GAME = "game";
    private final int PICK_IMAGE_REQUEST = 71;
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private Bitmap newGameImage;
    private Uri filePath;
    private ImageView imageView;
    private Button btnChoose;
    private String imageFile;

    EditText titleEditText;
    EditText descriptionEditText;
    Spinner monthSpinner;
    Spinner daySpinner;
    Spinner yearSpinner;
    EditText consoleEditText;
    EditText conditionEditText;
    EditText completionEditText;
    EditText notesEditText;

    private PersonalGame game;
    private String currentCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        this.game = (PersonalGame) getIntent().getSerializableExtra(GAME);
        this.currentCollection = getIntent().getStringExtra(CURRENT_COLLECTION);

        btnChoose = findViewById(R.id.buttonLoadPicture);

        imageView = findViewById(R.id.imgView);
        StorageReference image = storageReference.child(game.getImageFile());
        GlideApp.with(this)
                .load(image)
                .into(imageView);
        this.imageFile = game.getImageFile();

        titleEditText = findViewById(R.id.title_input);
        titleEditText.setText(game.getTitle());

        descriptionEditText = findViewById(R.id.description_input);
        descriptionEditText.setText(game.getDescription());


        String date = game.getReleaseDate();
        String[] newDate = date.split(",");
        Log.d(TAG, Arrays.toString(newDate));
        String[] monthDay = newDate[0].split(" ");
        Log.d(TAG, Arrays.toString(monthDay));
        String tempMonth = monthDay[0];
        String day = monthDay[1].substring(0, monthDay[1].length()-2);
        String year = newDate[1].substring(1);

        monthSpinner = findViewById(R.id.monthSelector);
        String[] allMonths = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        int month = Arrays.asList(allMonths).indexOf(tempMonth);
        monthSpinner.setSelection(month);
        daySpinner = findViewById(R.id.daySelector);
        daySpinner.setSelection(Integer.parseInt(day) - 1);
        yearSpinner = findViewById(R.id.yearSelector);
        yearSpinner.setSelection(2020 - Integer.parseInt(year));

        consoleEditText = findViewById(R.id.console_input);
        consoleEditText.setText(game.getConsole());

        conditionEditText = findViewById(R.id.condition_input);
        conditionEditText.setText(game.getCondition());

        completionEditText = findViewById(R.id.completion_input);
        completionEditText.setText(game.getCompletion());

        notesEditText = findViewById(R.id.notes_input);
        notesEditText.setText(game.getNotes());

        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            filePath = savedInstanceState.getParcelable("uri");
            imageView.setImageURI(filePath);
        }

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (filePath !=null) {
            savedInstanceState.putParcelable("uri", filePath);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "I even got here IMAGE congrsts");
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, "IMAGE SUCCESSFULLY GOTTED");
            }
            catch (IOException e)
            {
                Log.d(TAG, "COULD NOT GET IMAGE");
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(String gameTitle) {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String newImageFile = "images/" + mAuth.getCurrentUser().getUid() + "/" + gameTitle.toLowerCase().replace(' ', '_') + ".jpg";
            setNewImagePath(newImageFile);
            StorageReference ref = storageReference.child(newImageFile);
            final String finalGameTitle = gameTitle;
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(EditPersonalGameActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
                            finishCreate(finalGameTitle);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditPersonalGameActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
        else{
            finishCreate(gameTitle);
        }
    }

    public void setNewImagePath(String filePath){
        this.imageFile = filePath;
    }

    private boolean validateForm() {
        boolean valid = true;

        String gameTitle = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String month = monthSpinner.getSelectedItem().toString();
        String day = daySpinner.getSelectedItem().toString();
        String year = yearSpinner.getSelectedItem().toString();
        String console = consoleEditText.getText().toString();
        String condition = conditionEditText.getText().toString();
        String completion = completionEditText.getText().toString();

        if (TextUtils.isEmpty(gameTitle)) {
            titleEditText.setError("Please input the game's title.");
            valid = false;
        } else {
            titleEditText.setError(null);
        }

        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("Please input a brief description of the game.");
            valid = false;
        } else {
            descriptionEditText.setError(null);
        }

        if (TextUtils.isEmpty(console)) {
            consoleEditText.setError("Please input the console the game is for.");
            valid = false;
        } else {
            consoleEditText.setError(null);
        }

        if (month.equals("February")) {
            if(day.equals("29th") || day.equals("30th") || day.equals("31st")){
                Toast toast = Toast.makeText(this,
                        month + " " + day + " is not a valid date.", Toast.LENGTH_SHORT);
                toast.show();
                valid = false;
            }
        } else if (month.equals("April") || month.equals("June") || month.equals("September") || month.equals("November")){
            if(day.equals("31st")){
                Toast toast = Toast.makeText(this,
                        month + " " + day + "is not a valid date.", Toast.LENGTH_SHORT);
                toast.show();
                valid = false;
            }
        }

        if (TextUtils.isEmpty(condition)) {
            consoleEditText.setError("Please input the condition of your copy.");
            valid = false;
        } else {
            consoleEditText.setError(null);
        }

        if (TextUtils.isEmpty(console)) {
            consoleEditText.setError("Please input how complete your copy is.");
            valid = false;
        } else {
            consoleEditText.setError(null);
        }


        return valid;
    }



    public void onClickSubmitCreate(View view){
        if (!validateForm()) {
            return;
        }
        String gameTitle = titleEditText.getText().toString();
        uploadImage(gameTitle);
    }

    public void finishCreate(String finalGameTitle){
        String gameTitle = finalGameTitle;
        String description = descriptionEditText.getText().toString();
        String month = monthSpinner.getSelectedItem().toString();
        String day = daySpinner.getSelectedItem().toString();
        String year = yearSpinner.getSelectedItem().toString();
        String releaseDate = month + " " + day + ", " + year;
        String console = consoleEditText.getText().toString();
        String condition = conditionEditText.getText().toString();
        String completion = completionEditText.getText().toString();
        String notes = notesEditText.getText().toString();

        final PersonalGame newGame = new PersonalGame(gameTitle, this.imageFile, console, description, releaseDate, condition, completion, notes);

        final Map<String, Object> updateGame = new HashMap<>();
        updateGame.put("title", gameTitle);
        updateGame.put("imageFile", this.imageFile);
        updateGame.put("console", console);
        updateGame.put("description", description);
        updateGame.put("releaseDate", releaseDate);
        updateGame.put("condition", condition);
        updateGame.put("completion", completion);
        updateGame.put("notes", notes);

        mDb.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection(Character.toLowerCase(currentCollection.charAt(0)) + currentCollection.substring(1).replace(" ", ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final PersonalGame tempGame = document.toObject(PersonalGame.class);
                                if(tempGame.equals(game)){
                                    document.getReference().set(updateGame).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast toast = Toast.makeText(EditPersonalGameActivity.this,
                                                    "Game successfully updated", Toast.LENGTH_SHORT);
                                            toast.show();

                                            Intent intent = new Intent(EditPersonalGameActivity.this, PersonalDetailActivity.class);
                                            intent.putExtra(DetailActivity.GAME, newGame);
                                            intent.putExtra(PersonalDetailActivity.CURRENT_COLLECTION, currentCollection);
                                            startActivity(intent);
                                        }
                                    });
                                    break;
                                }
                            }

                        } else {
                            Log.w(TAG, "Could not retrieve game to update: ", task.getException());
                        }

                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PersonalDetailActivity.class);
        intent.putExtra(DetailActivity.GAME, this.game);
        startActivity(intent);
    }


}