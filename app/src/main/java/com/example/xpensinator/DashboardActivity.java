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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.xpensinator.ReportsActivity;
import com.example.xpensinator.DBHandler;

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
    Button btnGraph;
    Button btnFilter2;
    Spinner monthSpinner, yearSpinner;

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
        btnGraph = findViewById(R.id.btnGraph);
        btnFilter2 = findViewById(R.id.btnFilter2);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);

        email = getIntent().getStringExtra("email");
        final String firstName = getCurrentUserFirstName(email);

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DBHandler dbHandler = new DBHandler(this, email);

        txtWelcome.setText(getString(R.string.lblWelcome) + " " + firstName);

        String currentMonth = getCurrentYearAndMonth();
        String spnCurrentMonth = getCurrentMonth();

        double lastBudget = 0.00;

        lastBudget = dbHandler.getLastBudgetForMonth(email, currentMonth);

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

        totalExpenses = dbHandler.getTotalExpensesForMonth(email, currentMonth);
        txtTotalExp.setText(String.format(Locale.getDefault(), "%.2f", totalExpenses));

        List<String> expensesList = dbHandler.getAllExpensesFromDatabase(email);

        ExpensesAdapter expensesAdapter = new ExpensesAdapter(this, expensesList);
        expensesListView.setAdapter(expensesAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        int currentMonthPosition = adapter.getPosition(spnCurrentMonth);
        monthSpinner.setSelection(currentMonthPosition);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // updateExpensesList(getSelectedMonth());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection
            }
        });

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // updateExpensesList(getSelectedMonth());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnFilter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterExpensesByMonth();
            }
        });

        if (lastBudget != 0.00 && lastBudget < totalExpenses) {
            txtBudgetMsg.setText("Warning: You have exceeded your budget!");
            txtBudgetMsg.setVisibility(View.VISIBLE);
        } else {
            txtBudgetMsg.setVisibility(View.GONE);
        }

        btnGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processExpensesDataAndShowGraph();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_enter_expense) {
            Intent intent1 = new Intent(DashboardActivity.this, MainActivity.class);
            intent1.putExtra("email", email);
            startActivity(intent1);
        } else if (id == R.id.nav_dashboard) {
            Intent intent2 = new Intent(DashboardActivity.this, DashboardActivity.class);
            intent2.putExtra("email", email);
            startActivity(intent2);
        } else if (id == R.id.nav_projection) {
            Intent intent2 = new Intent(DashboardActivity.this, ExpenseProjectionActivity.class);
            intent2.putExtra("email", email);
            startActivity(intent2);
        }
        else if (id == R.id.nav_logout) {
            logout();
            return true;
        }

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

    private String getCurrentUserFirstName(String email) {
        DBHandler dbHandler = new DBHandler(this, email);
        return dbHandler.getFirstName(email);
    }

        private String getCurrentYearAndMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month is zero-based, so add 1
        return String.format(Locale.getDefault(), "%04d-%02d", year, month);
    }

    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; // Month is zero-based, so add 1
        return String.format(Locale.getDefault(), "%02d", month);
    }

    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Budget");

        View view = LayoutInflater.from(this).inflate(R.layout.popup_set_budget, null);
        EditText txtSetBudget = view.findViewById(R.id.txtSetBudget);
        Button btnSaveBudget = view.findViewById(R.id.btnSaveBudget);

        final String email = getIntent().getStringExtra("email");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
        String selectedMonth = monthFormat.format(calendar.getTime());
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newBudget = txtSetBudget.getText().toString();
                if (newBudget != null) {
                    double newBudgetValue = Double.parseDouble(newBudget);
                    String formattedBudget = String.format(Locale.getDefault(), "%.2f", newBudgetValue);
                    txtBudget.setText(formattedBudget);

                    DBHandler dbHandler = new DBHandler(DashboardActivity.this, email);
                    dbHandler.saveBudget(newBudgetValue, email, selectedMonth);

                    if (newBudgetValue < totalExpenses) {
                        txtBudgetMsg.setText("Warning: You have exceeded your budget!");
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

    private String filterExpensesByMonth() {
        String tmp_selectedYear = yearSpinner.getSelectedItem().toString();
        String tmp_selectedMonth = monthSpinner.getSelectedItem().toString();
        String selectedMonth = tmp_selectedYear + "-" + tmp_selectedMonth;

        displayExpensesForMonth(selectedMonth);

        DBHandler dbHandler = new DBHandler(this, email);
        totalExpenses = dbHandler.getTotalExpensesForMonth(email, selectedMonth);
        txtTotalExp.setText(String.format(Locale.getDefault(), "%.2f", totalExpenses));

        double budgetForMonth = dbHandler.getLastBudgetForMonth(email, selectedMonth);
        txtBudget.setText(String.format(Locale.getDefault(), "%.2f", budgetForMonth));

        return selectedMonth;
    }

    private void displayExpensesForMonth(String selectedMonth) {
        DBHandler dbHandler = new DBHandler(this, email);
        List<String> expensesForMonth = dbHandler.getExpensesForMonth(email, selectedMonth);

        expensesAdapter = new ExpensesAdapter(this, expensesForMonth);
        expensesListView.setAdapter(expensesAdapter);
    }

    private void processExpensesDataAndShowGraph() {
        if (email != null) {
            DBHandler dbHandler = new DBHandler(this, email);

            List<String> expensesList = dbHandler.getAllExpensesFromDatabase(email);

            if (expensesList != null && !expensesList.isEmpty()) {
                Intent intent = new Intent(DashboardActivity.this, ReportsActivity.class);
                intent.putStringArrayListExtra("expensesList", (ArrayList<String>) expensesList);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                Toast.makeText(DashboardActivity.this, "No expenses data available", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where email is null
            Toast.makeText(DashboardActivity.this, "Email is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        email = null;
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}