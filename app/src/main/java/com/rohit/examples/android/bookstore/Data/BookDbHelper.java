package com.rohit.examples.android.bookstore.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_AUTHOR;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_COVER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_ISBN;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_PRICE;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_QUANTITY;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_SUPPLIER_EMAIL;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.TABLE_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry._ID;

class BookDbHelper extends SQLiteOpenHelper {

    // Assigning a name to the database schema
    private static final String DATABASE_NAME = "books.db";

    // Database version to keep track if the schema is changed, the database version is incremented
    private static final int DATABASE_VERSION = 2;

    /*
     *  Database table creation with constant column fields
     */
    private static final String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_BOOK_COVER + " TEXT NOT NULL, "
            + COLUMN_BOOK_NAME + " TEXT NOT NULL, "
            + COLUMN_BOOK_AUTHOR + " TEXT NOT NULL, "
            + COLUMN_BOOK_ISBN + " TEXT NOT NULL, "
            + COLUMN_BOOK_PRICE + " REAL NOT NULL, "
            + COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL, "
            + COLUMN_BOOK_SUPPLIER_NAME + " TEXT NOT NULL, "
            + COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
            + COLUMN_BOOK_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL );";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Method to create database for the first time
     *
     * @param sqLiteDatabase to create, execute SQL commands and perform other database tasks
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    /**
     * Method when database needs to be upgraded
     *
     * @param sqLiteDatabase to create, execute SQL commands and perform other database tasks
     * @param oldVer         for old database version
     * @param newVer         for new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        sqLiteDatabase.execSQL(DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}