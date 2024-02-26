package com.example.xpensinator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.Manifest;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnCapture;
    Button btnClear;
    Button btnCopy;
    EditText txtDisplay;
    Uri imgUri;
    TextRecognizer textRecognizer;
    Spinner spnExpCat;
    private static final int REQUEST_CAMERA_CODE = 2404;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.btnCapture);
        btnClear = findViewById(R.id.btnClear);
        txtDisplay = findViewById(R.id.txtDisplay);
        spnExpCat = findViewById(R.id.spnExpCat);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        DBHandler dbHandler = new DBHandler(this);

        List<String> expenseCategories = dbHandler.getAllExpenseCategories();
        String[] categoriesArray = expenseCategories.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnExpCat.setAdapter(adapter);

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_CODE);
        }

        // Check and request read external storage permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainActivity.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDisplay.setText("");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    imgUri = data.getData();
                    Toast.makeText(this, "An image has been selected", Toast.LENGTH_SHORT).show();

                    recognizeText();

                    btnCapture.setText("Retake");
                }
            } else {
                Toast.makeText(this, "There were no images selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void recognizeText() {
        if (imgUri != null) {
            try {
                InputImage inpImg = InputImage.fromFilePath(MainActivity.this, imgUri);

                Task<Text> result = textRecognizer.process(inpImg)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                if (text != null) {
                                    String recgText = text.getText();
                                    txtDisplay.setText(recgText);
                                } else {
                                    Toast.makeText(MainActivity.this, "No text found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Text recognition failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Image URI is null", Toast.LENGTH_SHORT).show();
        }
    }

    }