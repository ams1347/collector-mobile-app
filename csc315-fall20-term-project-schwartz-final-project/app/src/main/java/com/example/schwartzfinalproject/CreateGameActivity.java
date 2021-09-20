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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class CreateGameActivity extends AppCompatActivity {

    private static final String TAG = "CreateGameActivity";
    private final int PICK_IMAGE_REQUEST = 71;
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        btnChoose = findViewById(R.id.buttonLoadPicture);
        imageView = findViewById(R.id.imgView);

        titleEditText = findViewById(R.id.title_input);
        descriptionEditText = findViewById(R.id.description_input);
        monthSpinner = findViewById(R.id.monthSelector);
        daySpinner = findViewById(R.id.daySelector);
        yearSpinner = findViewById(R.id.yearSelector);
        consoleEditText = findViewById(R.id.console_input);

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
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
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
            String newImageFile = "images/" + gameTitle.toLowerCase().replace(' ', '_') + ".jpg";
            setNewImagePath(newImageFile);
            StorageReference ref = storageReference.child(newImageFile);
            final String finalGameTitle = gameTitle;
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateGameActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
                            finishCreate(finalGameTitle);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateGameActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        return valid;
    }



    public void onClickSubmitCreate(View view){
        if (!validateForm()) {
            return;
        }
        this.imageFile = "images/noImage.png";
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

        Game newGame = new Game(gameTitle, this.imageFile, console, description, releaseDate);

        mDb.collection("games")
                .add(newGame)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast toast = Toast.makeText(CreateGameActivity.this,
                                    "Game successfully added to database", Toast.LENGTH_SHORT);
                            toast.show();
                            Intent intent = new Intent(CreateGameActivity.this, HomepageActivity.class);
                            startActivity(intent);
                        } else {
                            Toast toast = Toast.makeText(CreateGameActivity.this,
                                    "Game could not be added to database", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
    }


}