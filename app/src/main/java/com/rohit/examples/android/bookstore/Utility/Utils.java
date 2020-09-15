package com.rohit.examples.android.bookstore.Utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import com.rohit.examples.android.bookstore.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.rohit.examples.android.bookstore.Data.BookContract.LOG_TAG;

public class Utils {

    /**
     * Scaling the newly picked image to match the destination view size to handle memory resources efficiently
     *
     * @param context context reference to access application resources
     * @param image   ImageView reference to determine it's width and height
     * @param uri     uri of the newly picked image
     * @return Bitmap object of the newly scaled image
     */
    public static Bitmap getBitmapFromUri(Context context, ImageView image, Uri uri) {

        // Getting the dimensions of the view
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        InputStream inputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);

            // Getting the dimensions of the bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }

            int targetWidth = options.outWidth;
            int targetHeight = options.outHeight;

            // Determining the scaling factor for the image
            int scaleFactor = Math.min(imageWidth / targetWidth, imageHeight / targetHeight);

            // Decoding the image file into a Bitmap that is sized to fill the view
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;

            inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Failed to load the image", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "I/O error when trying to load the image", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Method definition to hide soft keyboard
     *
     * @param activity reference to activity to access application resources
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Method definition to enable a sell/order button
     *
     * @param context reference to context to enable access to application resources
     * @param button  button for which the state has to be enabled
     */
    public static void enableButton(Context context, Button button) {
        if (!button.isEnabled()) {
            //enable sale button in case it was previously disabled
            button.setEnabled(true);
            //update the button text color for enabled state
            switch (button.getId()) {
                case R.id.sell_button:
                    button.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    ((GradientDrawable) button.getBackground()).setColor(context.getResources().getColor(R.color.colorAccent));
                    break;
                case R.id.order_btn:
                    button.setTextColor(context.getResources().getColor(android.R.color.white));
                    //restore the background color for order button
                    ((GradientDrawable) button.getBackground()).setColor(context.getResources().getColor(R.color.colorAccent));
            }
        }

    }

    /**
     * Method definition to disable a sell/order button
     *
     * @param context reference to context to enable access to application resources
     * @param button  button for which the state has to be disabled
     */
    public static void disableButton(Context context, Button button) {
        if (button.isEnabled()) {
            //disable the sell/order button
            button.setEnabled(false);
            //change the button text color to gray
            button.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            //change the background color to gray for order button
            if (button.getId() == R.id.order_btn) {
                ((GradientDrawable) button.getBackground()).setColor((context.getResources().getColor(R.color.lightGray)));
            }
        }
    }
}