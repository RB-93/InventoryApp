package com.rohit.examples.android.bookstore.Activity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.rohit.examples.android.bookstore.Adapter.BookCursorAdapter;
import com.rohit.examples.android.bookstore.Listener.onQuantityChangeListener;
import com.rohit.examples.android.bookstore.R;

import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_AUTHOR;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_COVER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_PRICE;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_QUANTITY;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_SUPPLIER_EMAIL;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.CONTENT_URI;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry._ID;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, onQuantityChangeListener {

    private FloatingActionButton fab_add;

    private BookCursorAdapter bookCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting view IDs of the widgets
        // View variable declaration present on screen
        ListView product_listView = findViewById(R.id.listView);
        TextView textView = findViewById(R.id.empty_state_text);
        fab_add = findViewById(R.id.fab_add_book);

        //Launching Add new book activity through Intent
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DetailsActivity.class));
            }
        });

        // Setting empty state view if no Items are available
        product_listView.setEmptyView(textView);

        bookCursorAdapter = new BookCursorAdapter(this, null);
        product_listView.setAdapter(bookCursorAdapter);

        /*
         *  Scroll listener for ListView, to hide and show FAB accordingly
         */
        product_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int lastItem = i + i1;
                if (lastItem == i2) {
                    Animation scale_down = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_down);
                    fab_add.startAnimation(scale_down);
                    // fab_add.setVisibility(View.VISIBLE);
                } else {
                    Animation scale_up = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up);
                    fab_add.startAnimation(scale_up);
                    //    fab_add.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Setting onClickListener for each list item
        product_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Passing intent to Details activity to show Edit Book screen
                Intent intentDetail = new Intent(MainActivity.this, DetailsActivity.class);
                //Sending URI to identify specific book data
                intentDetail.setData(ContentUris.withAppendedId(CONTENT_URI, id));
                startActivity(intentDetail);
            }
        });

        // Loader initialized
        getLoaderManager().initLoader(1, null, this);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param i      The ID whose loader is to be created
     * @param bundle Any arguments supplied by the caller
     * @return Return a new Loader instance that is ready to start loading
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] book_projection = {
                _ID,
                COLUMN_BOOK_COVER,
                COLUMN_BOOK_NAME,
                COLUMN_BOOK_AUTHOR,
                COLUMN_BOOK_PRICE,
                COLUMN_BOOK_QUANTITY,
                COLUMN_BOOK_SUPPLIER_NAME,
                COLUMN_SUPPLIER_EMAIL,
                COLUMN_BOOK_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this, CONTENT_URI, book_projection, null, null, null);
    }

    /**
     * Called when a previously created loader has finished its load
     *
     * @param loader The Loader that has finished
     * @param cursor The data generated by the Loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //swap in a new cursor, return the old cursor
        bookCursorAdapter.swapCursor(cursor);
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable
     *
     * @param loader The Loader that is being reset
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //swap in a new cursor, return the old cursor
        bookCursorAdapter.swapCursor(null);
    }

    /**
     * Update Stock quantity on increasing or decreasing quantity
     *
     * @param rowId            Columns rowId for which the quantity is changed
     * @param newStockQuantity The quantity that to be set replacing old Quantity
     */
    @Override
    public void updateStockQuantity(long rowId, int newStockQuantity) {
        //set up a content values object and set the quantity value
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BOOK_QUANTITY, newStockQuantity);
        //define the specific product uri based on the row id
        Uri updateUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
        //invoke the update action via the content resolver
        getContentResolver().update(updateUri, contentValues, null, null);
    }
}