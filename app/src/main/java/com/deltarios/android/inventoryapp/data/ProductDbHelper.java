package com.deltarios.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.deltarios.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Deltarios on 03/06/17.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    private static final String DATEBASE_NAME = "shelter.db";

    private static final int DATEBASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String AUTOINCREMENT = " AUTOINCREMENT";
    private static final String COMMA_SEP = ",";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String NOT_NULL = " NOT NULL";
    private static final String BLOB_TYPE = " BLOB";

    private static final int DEFAULT_PRICE_VALUE = 0;

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
            ProductEntry._ID + " INTEGER PRIMARY KEY" + AUTOINCREMENT + COMMA_SEP +
            ProductEntry.COLUMN_PRODUCT_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            ProductEntry.COLUMN_PRODUCT_PROVIDER + TEXT_TYPE + COMMA_SEP +
            ProductEntry.COLUMN_PRODUCT_QUANTITY + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
            ProductEntry.COLUMN_PRODUCT_STOCK + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
            ProductEntry.COLUMN_PRODUCT_PRICE + REAL_TYPE + NOT_NULL + " DEFAULT " + DEFAULT_PRICE_VALUE + COMMA_SEP +
            ProductEntry.COLUMN_PRODUCT_IMAGE + BLOB_TYPE + NOT_NULL + " );";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    public ProductDbHelper(Context context) {
        super(context, DATEBASE_NAME, null, DATEBASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
