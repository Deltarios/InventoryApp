package com.deltarios.android.inventoryapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deltarios.android.inventoryapp.R;
import com.deltarios.android.inventoryapp.data.ProductContract;

import org.w3c.dom.Text;

/**
 * Created by Deltarios on 03/06/17.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text_view);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text_view);
        TextView inStockTextView = (TextView) view.findViewById(R.id.stock_text_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.product_image_list_view);

        String productNameText = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
        String productQuantityText = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        int productStockInt = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_STOCK));
        String productPriceText = "$" + cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        byte[] productImageView = cursor.getBlob(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE));

        Bitmap image = getImage(productImageView);

        Bitmap resizedImage = Bitmap.createScaledBitmap(image, 80, 80, true);

        nameTextView.setText(productNameText);
        priceTextView.setText(productPriceText);
        quantityTextView.setText(productQuantityText);
        imageView.setImageBitmap(resizedImage);

        switch (productStockInt) {
            case ProductContract.ProductEntry.YES_STOCK:
                inStockTextView.setText(R.string.product_yes_stock);
                break;
            default:
                inStockTextView.setText(R.string.product_not_stock);
                break;
        }
    }

    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
