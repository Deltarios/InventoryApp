package com.deltarios.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Deltarios on 03/06/17.
 */

public class ProductContract {

    private ProductContract() {}

    public static final String CONTENT_AUTHORITY = "com.deltarios.android.inventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    public static final class ProductEntry implements BaseColumns {

        /**
         *
         * The possible value of the stock of the product.
         */
        public static final int NOT_STOCK = 0;
        public static final int YES_STOCK = 1;

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * Name of the table date base for products
         */
        public static final String TABLE_NAME = "products";


        public static final String _ID = BaseColumns._ID;

        /**
         * Name for the product.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "name";

        /**
         * provider name of the product.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_PROVIDER = "provider";

        /**
         * stock of the product.
         * <p>
         * The only possible value are {@link #YES_STOCK}
         * or {@link #NOT_STOCK}
         * <p>
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_STOCK = "stock";

        /**
         * quantity of the product.
         * <p>
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * price of the product.
         * <p>
         * Type: REAL
         */
        public static final String COLUMN_PRODUCT_PRICE = "price";

        /**
         * price of the product.
         * <p>
         * Type: BLOB
         */
        public static final String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * Returns whether or not the given gender is {@link #YES_STOCK}
         * or {@link #NOT_STOCK}.
         */
        public static boolean isValidStock(int stock) {
            if(stock >= NOT_STOCK && stock <= YES_STOCK) {
                return true;
            }
            return false;
        }
    }
}
