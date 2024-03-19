package com.example.xpensinator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ExpensesAdapter extends ArrayAdapter<String> {
    public List<String> expensesList;
    private Context context;

    public ExpensesAdapter(Context context, List<String> expensesList) {
        super(context, 0, expensesList);
        this.context = context;
        this.expensesList = expensesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expenses_list, parent, false);
        }


        String expense = expensesList.get(position);

        String[] parts = expense.split(",");


        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        TextView textViewExpenseCategory = convertView.findViewById(R.id.textViewExpenseCategory);
        TextView textViewTotalExpense = convertView.findViewById(R.id.textViewTotalExpense);
        TextView textViewNotes = convertView.findViewById(R.id.textViewNotes);


        textViewDate.setText(parts[0]);
        textViewExpenseCategory.setText(parts[1]);
        textViewTotalExpense.setText(parts[2]);
        textViewNotes.setText(parts[3]);


        return convertView;
    }
}
