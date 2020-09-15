package com.rohit.examples.android.bookstore.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.rohit.examples.android.bookstore.Activity.MainActivity;
import com.rohit.examples.android.bookstore.BuildConfig;

/*
 * Contract class to store constants defining Names for URIs, tables and columns.
 */
public class BookContract {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final String PATH_BOOKS = "books";
    private static final Uri BASE_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private BookContract() {

    }

    /**
     * Implementing the BaseColumns interface by which inner class can inherit a primary key field _ID
     */
    public static class BookEntry implements BaseColumns {
        public static final String TABLE_NAME = "Books";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_BOOK_COVER = "book_cover";
        public static final String COLUMN_BOOK_NAME = "book_title";
        public static final String COLUMN_BOOK_AUTHOR = "book_author";
        public static final String COLUMN_BOOK_ISBN = "book_isbn";
        public static final String COLUMN_BOOK_PRICE = "book_price";
        public static final String COLUMN_BOOK_QUANTITY = "book_quantity";
        public static final String COLUMN_BOOK_SUPPLIER_NAME = "book_supplier_name";
        public static final String COLUMN_SUPPLIER_EMAIL = "book_supplier_email";
        public static final String COLUMN_BOOK_SUPPLIER_PHONE_NUMBER = "book_supplier_phone_number";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, PATH_BOOKS);

        // Defining MIME types for the URIs
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
    }
}