package com.rohit.examples.android.bookstore.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rohit.examples.android.bookstore.R;

import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_PRICE;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_QUANTITY;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_SUPPLIER_EMAIL;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.CONTENT_ITEM_TYPE;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.CONTENT_LIST_TYPE;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.TABLE_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry._ID;
import static com.rohit.examples.android.bookstore.Data.BookContract.CONTENT_AUTHORITY;
import static com.rohit.examples.android.bookstore.Data.BookContract.PATH_BOOKS;

public class BookContentProvider extends ContentProvider {

    // Integer code to identify URIs
    private static final int BOOK_CODE = 100;
    private static final int BOOK_CODE_WITH_ID = 101;

    // UriMatcher reference to match a given uri request
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Setting up the URIs to be matched with when a database request arises
    static {
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS, BOOK_CODE);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS + "/#", BOOK_CODE_WITH_ID);
    }

    private BookDbHelper bookDbHelper;

    /**
     * @return if we want to handle by own
     */
    @Override
    public boolean onCreate() {
        // Initialized database helper instance
        bookDbHelper = new BookDbHelper(getContext());
        return true;
    }

    /**
     * @param uri           The table name to compile the query against Uri
     * @param projection    A list of which columns to return. Passing null will return all columns,
     *                      which is discouraged to prevent reading data from storage that isn't going to be used.
     * @param selection     A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding
     *                      the WHERE itself). Passing null will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs,
     *                      in order that they appear in the selection. The values will be bound as Strings.
     * @param sortOrder     How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     *                      Passing null will use the default sort order, which may be unordered.
     * @return A Cursor object, which is positioned before the first entry. Note that Cursors are not synchronized,
     * see the documentation for more details.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = bookDbHelper.getReadableDatabase();
        Cursor cursor;

        // Verify if the query is for all records or for a single record based on the URI
        switch (uriMatcher.match(uri)) {
            case BOOK_CODE:
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case BOOK_CODE_WITH_ID:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI : " + uri);
        }
        if (getContext() != null) {
            //set up notifications to the uri to enable update sync
            // whenever a change occurs with the uri or it's descendants
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    /**
     * @param uri the URI to query
     * @return a MIME type string, or null if there is no type
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Defined the MIME type based on the uri
        switch (uriMatcher.match(uri)) {
            case BOOK_CODE:
                return CONTENT_LIST_TYPE;
            case BOOK_CODE_WITH_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + uriMatcher.match(uri));
        }
    }

    /**
     * @param uri           The content:// URI of the insertion request. This must not be null
     * @param contentValues A set of column_name/value pairs to add to the database. This must not be null
     * @return The URI for the newly inserted item, this value may be null
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase database = bookDbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOK_CODE:
                if (contentValues != null) {
                    // Validating the data to be inserted
                    validateInsertData(contentValues);
                    // Getting the row id after insert
                    long id = database.insert(TABLE_NAME, null, contentValues);
                    if (id != -1 && getContext() != null) {
                        // Notifying the change in uri
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                    // Returning uri with the newly added row ID
                    return ContentUris.withAppendedId(uri, id);
                }
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * @param uri           The full URI to query, including a row ID (if a specific record is requested)
     * @param selection     An optional restriction to apply to rows when deleting
     * @param selectionArgs This value may be null
     * @return The number of rows affected
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = bookDbHelper.getWritableDatabase();

        int rowsDeleted;

        // Identifying whether uri indicates deleting all rows or a specific row
        switch (uriMatcher.match(uri)) {

            case BOOK_CODE:
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case BOOK_CODE_WITH_ID:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0 && getContext() != null) {
            // Notifying the change in uri
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * @param uri           The URI to query. This can potentially have a record ID if this is an update request for a specific record.
     * @param contentValues A set of column_name/value pairs to update in the database. This must not be null
     * @param selection     An optional filter to match rows to update
     * @param selectionArgs This value may be null
     * @return The number of rows affected
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = bookDbHelper.getWritableDatabase();
        int rowsUpdated;

        if (contentValues == null || contentValues.size() == 0) {
            return 0;
        }

        // Validating the data to be updated
        validateUpdateData(contentValues);

        // Identifying whether uri indicates deleting all rows or a specific row
        switch (uriMatcher.match(uri)) {
            case BOOK_CODE:
                rowsUpdated = database.update(TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case BOOK_CODE_WITH_ID:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = database.update(TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        if (rowsUpdated != 0 && getContext() != null) {
            // Notifying the change in uri
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Method definition to validate data entered by the user
     *
     * @param contentValues set of column_name/value pairs to add to the database
     */
    private void validateInsertData(ContentValues contentValues) {
        String name = contentValues.getAsString(COLUMN_BOOK_NAME);
        if (name == null || TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException(String.valueOf(R.string.title_empty_error));
        }

        String priceInString = contentValues.getAsString(COLUMN_BOOK_PRICE);
        double price = priceInString != null && !TextUtils.isEmpty(priceInString) ? Double.parseDouble(priceInString) : 0;
        if (price < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.price_invalid_error));
        }

        String quantityInString = contentValues.getAsString(COLUMN_BOOK_QUANTITY);
        int quantity = quantityInString != null && !TextUtils.isEmpty(quantityInString) ? Integer.parseInt(quantityInString) : 0;
        if (quantity < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.negative_quantity_error));
        }

        String supplierName = contentValues.getAsString(COLUMN_BOOK_SUPPLIER_NAME);
        if (supplierName == null || TextUtils.isEmpty(supplierName)) {
            throw new IllegalArgumentException(String.valueOf(R.string.supplier_name_empty_error));
        }

        String supplierPhone = contentValues.getAsString(COLUMN_BOOK_SUPPLIER_PHONE_NUMBER);
        String supplierEmail = contentValues.getAsString(COLUMN_SUPPLIER_EMAIL);

        if (supplierPhone == null && supplierEmail == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.supplier_details_error));
        } else if (TextUtils.isEmpty(supplierPhone) && TextUtils.isEmpty(supplierEmail)) {
            throw new IllegalArgumentException(String.valueOf(R.string.supplier_details_error));
        }
    }

    /**
     * Validating data while editing and saving it
     *
     * @param contentValues set of column_name/value pairs to add to the database
     */
    private void validateUpdateData(ContentValues contentValues) {
        if (contentValues.containsKey(COLUMN_BOOK_QUANTITY) && contentValues.getAsString(COLUMN_BOOK_QUANTITY).length() > 0) {
            int quantity = contentValues.getAsInteger(COLUMN_BOOK_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.negative_quantity_error));
            }
        }

        if (contentValues.size() == 1 && contentValues.containsKey(COLUMN_BOOK_QUANTITY)) {
            return;
        }

        if (contentValues.containsKey(COLUMN_BOOK_NAME)) {
            String name = contentValues.getAsString(COLUMN_BOOK_NAME);
            if (name == null || TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException(String.valueOf(R.string.title_empty_error));
            }
        }

        if (contentValues.containsKey(COLUMN_BOOK_PRICE) && contentValues.getAsString(COLUMN_BOOK_PRICE).length() > 0) {
            double price = contentValues.getAsDouble(COLUMN_BOOK_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.price_invalid_error));
            }
        }

        if (contentValues.containsKey(COLUMN_BOOK_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(COLUMN_BOOK_SUPPLIER_NAME);
            if (supplierName == null || TextUtils.isEmpty(supplierName)) {
                throw new IllegalArgumentException(String.valueOf(R.string.supplier_name_empty_error));
            }
        }

        if (!contentValues.containsKey(COLUMN_BOOK_SUPPLIER_PHONE_NUMBER) && !contentValues.containsKey(COLUMN_SUPPLIER_EMAIL)) {
            throw new IllegalArgumentException(String.valueOf(R.string.supplier_details_error));
        }

        String supplierPhone = "";
        String supplierEmail = "";

        if (contentValues.containsKey(COLUMN_BOOK_SUPPLIER_PHONE_NUMBER)) {
            supplierPhone = contentValues.getAsString(COLUMN_BOOK_SUPPLIER_PHONE_NUMBER);
        }

        if (contentValues.containsKey(COLUMN_SUPPLIER_EMAIL)) {
            supplierEmail = contentValues.getAsString(COLUMN_SUPPLIER_EMAIL);
        }

        if (TextUtils.isEmpty(supplierPhone) && TextUtils.isEmpty(supplierEmail)) {
            throw new IllegalArgumentException(String.valueOf(R.string.supplier_details_error));
        }
    }
}