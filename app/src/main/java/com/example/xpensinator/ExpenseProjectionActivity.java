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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseProjectionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Button calculateProjectionButton;
    private TextView projectedExpensesTextView;
    private String email;
    private LineChart lineChart;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_projection);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        email = getIntent().getStringExtra("email");
        DBHandler dbHandler = new DBHandler(this, email);

        calculateProjectionButton = findViewById(R.id.calculate_projection_button);
        projectedExpensesTextView = findViewById(R.id.projected_expenses_text_view);
        lineChart = findViewById(R.id.line_chart);

        calculateProjectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAndDisplayProjection();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_enter_expense) {
            Intent intent1 = new Intent(ExpenseProjectionActivity.this, MainActivity.class);
            intent1.putExtra("email", email);
            startActivity(intent1);
        } else if (id == R.id.nav_dashboard) {
            Intent intent2 = new Intent(ExpenseProjectionActivity.this, DashboardActivity.class);
            intent2.putExtra("email", email);
            startActivity(intent2);
        } else if (id == R.id.nav_projection) {
            Intent intent3 = new Intent(ExpenseProjectionActivity.this, ExpenseProjectionActivity.class);
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

    private void calculateAndDisplayProjection() {
        DBHandler dbHandler = new DBHandler(this, email);
        List<String> expensesList = dbHandler.getAllExpensesFromDatabase(email);

        double projectedExpenses = calculateProjectedExpenses(expensesList);

        projectedExpensesTextView.setText(String.format("Projected Expenses: $%.2f", projectedExpenses));
        updateLineChart(expensesList, projectedExpenses);
    }

    private double calculateProjectedExpenses(List<String> expensesList) {
        if (expensesList.isEmpty()) {
            return 0.0;
        }

        double totalExpenses = 0;
        for (String expense : expensesList) {
            String[] parts = expense.split(",");
            double expenseAmount = Double.parseDouble(parts[2]);
            totalExpenses += expenseAmount;
        }

        double averageExpense = totalExpenses / expensesList.size();

        int numberOfMonths = 3;
        double projectedExpenses = averageExpense * numberOfMonths;

        return projectedExpenses;
    }
    private void updateLineChart(List<String> expensesList, double projectedExpenses) {
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < expensesList.size(); i++) {
            String[] parts = expensesList.get(i).split(",");
            double expenseAmount = Double.parseDouble(parts[2]);
            entries.add(new Entry(i, (float) expenseAmount));
        }

        entries.add(new Entry(expensesList.size(), (float) projectedExpenses));

        LineDataSet dataSet = new LineDataSet(entries, "Expenses");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        Description description = new Description();
        description.setText("Expense Projection");
        lineChart.setDescription(description);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        lineChart.invalidate();
    }

    private void logout() {
        email = null;
        Intent intent = new Intent(ExpenseProjectionActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}