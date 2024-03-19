package com.example.xpensinator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button btnBudget;
    TextView txtBudget;
    AlertDialog alertDialog;
    TextView txtTotalExp;
    ListView expensesListView;
    ArrayAdapter<String> expensesAdapter;
    List<String> expensesList;
    TextView txtBudgetMsg;
    double totalExpenses;
    TextView txtWelcome;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnBudget = findViewById(R.id.btnBudget);
        txtBudget = findViewById(R.id.txtBudget);
        txtTotalExp = findViewById(R.id.txtTotalExp);
        txtBudgetMsg = findViewById(R.id.txtBudgetMsg);
        expensesListView = findViewById(R.id.expensesListView);
        txtWelcome = findViewById(R.id.txtWelcome);

        email = getIntent().getStringExtra("email");
        final String password = getIntent().getStringExtra("password");
        final String firstName = getCurrentUserFirstName(email, password);

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DBHandler dbHandler = new DBHandler(this, email);

        txtWelcome.setText(getString(R.string.lblWelcome) + " " + firstName);

        double lastBudget = dbHandler.getLastBudget(email);

        String formattedLastBudget = String.format(Locale.getDefault(), "%.2f", lastBudget);
        txtBudget.setText(formattedLastBudget);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetBudgetDialog();
            }
        });

        totalExpenses = dbHandler.getTotalExpensesFromDatabase(email);

        txtTotalExp.setText(String.format(Locale.getDefault(), "%.2f", totalExpenses));

        List<String> expensesList = dbHandler.getAllExpensesFromDatabase(email);

        ExpensesAdapter expensesAdapter = new ExpensesAdapter(this, expensesList);
        expensesListView.setAdapter(expensesAdapter);
       // return formattedLastBudget;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_enter_expense) {
            // Handle navigation to item 1
            Intent intent1 = new Intent(DashboardActivity.this, MainActivity.class);
            intent1.putExtra("email", email);
            startActivity(intent1);
        } else if (id == R.id.nav_dashboard) {
            // Handle navigation to item 2
            Intent intent2 = new Intent(DashboardActivity.this, DashboardActivity.class);
            intent2.putExtra("email", email);
            startActivity(intent2);
        }
        else if (id == R.id.nav_logout) {
            // Handle navigation to item 2
            logout();
            return true;
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

    private String getCurrentUserFirstName(String email, String password) {
        DBHandler dbHandler = new DBHandler(this, email);
        return dbHandler.getFirstName(email, password);
    }

    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Budget");

        View view = LayoutInflater.from(this).inflate(R.layout.popup_set_budget, null);
        EditText txtSetBudget = view.findViewById(R.id.txtSetBudget);
        Button btnSaveBudget = view.findViewById(R.id.btnSaveBudget);

        final String email = getIntent().getStringExtra("email");
        final String password = getIntent().getStringExtra("password");
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newBudget = txtSetBudget.getText().toString();
                if (newBudget != null) {
                    double newBudgetValue = Double.parseDouble(newBudget);
                    String formattedBudget = String.format(Locale.getDefault(), "%.2f", newBudgetValue);
                    txtBudget.setText(formattedBudget);

                    DBHandler dbHandler = new DBHandler(DashboardActivity.this, email);
                    dbHandler.saveBudget(newBudgetValue, email);

                    if (newBudgetValue < totalExpenses) {
                        txtBudgetMsg.setText("Warning: Budget is less than total expenses!");
                        txtBudgetMsg.setVisibility(View.VISIBLE);
                    } else {
                        txtBudgetMsg.setVisibility(View.GONE);
                    }
                    Toast.makeText(DashboardActivity.this, "Budget updated!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(DashboardActivity.this, "Operation cancelled.", Toast.LENGTH_SHORT).show();
                }

                alertDialog.dismiss();
            }
        });
        builder.setView(view);

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void logout() {
        email = null;
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}