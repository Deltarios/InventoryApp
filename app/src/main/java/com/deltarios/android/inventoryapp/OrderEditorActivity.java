package com.deltarios.android.inventoryapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deltarios.android.inventoryapp.data.ProductContract;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Deltarios on 03/06/17.
 */

public class OrderEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private String mActionIntent;

    private EditText mProductName;

    private EditText mProviderEmail;

    private TextView mQuantityProductTextView;

    private Button mDecrementButton;

    private Button mIncrementButton;

    private Button mOrderButton;

    private ImageView mImageProduct;

    private Uri mCurrentProductUri;

    private int mQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_editor);

        Intent intent = getIntent();

        mCurrentProductUri = intent.getData();

        mActionIntent = intent.getAction();

        if (mActionIntent.equals("order")) {
            setTitle(getString(R.string.order_editor_activity_title));
        } else {
            setTitle(getString(R.string.shipping_editor_activity_title));
        }
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        mProductName = (EditText) findViewById(R.id.edit_product_name_order);
        mProviderEmail = (EditText) findViewById(R.id.edit_provider_email);
        mQuantityProductTextView = (TextView) findViewById(R.id.quantity_text_view);
        mDecrementButton = (Button) findViewById(R.id.btn_decrement);
        mIncrementButton = (Button) findViewById(R.id.btn_increment);
        mOrderButton = (Button) findViewById(R.id.btn_submit_order);
        mImageProduct = (ImageView) findViewById(R.id.product_image_view_order_editor);

        if (!mActionIntent.equals("order")) {
            mProviderEmail.setHint(getString(R.string.client_email));
        } else {
            mProviderEmail.setHint(getString(R.string.provider_email));
        }

        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantity > 0) {
                    mQuantity = mQuantity - 1;
                    displayQuantity(mQuantity);
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.limit_product_less), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuantity = mQuantity + 1;
                displayQuantity(mQuantity);
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date day = new Date();
                SimpleDateFormat formatDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ROOT);
                String currentDay = formatDate.format(day);

                String nameProduct = mProductName.getText().toString().trim();
                String[] emailProvider = {mProviderEmail.getText().toString().trim()};
                String quantityCurrent = mQuantityProductTextView.getText().toString().trim();

                mImageProduct.buildDrawingCache();
                Bitmap imageProduct = mImageProduct.getDrawingCache();

                if (!emailProvider[0].equals("") && !emailProvider[0].equals(" ") && !emailProvider[0].isEmpty()) {
                    composeEmail(emailProvider, quantityCurrent, currentDay, nameProduct, imageProduct);

                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.msg_error_provider_email), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void composeEmail(String[] emailProvider, String quantityCurrent, String currentDay, String nameProduct, Bitmap imageProduct) {

        File file = new File(getExternalCacheDir(), imageProduct + ".png");

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            imageProduct.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        }

        String subject = getString(R.string.msg_subject);

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, emailProvider);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, createdOrder(quantityCurrent, currentDay, nameProduct));
        if (file != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        }

        intent.setType("*/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 225);
        } else {
            Toast.makeText(this, getString(R.string.msg_error_email), Toast.LENGTH_SHORT).show();
        }
    }

    private String createdOrder(String quantityCurrent, String currentDay, String nameProduct) {
        String displayDay = " Date today is: " + currentDay;
        String quantityDisplay = "Quantity: " + quantityCurrent;
        String nameProductDisplay = "The product to order is: " + nameProduct;

        return displayDay +
                "\n" + getString(R.string.order_text) +
                "\n" + nameProductDisplay +
                "\n" + quantityDisplay;
    }

    private void displayQuantity(int mQuantity) {
        mQuantityProductTextView.setText(Integer.toString(mQuantity));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PROVIDER,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);

            mProductName.setText(name);
            mImageProduct.setImageBitmap(getImage(image));
        }
    }

    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mImageProduct.setImageResource(R.mipmap.ic_launcher);
    }
}