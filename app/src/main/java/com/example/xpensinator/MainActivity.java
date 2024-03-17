package com.example.xpensinator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.android.material.navigation.NavigationView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button btnCapture;
    Button btnClear;
    Button btnEnter;
    Button btnRegister;
    EditText txtDisplay;
    Uri imgUri;
    TextRecognizer textRecognizer;
    Spinner spnExpCat;
    EditText txtDateEntered;
    EditText txtNotes;
    private static final int REQUEST_CAMERA_CODE = 2404;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 101;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.btnCapture);
        btnClear = findViewById(R.id.btnClear);
        btnEnter = findViewById(R.id.btnEnter);
        txtDisplay = findViewById(R.id.txtDisplay);
        spnExpCat = findViewById(R.id.spnExpCat);
        txtDateEntered = findViewById(R.id.txtDateEntered);
        txtNotes = findViewById(R.id.txtNotes);
        btnRegister = findViewById(R.id.btnRegister);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date currentDate = new Date();
        String formattedDateTime = dateFormat.format(currentDate);
        txtDateEntered.setText(formattedDateTime);

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

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userid = 1;
                String expDate = txtDateEntered.getText().toString();
                String expCategory = spnExpCat.getSelectedItem().toString();
                String totalExpense = txtDisplay.getText().toString();
                String notes = txtNotes.getText().toString();

                DBHandler dbHandler = new DBHandler(MainActivity.this);

                dbHandler.insertExpense(userid, expDate, expCategory, totalExpense, notes);

                Toast.makeText(getApplicationContext(), "Expense added successfully", Toast.LENGTH_LONG).show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent3);
            }
        });
       // return formattedDateTime;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_enter_expense) {
            // Handle navigation to item 1
            Intent intent1 = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent1);
        } else if (id == R.id.nav_dashboard) {
            // Handle navigation to item 2
            Intent intent2 = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent2);
        }
        // Close the navigation drawer after item selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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