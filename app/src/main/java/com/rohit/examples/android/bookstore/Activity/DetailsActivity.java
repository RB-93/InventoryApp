package com.rohit.examples.android.bookstore.Activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.rohit.examples.android.bookstore.R;
import com.rohit.examples.android.bookstore.Utility.CustomDialog;
import com.rohit.examples.android.bookstore.Utility.Utils;

import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_AUTHOR;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_COVER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_ISBN;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_PRICE;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_QUANTITY;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_NAME;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.COLUMN_SUPPLIER_EMAIL;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry.CONTENT_URI;
import static com.rohit.examples.android.bookstore.Data.BookContract.BookEntry._ID;


public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, TextWatcher {

    private static final int PICK_IMAGE_REQUEST = 0;

    // Variable declaration for all the views available on screen
    private ImageView book_imageView;
    private EditText book_name_et;
    private EditText book_author_et;
    private EditText book_isbn_et;
    private EditText book_quantity_et;
    private EditText book_price_et;
    private EditText book_supplier_et;
    private EditText book_supp_phone_et;
    private EditText book_supp_email_et;

    private Button orderButton;

    // Variable for URI identification and fetching ImageURI
    private Uri uri;
    private Uri imageUri;
    private Toast toast;

    // Check set to saved & unsaved item value for Book data
    private boolean saveFeedback = true;
    private boolean unsavedChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Fetching view IDs for views from Resources
        book_imageView = findViewById(R.id.book_imageview);
        book_name_et = findViewById(R.id.book_et);
        book_author_et = findViewById(R.id.author_et);
        book_isbn_et = findViewById(R.id.isbn_et);
        book_quantity_et = findViewById(R.id.quantity_et);
        book_price_et = findViewById(R.id.price_et);
        book_supplier_et = findViewById(R.id.supplier_et);
        book_supp_phone_et = findViewById(R.id.supplier_phone_et);
        book_supp_email_et = findViewById(R.id.supplier_email_et);

        ImageButton descQuantity_btn = findViewById(R.id.decreaseQuantity);
        ImageButton incrQuantity_btn = findViewById(R.id.increaseQuantity);

        orderButton = findViewById(R.id.order_btn);

        // Setting support action bar with customized icon
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        }

        // Order button disable to prevent accidentally proceeding with multiple invalid inputs
        Utils.disableButton(this, orderButton);

        //Defining default image URI that would be displayed if no image is uploaded by the user
        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.no_book_image) +
                '/' + getResources().getResourceTypeName(R.drawable.no_book_image) +
                '/' + getResources().getResourceEntryName(R.drawable.no_book_image));

        //Getting intent to identify if this is to add a new product or edit an existing one
        Intent intent = getIntent();
        if (intent.getData() == null) {
            setTitle(getString(R.string.add_new_book));
            //Calling invalidate options menu to other menu items
            invalidateOptionsMenu();
            //Setting default imageUri to book_image
            book_imageView.setImageURI(imageUri);

            /*
             *  Registering text changed listeners for all edit texts to track any changes made so that
             *  the user can be alerted when there are any unsaved changes on exiting the activity
             */

            registerTextChangedListeners();
        } else {
            // Fetching the specific URI from the intent
            uri = intent.getData();
            setTitle(getString(R.string.edit_book));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);
            // Initialized the loader to fetch data for the current Book
            getLoaderManager().initLoader(1, null, this);
        }

        //Registering listeners for increase and decrease quantity buttons
        incrQuantity_btn.setOnClickListener(this);
        descQuantity_btn.setOnClickListener(this);

        //Registering listeners to upload new image
        book_imageView.setOnClickListener(this);
    }

    /**
     * Called to set up action bar menu for the activity
     *
     * @param menu menu reference to which the custom menu will be inflated
     * @return flag indicating whether menu set up was handled
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_book_menu, menu);
        return true;
    }

    /**
     * handle items clicked on the options menu
     *
     * @param item item selected from the options menu
     * @return flag indicating whether clicked menu item was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Handling save option
            case R.id.action_done:
                saveBook();
                // Close the edit screen on successfully saving the data to the database
                if (saveFeedback) {
                    finish();
                    return true;
                }
                return false;

            // Handling delete option
            case R.id.action_delete:
                // Display delete confirmation dialog to the user
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setting up any menu validations
     *
     * @param menu reference to action bar menu
     * @return boolean flag indicating whether the menu option preparations are handled
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //hide delete option if the intent to this activity is for adding a new product
        //instead of editing an existing product
        if (uri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Displays an alert dialog to confirm discarding any unsaved changes
     * on pressing back
     */
    private void showUnsavedChangesAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            //close the editing activity if the user chooses to discard changes
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            //dismiss the dialog if the user chooses cancel
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        // Display the alert dialog
        builder.create().show();
        // Reset unsaved changes flag
        unsavedChanges = false;
    }

    /**
     * Method call when the save button is clicked to save data to database
     */
    private void saveBook() {

        // Verify if there are any changes made that need saving, returned if there are no changes to be saved
        if (!unsavedChanges) {
            return;
        }

        // Fetching all the edit text data, trim any leading or following white spaces
        String book_name = book_name_et.getText().toString().trim();
        String author = book_author_et.getText().toString().trim();
        String isbn = book_isbn_et.getText().toString().trim();
        String price = book_price_et.getText().toString().trim();
        String quantity = book_quantity_et.getText().toString().trim();
        String supplierName = book_supplier_et.getText().toString().trim();
        String supplierPhone = book_supp_phone_et.getText().toString().trim();
        String supplierEmail = book_supp_email_et.getText().toString().trim();


        // If the user chose to add a new Book but did not enter any fields, return to exit out
        if (uri == null && TextUtils.isEmpty(book_name) && TextUtils.isEmpty(author) && TextUtils.isEmpty(isbn) &&
                TextUtils.isEmpty(price) && TextUtils.isEmpty(quantity) && TextUtils.isEmpty(supplierName) &&
                TextUtils.isEmpty(supplierPhone) && TextUtils.isEmpty(supplierEmail)) {
            return;
        }

        // Validating that the Book Title isn't an empty string
        if (TextUtils.isEmpty(book_name)) {
            // Set error accordingly
            book_name_et.requestFocus();
            book_name_et.setError(getString(R.string.title_empty_error));
            displayToastAlert(getString(R.string.title_empty_error));
            // To Indicate save wasn't successful
            saveFeedback = false;
            //Hiding soft keyboard to indicate to the user that field validation has failed
            Utils.hideSoftKeyboard(this);
            return;
        } else {
            // Update save success flag if validation succeeds
            saveFeedback = true;
        }

        if (TextUtils.isEmpty(author)) {
            // Setting default author text if the user has not entered author name
            author = getString(R.string.unknown_author);
        }

        if (TextUtils.isEmpty(isbn)) {
            // Setting default isbn text if the user has not entered isbn details
            isbn = getString(R.string.isbn_empty);
        }

        //Validating price field isn't empty
        if (TextUtils.isEmpty(price)) {
            // Set error accordingly
            book_price_et.setError("");
            displayToastAlert(getString(R.string.price_invalid_error));
            // Indicate save wasn't successful
            saveFeedback = false;
            // Hiding soft keyboard to indicate to the user that field validation has failed
            Utils.hideSoftKeyboard(this);
            return;
        } else {
            // Update save success flag if validation succeeds
            saveFeedback = true;
        }

        if (TextUtils.isEmpty(quantity)) {
            // Set error accordingly
            book_quantity_et.setError("");
            displayToastAlert(getString(R.string.quantity_empty_error));
            // Indicate save wasn't successful
            saveFeedback = false;
            // Hiding soft keyboard to indicate to the user that field validation has failed
            Utils.hideSoftKeyboard(this);
            return;
        } else {
            // Update save success flag if validation succeeds
            saveFeedback = true;
        }

        if (TextUtils.isEmpty(supplierName)) {
            // Set error accordingly
            book_supplier_et.setError("");
            displayToastAlert(getString(R.string.supplier_name_empty_error));
            // Indicate save wasn't successful
            saveFeedback = false;
            // Hide soft keyboard to indicate to the user that field validation has failed
            Utils.hideSoftKeyboard(this);
            return;
        } else {
            // update save success flag if validation succeeds
            saveFeedback = true;
        }

        if (TextUtils.isEmpty(supplierPhone) && TextUtils.isEmpty(supplierEmail)) {
            // Display error accordingly
            displayToastAlert(getString(R.string.supplier_details_error));
            // Indicate save wasn't successful
            saveFeedback = false;
            // Hiding soft keyboard to indicate to the user that field validation has failed
            Utils.hideSoftKeyboard(this);
            book_supplier_et.requestFocus();
            return;
        } else {
            // Update save success flag if validation succeeds
            saveFeedback = true;
        }

        // Setting up content values object to store all fields
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BOOK_COVER, imageUri.toString());
        contentValues.put(COLUMN_BOOK_NAME, book_name);
        contentValues.put(COLUMN_BOOK_AUTHOR, author);
        contentValues.put(COLUMN_BOOK_ISBN, isbn);
        contentValues.put(COLUMN_BOOK_PRICE, price);
        contentValues.put(COLUMN_BOOK_QUANTITY, quantity);
        contentValues.put(COLUMN_BOOK_SUPPLIER_NAME, supplierName);
        contentValues.put(COLUMN_BOOK_SUPPLIER_PHONE_NUMBER, supplierPhone);
        contentValues.put(COLUMN_SUPPLIER_EMAIL, supplierEmail);

        // Verify if this is a new product insert request
        if (uri == null) {
            // Invoking insert method via content resolver
            Uri newUri = getContentResolver().insert(CONTENT_URI, contentValues);

            // Display error/success alert to the user
            if (newUri == null) {
                displayToastAlert(getString(R.string.no_save));
            } else {
                displayToastAlert(getString(R.string.save_success_book));
            }
        } else {
            // Invoking update method via content resolver
            int rowsAffected = getContentResolver().update(uri, contentValues, null, null);

            // Displaying error/success alert to the user
            if (rowsAffected == 0) {
                displayToastAlert(getString(R.string.no_save));
            } else {
                displayToastAlert(getString(R.string.save_success_book));
            }
        }
    }

    /**
     * Displays alert message in the form of a toast
     *
     * @param message message to be displayed to the user
     */
    private void displayToastAlert(String message) {
        //cancel any outstanding toast before displaying a new one
        cancelToast();
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Cancel any existing toasts
     */
    private void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    /**
     * Displaying delete confirm dialog to the user before deleting a product
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //delete pet on confirmation
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss dialog if denied
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Display alert dialog
        builder.create().show();
    }

    /**
     * Deletes an existing book by the id specific uri
     */
    private void deleteBook() {
        if (uri != null) {
            //invoke delete action via the content resolver
            int rowsDeleted = getContentResolver().delete(uri, null, null);

            // Displaying error/success message accordingly
            if (rowsDeleted == 0) {
                displayToastAlert(getString(R.string.no_save));
            } else {
                displayToastAlert(getString(R.string.save_success_book));
            }
            finish();
        }
    }


    /**
     * creates a loader if one with specified ID doesn't exist when initialize loader or restart loader is called
     *
     * @param i      ID that indicates whether a loader needs to be created or one exists already
     * @param bundle bundle of arguments to be passed to the loader when initializing
     * @return a new cursor loader reference with the given ID
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // State the list of columns to be fetched
        String[] projection = {
                _ID,
                COLUMN_BOOK_COVER,
                COLUMN_BOOK_NAME,
                COLUMN_BOOK_AUTHOR,
                COLUMN_BOOK_ISBN,
                COLUMN_BOOK_PRICE,
                COLUMN_BOOK_QUANTITY,
                COLUMN_BOOK_SUPPLIER_NAME,
                COLUMN_BOOK_SUPPLIER_PHONE_NUMBER,
                COLUMN_SUPPLIER_EMAIL
        };
        return new CursorLoader(this, uri, projection, null, null, null);
    }

    /**
     * Called when a previously created loader has finished it's load
     *
     * @param loader reference to the cursor loader that has finished loading of the required data
     * @param cursor reference to the cursor that contains the data from the database
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //exit early if there is no valid data loaded in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // verify that the cursor contains data
        if (cursor.moveToFirst()) {

            // Getting column indices for the required fields
            int imageColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_COVER);
            int nameColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_NAME);
            int authorColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
            int isbnColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_ISBN);
            int priceColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(COLUMN_BOOK_SUPPLIER_PHONE_NUMBER);
            int supplierEmailColumnIndex = cursor.getColumnIndex(COLUMN_SUPPLIER_EMAIL);

            // Fetching the data obtained from the cursor
            imageUri = Uri.parse(cursor.getString(imageColumnIndex));
            String book_name = cursor.getString(nameColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String isbn = cursor.getString(isbnColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            // Setting the data in the edit texts and the image view
            book_imageView.setImageURI(imageUri);
            book_name_et.setText(book_name);
            book_name_et.setSelection(book_name_et.getText().length());
            book_author_et.setText(author);
            book_isbn_et.setText(isbn);
            book_price_et.setText(String.valueOf(price));
            book_quantity_et.setText(String.valueOf(quantity));
            book_supplier_et.setText(supplierName);
            book_supp_phone_et.setText(supplierPhone);
            book_supp_email_et.setText(supplierEmail);

            // Registering text changed listeners for all edit texts to track any changes made so that
            //the user can be alerted when there are any unsaved changes on exiting the activity
            registerTextChangedListeners();

            // Enable order button to allow the user to contact the supplier
            if (!TextUtils.isEmpty(supplierEmail) || !TextUtils.isEmpty(supplierPhone)) {
                Utils.enableButton(this, orderButton);
            }

            // Setting click functionality for order button to contact supplier via phone/email intent
            if (orderButton.isEnabled()) {
                orderButton.setOnClickListener(this);
            }
        }
    }

    /**
     * Registers text changed listeners for all editable fields
     */
    private void registerTextChangedListeners() {
        book_name_et.addTextChangedListener(this);
        book_author_et.addTextChangedListener(this);
        book_isbn_et.addTextChangedListener(this);
        book_price_et.addTextChangedListener(this);
        book_quantity_et.addTextChangedListener(this);
        book_supplier_et.addTextChangedListener(this);
        book_supp_phone_et.addTextChangedListener(this);
        book_supp_email_et.addTextChangedListener(this);
    }

    /**
     * Called when a previously created loader is being reset - makes the data associated with the loader unavailable
     *
     * @param loader reference to previously created cursor loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //reset the content of all edit texts and image view's resource
        book_imageView.setImageResource(0);
        book_name_et.setText("");
        book_author_et.setText("");
        book_isbn_et.setText("");
        book_price_et.setText("");
        book_quantity_et.setText("");
        book_supplier_et.setText("");
        book_supp_phone_et.setText("");
        book_supp_email_et.setText("");
    }

    /**
     * Handle action bar back navigation
     *
     * @return boolean flag indicating whether the action was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        if (unsavedChanges) {
            // Displaying dialog to alert the user of the unsaved changes, if any
            showUnsavedChangesAlert();
            return false;
        }
        // Closing the edit/add activity if there are no unsaved changes to be handled
        finish();
        return true;
    }

    /**
     * Handle click listeners for views
     *
     * @param view reference to the view that receives the click event
     */
    @Override
    public void onClick(View view) {

        // Declaration and initialization the  variable to hold and update quantity
        int quantity = 0;
        if (book_quantity_et.getText().length() > 0) {
            // Fetching the quantity from the edit text
            quantity = Integer.parseInt(book_quantity_et.getText().toString());
        }

        switch (view.getId()) {
            // Handle incrementing the quantity
            case R.id.increaseQuantity:
                quantity++;
                updateStockQuantity(quantity);
                break;
            // Handle decrementing the quantity
            case R.id.decreaseQuantity:
                if (quantity > 0) {
                    quantity--;
                    updateStockQuantity(quantity);
                } else {
                    // Ensure that the quantity doesn't go negative
                    displayToastAlert(getString(R.string.negative_quantity_error));
                }
                break;

            // Handle updating product image on the image view
            case R.id.book_imageview:
                Intent intent;
                // Create new intent to fetch an image from device storage
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                } else {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                // Setting intent type to filter images
                intent.setType("image/*");
                // Start activity and get the resulting URI if a new image is selected
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                break;

            case R.id.order_btn:
                CustomDialog dialog = new CustomDialog(
                        this,
                        book_name_et.getText().toString().trim(),
                        book_supplier_et.getText().toString().trim(),
                        book_supp_email_et.getText().toString().trim(),
                        book_supp_phone_et.getText().toString().trim());
                dialog.show();
                break;
        }
    }

    /**
     * Displays the updated quantity value in the edit text field
     *
     * @param quantity new quantity value
     */
    private void updateStockQuantity(int quantity) {
        //update the edit text with the new value, clear error alert if any
        book_quantity_et.setText(String.valueOf(quantity));
        book_quantity_et.setSelection(book_quantity_et.getText().length());
        book_quantity_et.setError(null);
    }

    /**
     * Called once startActivityForResult returns
     *
     * @param requestCode integer code to identify the type of intent request completed
     * @param resultCode  status of the intent action
     * @param data        data fetched on successful intent action
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //hold reference to the current image uri
        //if the user picks an image same as the existing one, do not update the uri
        Uri currentImageUri = imageUri;
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                //get the uri on successful intent action
                imageUri = data.getData();
                if (!currentImageUri.toString().equals(imageUri.toString())) {
                    //if a different image is picked, indicate that the image uri is to be updated in the database
                    unsavedChanges = true;
                }
                //update the image view with the newly chosen image after scaling the image size
                book_imageView.setImageBitmap(Utils.getBitmapFromUri(this, book_imageView, imageUri));
            }
        }
    }

    /**
     * Verify and display unsaved changes alert dialog to the user on back pressed
     */
    @Override
    public void onBackPressed() {
        if (unsavedChanges) {
            showUnsavedChangesAlert();
        } else {
            // Exit (go back) if there are no unsaved changes
            super.onBackPressed();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    // Override onTextChanged method for EditText fields to track any changes
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        unsavedChanges = true;
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}