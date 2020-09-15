package com.rohit.examples.android.bookstore.Utility;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rohit.examples.android.bookstore.R;

import java.util.Random;

import static android.view.Window.FEATURE_NO_TITLE;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private final Context context;
    private final String bookName;
    private final String supplier_email;
    private final String supplier_phone;

    public CustomDialog(@NonNull Context context, String bookName, String supplierName, String supplierEmail, String supplierPhone) {
        super(context);
        this.requestWindowFeature(FEATURE_NO_TITLE);
        // Setting custom dialog layout
        this.setContentView(R.layout.layout_contact_dialog);

        this.context = context;
        this.bookName = bookName;
        this.supplier_email = supplierEmail;
        this.supplier_phone = supplierPhone;

        // Fetching View IDs from resource for Dialog interface
        TextView mSupplierName = findViewById(R.id.supplierName);
        TextView mSupplierEmail = findViewById(R.id.email);
        TextView mSupplierPhone = findViewById(R.id.phone);

        // Displaying supplier name with header text
        mSupplierName.setText(String.format(context.getString(R.string.contact_supplier), supplierName.toUpperCase()));

        //Verifying if supplier email is available then display/hide corresponding Contact TextView accordingly
        if (supplierEmail.length() > 0) {
            mSupplierEmail.setVisibility(View.VISIBLE);
            mSupplierEmail.setText(String.format(context.getString(R.string.email_at), supplier_email));
        } else {
            mSupplierEmail.setVisibility(View.GONE);
        }

        //Verifying if supplier phone is available and display/hide corresponding Contact TextView accordingly
        if (supplierPhone.length() > 0) {
            mSupplierPhone.setVisibility(View.VISIBLE);
            mSupplierPhone.setText(String.format(context.getString(R.string.call_at), supplier_phone));
        } else {
            mSupplierPhone.setVisibility(View.GONE);
        }

        // Setting click listeners to start implicit intents for email/phone
        mSupplierEmail.setOnClickListener(this);
        mSupplierPhone.setOnClickListener(this);
    }

    /**
     * Handling on click listeners for Dialog menu options
     *
     * @param view to get view ID of Dialog screen
     */
    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.email:
                // Setting email intent
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + supplier_email));
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format(context.getString(R.string.email_subject_text), bookName));
                intent.putExtra(Intent.EXTRA_TEXT, String.format(context.getString(R.string.email_body_text), new Random().nextInt(100), bookName.toUpperCase()));
                break;
            case R.id.phone:
                //Setting phone dial intent
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + supplier_phone));
                break;
        }
        // Launching appropriate intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            // Dismiss once the intent was successfully handled
            dismiss();
        } else {
            // To prompt user if no App is available to handle action
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show();
        }
    }
}