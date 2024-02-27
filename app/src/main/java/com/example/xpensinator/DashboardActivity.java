package com.example.xpensinator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    Button btnBudget;
    TextView txtBudget;
    AlertDialog alertDialog;
    TextView txtTotalExp;
    ListView expensesListView;
    ArrayAdapter<String> expensesAdapter;
    List<String> expensesList;
    TextView txtBudgetMsg;
    double totalExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnBudget = findViewById(R.id.btnBudget);
        txtBudget = findViewById(R.id.txtBudget);
        txtTotalExp = findViewById(R.id.txtTotalExp);
        txtBudgetMsg = findViewById(R.id.txtBudgetMsg);
        expensesListView = findViewById(R.id.expensesListView);
        DBHandler dbHandler = new DBHandler(this);
        btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetBudgetDialog();
            }
        });

        totalExpenses = dbHandler.getTotalExpensesFromDatabase();

        txtTotalExp.setText(String.format(Locale.getDefault(), "%.2f", totalExpenses));

        List<String> expensesList = dbHandler.getAllExpensesFromDatabase();

//        expensesList = dbHandler.getAllExpensesFromDatabase();
        ExpensesAdapter expensesAdapter = new ExpensesAdapter(this, expensesList);
//        expensesAdapter = new ArrayAdapter<>(this, R.layout.expenses_list, expensesList);
        expensesListView.setAdapter(expensesAdapter);
        Log.d("MyTag", "expensesList size: " + expensesList.size());
    }

    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Budget");

        // Inflate the layout for the dialog
        View view = LayoutInflater.from(this).inflate(R.layout.popup_set_budget, null);
        EditText txtSetBudget = view.findViewById(R.id.txtSetBudget);
        Button btnSaveBudget = view.findViewById(R.id.btnSaveBudget);
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newBudget = txtSetBudget.getText().toString();
                if (txtSetBudget != null) {
                    double newBudgetValue = Double.parseDouble(newBudget);
                    String formattedBudget = String.format(Locale.getDefault(), "%.2f", newBudgetValue);
                    txtBudget.setText(formattedBudget);

                    if (newBudgetValue < totalExpenses) {
                        txtBudgetMsg.setText("Warning: Budget is less than total expenses!");
                        txtBudgetMsg.setVisibility(View.VISIBLE); // Make the message visible
                    } else {
                        txtBudgetMsg.setVisibility(View.GONE); // Hide the message if the budget is sufficient
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
}