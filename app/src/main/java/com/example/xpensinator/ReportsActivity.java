package com.example.xpensinator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {
    BarChart barChart;
    List<String> expensesList;
    ExpensesAdapter expensesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        barChart = findViewById(R.id.barChart);
        expensesAdapter = new ExpensesAdapter(this, getExpensesList());


        populateChart();
    }

    private List<String> getExpensesList() {
        // Retrieve the expenses list from the ExpensesAdapter
        List<String> expensesList = new ArrayList<>();
        for (int i = 0; i < expensesAdapter.getCount(); i++) {
            expensesList.add(expensesAdapter.getItem(i));
        }
        return expensesList;
    }

    private void populateChart() {
        List<BarEntry> entries = new ArrayList<>();

        // Parse each expense to extract the category and total expense
        for (String expense : expensesAdapter.expensesList) {
            String[] parts = expense.split(",");
            String category = parts[1];
            float totalExpense = Float.parseFloat(parts[2]);
            // Add a new bar entry with the category as x-axis and total expense as y-axis
            entries.add(new BarEntry(entries.size(), totalExpense));
        }

        // Create a dataset with the bar entries and label it
        BarDataSet dataSet = new BarDataSet(entries, "Expenses per Category");

        // Create a BarData object and set the dataset
        BarData data = new BarData(dataSet);

        // Customize the appearance of the chart if needed

        // Set the data to the chart
        barChart.setData(data);

        // Refresh the chart
        barChart.invalidate();
    }
}