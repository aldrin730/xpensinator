package com.example.xpensinator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "xpensinator";
    private static final String TABLE_USERS = "Users";
    private static final String TABLE_EXPENSECATEGORIES = "Expense_Categories";
    private static final String TABLE_EXPENSES = "Expenses";

    // Users Table
    private static final String KEY_USER_ID = "UserID";
    private static final String KEY_FIRST_NAME = "FirstName";
    private static final String KEY_LAST_NAME = "LastName";
    private static final String KEY_EMAIL = "Email";
    private static final String KEY_PASSWORD = "Password";
    private static final String KEY_REG_DATE = "RegDate";

    // Expense Categories Table
    private static final String KEY_E_CATEGORY_ID = "E_CategoryID";
    private static final String KEY_E_CATEGORY_NAME = "E_CategoryName";

    // Expenses Table
    private static final String KEY_EXPENSE_ID = "ExpenseID";
    private static final String KEY_IMAGE_URL = "ImageURL";
    private static final String KEY_TOTAL_EXPENSE = "TotalExpense";
    private static final String KEY_EXPENSE_DATE = "ExpenseDate";
    private static final String KEY_NOTES = "Notes";

    public DBHandler(@Nullable Context context) {
        super(context,DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_FIRST_NAME + " TEXT NOT NULL,"
                + KEY_LAST_NAME + " TEXT NOT NULL,"
                + KEY_PASSWORD + " TEXT NOT NULL,"
                + KEY_EMAIL + " TEXT NOT NULL,"
                + KEY_REG_DATE + " DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_USERS);

        String CREATE_TABLE_EXPENSES_CATEGORIES = "CREATE TABLE " + TABLE_EXPENSECATEGORIES + "("
                + KEY_E_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_E_CATEGORY_NAME + " TEXT NOT NULL"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_EXPENSES_CATEGORIES);

        String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_EXPENSES + "("
                + KEY_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER NOT NULL,"
                + KEY_E_CATEGORY_ID + " INTEGER NOT NULL,"
                + KEY_IMAGE_URL + " TEXT NOT NULL,"
                + KEY_TOTAL_EXPENSE + " TEXT NOT NULL,"
                + KEY_EXPENSE_DATE + " DATETIME NOT NULL,"
                + KEY_NOTES + " TEXT NOT NULL,"
                + "FOREIGN KEY (" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ") ON UPDATE CASCADE ON DELETE NO ACTION,"
                + "FOREIGN KEY (" + KEY_E_CATEGORY_ID + ") REFERENCES " + TABLE_EXPENSECATEGORIES + "(" + KEY_E_CATEGORY_ID + ") ON UPDATE CASCADE ON DELETE NO ACTION"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_EXPENSES);

        prepopulateExpenseCategories(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older tables if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSECATEGORIES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    private void prepopulateExpenseCategories (SQLiteDatabase sqLiteDatabase) {
        String[] categories = {"Food", "Transportation", "Housing", "Entertainment", "Utilities", "Healthcare", "Education", "Clothing", "Others"};
        for (String category : categories) {
            insertExpenseCategory(sqLiteDatabase, category);
        }
    }

    void insertUser(String firstName, String lastName, String password, String email, String regDate) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_FIRST_NAME, firstName);
        cValues.put(KEY_LAST_NAME, lastName);
        cValues.put(KEY_PASSWORD, password);
        cValues.put(KEY_EMAIL, email);
        cValues.put(KEY_REG_DATE, regDate);
        long userId = sqLiteDatabase.insert(TABLE_USERS,null, cValues);
        sqLiteDatabase.close();
    }

    void insertExpense(int userId, int categoryId, String imageUrl, String totalExpense, String expenseDate, String notes) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_USER_ID, userId);
        cValues.put(KEY_E_CATEGORY_ID, categoryId);
        cValues.put(KEY_IMAGE_URL, imageUrl);
        cValues.put(KEY_TOTAL_EXPENSE, totalExpense);
        cValues.put(KEY_EXPENSE_DATE, expenseDate);
        cValues.put(KEY_NOTES, notes);
        long expenseId = sqLiteDatabase.insert(TABLE_EXPENSES,null, cValues);
        sqLiteDatabase.close();
    }

    void insertExpenseCategory (SQLiteDatabase sqLiteDatabase, String expCategory) {
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_E_CATEGORY_NAME, expCategory);
        long expCategoryId = sqLiteDatabase.insert(TABLE_EXPENSECATEGORIES,null, cValues);
    }

    @SuppressLint("Range")
    public List<String> getAllExpenseCategories() {
        List<String> categories = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSECATEGORIES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(cursor.getColumnIndex(KEY_E_CATEGORY_NAME));
                categories.add(categoryName);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return categories;
    }

}
