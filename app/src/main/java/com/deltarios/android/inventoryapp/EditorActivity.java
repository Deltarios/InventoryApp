package com.deltarios.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.deltarios.android.inventoryapp.data.ProductContract;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private static final int REQUEST_CODE_GALLERY = 999;

    private EditText mNameEditText;

    private EditText mProviderEditText;

    private EditText mPriceEditText;

    private EditText mQuantityEditText;

    private Button mButtonSale;

    private ImageView mImageView;

    private Spinner mStockSpinner;

    private int mStock = ProductContract.ProductEntry.NOT_STOCK;

    private Uri mCurrentProductUri;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();

        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(R.string.editor_activity_title_new_product);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_activity_title_edit_product);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mProviderEditText = (EditText) findViewById(R.id.edit_provider_product);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price_product);
        mImageView = (ImageView) findViewById(R.id.product_image_view);
        mButtonSale = (Button) findViewById(R.id.btn_submit_sale);


        mStockSpinner = (Spinner) findViewById(R.id.spinner_stock);

        mNameEditText.setOnTouchListener(mTouchListener);
        mProviderEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mStockSpinner.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);

        setupSpinner();

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(EditorActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
            }
        });

        if(mCurrentProductUri != null) {
            mButtonSale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String quantityString = mQuantityEditText.getText().toString().trim();
                    int quantity = Integer.parseInt(quantityString);
                    ContentValues values = new ContentValues();

                    if(quantity > 0) {
                        quantity = quantity - 1;
                        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                        getContentResolver().update(mCurrentProductUri, values, null, null);

                    } else if(quantity == 0){
                        mStockSpinner.setSelection(ProductContract.ProductEntry.NOT_STOCK);
                        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK, ProductContract.ProductEntry.NOT_STOCK);
                        getContentResolver().update(mCurrentProductUri, values, null, null);
                        Toast.makeText(getBaseContext(), getString(R.string.editor_buy_product), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            mButtonSale.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_GALLERY) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(this, getString(R.string.error_permission_galery), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                mImageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupSpinner() {

        ArrayAdapter stockSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_stock_options,
                R.layout.support_simple_spinner_dropdown_item);

        stockSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        mStockSpinner.setAdapter(stockSpinnerAdapter);

        mStockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.product_yes_stock))) {
                        mStock = ProductContract.ProductEntry.YES_STOCK;
                    } else {
                        mStock = ProductContract.ProductEntry.NOT_STOCK;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mStock = ProductContract.ProductEntry.NOT_STOCK;
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            MenuItem menuItemOrder = menu.findItem(R.id.action_order);
            MenuItem menuItemSending = menu.findItem(R.id.action_sending);

            menuItemDelete.setVisible(false);
            menuItemOrder.setVisible(false);
            menuItemSending.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case R.id.action_order:
                Intent intent = new Intent(this, OrderEditorActivity.class);
                intent.setData(mCurrentProductUri);
                intent.setAction("order");
                startActivity(intent);
                return true;

            case R.id.action_sending:
                intent = new Intent(this, OrderEditorActivity.class);
                intent.setData(mCurrentProductUri);
                intent.setAction("sending");
                startActivity(intent);
                return true;

            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnSaveChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String providerString = mProviderEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        Bitmap imageBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();

        byte[] imageByte = getBytes(imageBitmap);

        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(providerString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                mStock == ProductContract.ProductEntry.NOT_STOCK) {
            return;
        }

        if (priceString.equals("") || nameString.equals("") || quantityString.equals("")) {
            Toast.makeText(this, getString(R.string.editor_insert_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PROVIDER, providerString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK, mStock);

        int quantity = 0;
        float price = 0.0f;
        if (!TextUtils.isEmpty(quantityString) || !TextUtils.isEmpty(priceString)) {
            quantity = Integer.parseInt(quantityString);
            price = Float.parseFloat(priceString);
        }

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageByte);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_success), Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_success), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_dialog_msg);

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsAffected = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showUnSaveChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.unsaved_changes_dialog_msg);

        builder.setPositiveButton(R.string.discard, discardButtonClickListener);

        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnSaveChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
            int providerColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PROVIDER);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int stockColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String provider = cursor.getString(providerColumnIndex);

            int quantity = cursor.getInt(quantityColumnIndex);
            int stock = cursor.getInt(stockColumnIndex);

            float price = cursor.getFloat(priceColumnIndex);

            byte[] image = cursor.getBlob(imageColumnIndex);

            mNameEditText.setText(name);
            mProviderEditText.setText(provider);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Float.toString(price));

            mImageView.setImageBitmap(getImage(image));

            switch (stock) {
                case ProductContract.ProductEntry.YES_STOCK:
                    mStockSpinner.setSelection(1);
                    break;
                default:
                    mStockSpinner.setSelection(0);
                    break;
            }
        }
    }

    // convert from bitmap to byte array
    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mProviderEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mImageView.setImageResource(R.mipmap.ic_launcher);
        mStockSpinner.setSelection(0);
    }
}