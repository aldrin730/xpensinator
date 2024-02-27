package com.example.xpensinator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ExpensesAdapter extends ArrayAdapter<String> {
    private List<String> expensesList;
    private Context context;

    public ExpensesAdapter(Context context, List<String> expensesList) {
        super(context, 0, expensesList);
        this.context = context;
        this.expensesList = expensesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expenses_list, parent, false);
        }

        // Get the expense item for this position
        String expense = expensesList.get(position);

        String[] parts = expense.split(",");

        // Lookup view for data population
        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        TextView textViewExpenseCategory = convertView.findViewById(R.id.textViewExpenseCategory);
        TextView textViewTotalExpense = convertView.findViewById(R.id.textViewTotalExpense);
        TextView textViewNotes = convertView.findViewById(R.id.textViewNotes);

        // Populate the data into the template view using the expense object
        textViewDate.setText(parts[0]); // Assuming date is the first part of the string
        textViewExpenseCategory.setText(parts[1]); // Assuming category is the second part
        textViewTotalExpense.setText(parts[2]); // Assuming total expense is the third part
        textViewNotes.setText(parts[3]); // Assuming notes is the fourth part

        // Return the completed view to render on screen
        return convertView;
    }
}
