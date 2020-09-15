package com.rohit.examples.android.bookstore.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rohit.examples.android.bookstore.Listener.onQuantityChangeListener;
import com.rohit.examples.android.bookstore.R;
import com.rohit.examples.android.bookstore.Utility.Utils;

import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_AUTHOR;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_COVER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_PRICE;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_QUANTITY;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry._ID;

public class BookCursorAdapter extends CursorAdapter {

    private final onQuantityChangeListener quantityChangeListener;
    // Variable declaration for toast and listener for quantity change
    private Toast toast;

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        quantityChangeListener = (onQuantityChangeListener) context;
    }

    /**
     * Inflating the list item layout on to the listView
     *
     * @param context   reference to activity context that provides access to application resources
     * @param cursor    reference to the cursor holding the data for the individual list item
     * @param viewGroup viewGroup to which the individual list item view has to be attached
     * @return newly inflated list view item
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.layout_booklist_item, viewGroup, false);
    }

    /**
     * Binding the individual views of the list item to current activity context
     *
     * @param view    reference to individual list item view
     * @param context reference to activity context that provides access to application resources
     * @param cursor  reference to the cursor holding the data for the individual list item
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView book_cover_image = view.findViewById(R.id.imageView);
        TextView book_name_text = view.findViewById(R.id.book_title);
        TextView author_name_text = view.findViewById(R.id.author_text);
        TextView quantity_text = view.findViewById(R.id.quantity);
        TextView price_text = view.findViewById(R.id.price);
        final Button sell_btn = view.findViewById(R.id.sell_button);

        // Setting the title TextView to not extend to more than 1 line
        book_name_text.setSingleLine();

        // Getting the column indices for the required fields
        int rowIndex = cursor.getColumnIndex(_ID);
        int imageColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_COVER);
        int bookColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_NAME);
        int authorColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
        int priceColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_QUANTITY);

        // Fetching the values for each field
        String imageUri = cursor.getString(imageColumnIndex);
        final long rowId = cursor.getLong(rowIndex);
        final String name = cursor.getString(bookColumnIndex);
        String author = cursor.getString(authorColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);
        final int[] quantity = {cursor.getInt(quantityColumnIndex)};

        // Displaying the book details
        book_cover_image.setImageURI(Uri.parse(imageUri));
        book_name_text.setText(name);
        author_name_text.setText(author);
        price_text.setText(String.valueOf(price));
        quantity_text.setText(String.valueOf(quantity[0]));

        // Enable/disable sell button depending on stock availability
        if (quantity[0] == 0) {
            Utils.disableButton(context, sell_btn);
        } else {
            Utils.enableButton(context, sell_btn);
        }

        // Setting on click listener for the sell button
        sell_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantityChangeListener != null) {
                    //decrease the product quantity, ensuring it doesn't go below 0
                    if (quantity[0] > 0) {
                        quantity[0]--;
                        // Method call to updateStockQuantity to update the value in the database
                        quantityChangeListener.updateStockQuantity(rowId, quantity[0]);
                    }
                    if (quantity[0] == 0) {
                        if (toast != null) {
                            // Cancel any pending toasts
                            toast.cancel();
                        }
                        // Displaying out of stock error when the quantity reduces to 0 (zero)
                        toast = Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_SHORT);
                        toast.show();
                        // Disabling sell button
                        Utils.disableButton(context, sell_btn);
                    } else {
                        // If Book is available in stock, enable sell button
                        Utils.enableButton(context, sell_btn);
                    }
                }
            }
        });
    }
}