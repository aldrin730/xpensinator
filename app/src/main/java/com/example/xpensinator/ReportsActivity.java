package com.example.xpensinator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    BarChart barChart;
    ExpensesAdapter expensesAdapter;
    List<String> expensesList;
    private String email;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        barChart = findViewById(R.id.barChart);

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

        DBHandler dbHandler = new DBHandler(this, email);

        List<String> categoryLabels = dbHandler.getCategoryLabels();

        if (categoryLabels != null) {
            populateChart();
        } else {
            Toast.makeText(this, "Category labels list is null", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final String email = getIntent().getStringExtra("email");
        int id = item.getItemId();
        if (id == R.id.nav_enter_expense) {
            Intent intent1 = new Intent(ReportsActivity.this, MainActivity.class);
            intent1.putExtra("email", email);
            startActivity(intent1);
        } else if (id == R.id.nav_dashboard) {
            Intent intent2 = new Intent(ReportsActivity.this, DashboardActivity.class);
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