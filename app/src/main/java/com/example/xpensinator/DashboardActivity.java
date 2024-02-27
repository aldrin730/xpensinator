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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnBudget = findViewById(R.id.btnBudget);
        txtBudget = findViewById(R.id.txtBudget);
        txtTotalExp = findViewById(R.id.txtTotalExp);
        expensesListView = findViewById(R.id.expensesListView);
        DBHandler dbHandler = new DBHandler(this);
        btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetBudgetDialog();
            }
        });

        double totalExpenses = dbHandler.getTotalExpensesFromDatabase();

        txtTotalExp.setText(String.format(Locale.getDefault(), "%.2f", totalExpenses));

        expensesList = dbHandler.getAllExpensesFromDatabase();
        expensesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expensesList);
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
                if (txtBudget != null) {
                    txtBudget.setText(newBudget);
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