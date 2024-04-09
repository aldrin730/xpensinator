package com.example.xpensinator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    BarChart barChart;
    ExpensesAdapter expensesAdapter;
    List<String> expensesList;
    private String email;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Button btnFilter;
    private Calendar calendar;
    private DBHandler dbHandler;
    TextView txtSelectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        barChart = findViewById(R.id.barChart);
        btnFilter = findViewById(R.id.btnFilter);
        calendar = Calendar.getInstance();
        txtSelectedMonth = findViewById(R.id.txtSelectedMonth);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        expensesList = getIntent().getStringArrayListExtra("expensesList");
        expensesAdapter = new ExpensesAdapter(this, expensesList);

        email = getIntent().getStringExtra("email");

        dbHandler = new DBHandler(this, email);

        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setDrawLabels(false);

        List<String> categoryLabels = dbHandler.getCategoryLabels();

        if (categoryLabels != null && expensesList != null && !expensesList.isEmpty()) {
            populateChart();
        } else {
            Toast.makeText(this, "No expenses data available", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_enter_expense) {
            Intent intent1 = new Intent(ReportsActivity.this, MainActivity.class);
            intent1.putExtra("email", email);
            startActivity(intent1);
        } else if (id == R.id.nav_dashboard) {
            Intent intent2 = new Intent(ReportsActivity.this, DashboardActivity.class);
            intent2.putExtra("email", email);
            startActivity(intent2);
        } else if (id == R.id.nav_projection) {
            Intent intent3 = new Intent(ReportsActivity.this, ExpenseProjectionActivity.class);
            intent3.putExtra("email", email);
            startActivity(intent3);
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

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, 1);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
                String selectedMonth = dateFormat.format(calendar.getTime());

                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
                    Date date = inputFormat.parse(selectedMonth);

                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

                    String formattedMonth = outputFormat.format(date);

                    txtSelectedMonth.setText(formattedMonth);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Toast.makeText(ReportsActivity.this, "Selected month: " + selectedMonth.substring(0, 7), Toast.LENGTH_SHORT).show();

                filterExpenses(selectedMonth);
            }
        };

        new DatePickerDialog(
                ReportsActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void filterExpenses(String selectedMonth) {
        if (expensesAdapter != null) {
            List<String> filteredExpenses = dbHandler.getExpensesForMonth(email, selectedMonth);
            if (filteredExpenses != null && !filteredExpenses.isEmpty()) {
                expensesList = filteredExpenses;
                populateChart();
            } else {
                Toast.makeText(this, "No expenses found for the selected month", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void logout() {
        email = null;
        Intent intent = new Intent(ReportsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void populateChart() {
        if (expensesList != null && !expensesList.isEmpty()) {
            List<BarEntry> entries = new ArrayList<>();
            Map<String, Float> categoryExpensesMap = new HashMap<>();

            for (String expense : expensesList) {
                String[] parts = expense.split(",");
                String category = parts[1];
                float totalExpense = Float.parseFloat(parts[2]);

                float currentTotal = categoryExpensesMap.getOrDefault(category, 0f);
                categoryExpensesMap.put(category, currentTotal + totalExpense);
            }

            int index = 0;
            for (Map.Entry<String, Float> entry : categoryExpensesMap.entrySet()) {
                entries.add(new BarEntry(index++, entry.getValue()));
            }

            BarDataSet dataSet = new BarDataSet(entries, "Expenses per Category");

            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.BLACK);

            int barColor = Color.parseColor("#FEDC56");
            dataSet.setColor(barColor);

            List<String> categoryLabels = new ArrayList<>(categoryExpensesMap.keySet());

            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getBarLabel(BarEntry barEntry) {
                    int index = (int) barEntry.getX();
                    if (index >= 0 && index < categoryLabels.size()) {
                        return categoryLabels.get(index);
                    } else {
                        return "";
                    }
                }
            });

            BarData data = new BarData(dataSet);

            barChart.setData(data);

            barChart.invalidate();
        } else {
            Toast.makeText(this, "Expenses list is null or empty", Toast.LENGTH_SHORT).show();
        }
    }

    public class CategoryValueFormatter extends ValueFormatter {
        private List<String> categoryLabels;

        public CategoryValueFormatter(List<String> categoryLabels) {
            this.categoryLabels = categoryLabels;
        }

        @Override
        public String getBarLabel(BarEntry barEntry) {
            int index = (int) barEntry.getX();

            if (index >= 0 && index < categoryLabels.size()) {
                return categoryLabels.get(index);
            } else {
                return "";
            }
        }
    }
}