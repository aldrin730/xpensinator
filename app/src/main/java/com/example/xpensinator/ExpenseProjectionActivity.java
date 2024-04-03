package com.example.xpensinator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseProjectionActivity extends AppCompatActivity {
    private Button calculateProjectionButton;
    private TextView projectedExpensesTextView;
    private String email;
    private LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_projection);

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

    private void calculateAndDisplayProjection() {
        DBHandler dbHandler = new DBHandler(this, email);
        List<String> expensesList = dbHandler.getAllExpensesFromDatabase(email);

        // Calculate projected expenses (Example: Average of past expenses)
        double projectedExpenses = calculateProjectedExpenses(expensesList);

        // Display the projected expenses
        projectedExpensesTextView.setText(String.format("Projected Expenses: $%.2f", projectedExpenses));
        updateLineChart(expensesList, projectedExpenses);
    }

    // Example method for calculating projected expenses
    private double calculateProjectedExpenses(List<String> expensesList) {
        if (expensesList.isEmpty()) {
            return 0.0; // If there are no expenses, return 0
        }

        // Calculate the total expenses
        double totalExpenses = 0;
        for (String expense : expensesList) {
            String[] parts = expense.split(",");
            double expenseAmount = Double.parseDouble(parts[2]); // Assuming index 2 contains the expense amount
            totalExpenses += expenseAmount;
        }

        // Calculate the average
        double averageExpense = totalExpenses / expensesList.size();

        // Assuming projection is based on the average of the last few months
        // Adjust this logic according to your requirement
        int numberOfMonths = 3; // Projection based on the last 3 months
        double projectedExpenses = averageExpense * numberOfMonths;

        return projectedExpenses;
    }
    private void updateLineChart(List<String> expensesList, double projectedExpenses) {
        ArrayList<Entry> entries = new ArrayList<>();

        // Add existing expenses to the line chart
        for (int i = 0; i < expensesList.size(); i++) {
            String[] parts = expensesList.get(i).split(",");
            double expenseAmount = Double.parseDouble(parts[2]); // Assuming index 2 contains the expense amount
            entries.add(new Entry(i, (float) expenseAmount));
        }

        // Add projected expenses to the line chart
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
}