package fr.villot.codebarre.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.villot.codebarre.model.PriceRecord;
import fr.villot.codebarre.model.RecordSheet;
import fr.villot.codebarre.model.Product;
import fr.villot.codebarre.model.Store;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;
    private Context context;

    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 1;

    // Table des produits
    private static final String TABLE_PRODUCTS = "products";
    private static final String KEY_PRODUCT_BARCODE = "product_barcode";
    private static final String KEY_PRODUCT_NAME = "product_name";
    private static final String KEY_PRODUCT_BRAND = "product_brand";
    private static final String KEY_PRODUCT_QUANTITY = "product_quantity";
    private static final String KEY_PRODUCT_IMAGE_URL = "product_image_url";

    // Table des magasins
    private static final String TABLE_STORES = "stores";
    private static final String KEY_STORE_ID = "store_id";
    private static final String KEY_STORE_NAME = "store_name";
    private static final String KEY_STORE_LOCATION = "store_location";


    // Table pour les listes de relevés de prix
    private static final String TABLE_RECORD_SHEETS = "record_sheets";
    private static final String KEY_RECORD_SHEET_ID = "record_sheet_id";
    private static final String KEY_RECORD_SHEET_NAME = "record_sheet_name";
    private static final String KEY_RECORD_SHEET_DATE = "record_sheet_date";
    private static final String KEY_RECORD_SHEET_STORE_ID = "record_sheet_store_id";


    // Table des relevés de prix
    private static final String TABLE_PRICE_RECORDS = "price_records";
    private static final String KEY_PRICE_RECORD_ID = "price_record_id";
    private static final String KEY_PRICE_RECORD_PRICE = "price_record_price";

    private static final String KEY_PRICE_RECORD_RECORD_SHEET_ID = "price_record_record_sheet_id";
    private static final String KEY_PRICE_RECORD_PRODUCT_BARCODE = "price_record_product_barcode";

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    // Méthode statique pour récupérer l'instance unique de DatabaseHelper
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Création de la table des produits
        String createProductsTableQuery = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_PRODUCT_BARCODE + " TEXT PRIMARY KEY,"
                + KEY_PRODUCT_NAME + " TEXT,"
                + KEY_PRODUCT_BRAND + " TEXT,"
                + KEY_PRODUCT_QUANTITY + " TEXT,"
                + KEY_PRODUCT_IMAGE_URL + " TEXT" + ")";
        db.execSQL(createProductsTableQuery);

        // Création de la table des magasins
        String createStoresTableQuery = "CREATE TABLE " + TABLE_STORES + "("
                + KEY_STORE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_STORE_NAME + " TEXT,"
                + KEY_STORE_LOCATION + " TEXT" + ")";
        db.execSQL(createStoresTableQuery);

        String createRecordSheetsTable = "CREATE TABLE " + TABLE_RECORD_SHEETS + " (" +
                KEY_RECORD_SHEET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_RECORD_SHEET_NAME + " TEXT," +
                KEY_RECORD_SHEET_DATE + " DATETIME," +
                KEY_RECORD_SHEET_STORE_ID + " INTEGER," +
                "FOREIGN KEY(" + KEY_RECORD_SHEET_STORE_ID + ") REFERENCES " + TABLE_STORES + "(" + KEY_STORE_ID + ")" +
        ")";
        db.execSQL(createRecordSheetsTable);

        String createPriceRecordsTableQuery = "CREATE TABLE " + TABLE_PRICE_RECORDS + "("
                + KEY_PRICE_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PRICE_RECORD_PRICE + " REAL,"
                + KEY_PRICE_RECORD_RECORD_SHEET_ID + " INTEGER,"
                + KEY_PRICE_RECORD_PRODUCT_BARCODE + " TEXT,"
                + "FOREIGN KEY(" + KEY_PRICE_RECORD_RECORD_SHEET_ID + ") REFERENCES " + TABLE_RECORD_SHEETS + "(" + KEY_RECORD_SHEET_ID + "),"
                + "FOREIGN KEY(" + KEY_PRICE_RECORD_PRODUCT_BARCODE + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_PRODUCT_BARCODE + ")"
                + ")";
        db.execSQL(createPriceRecordsTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Suppression et recréation de la table lors d'une mise à jour de la base de données
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD_SHEETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_RECORDS);
        onCreate(db);
    }

    // -1 si update ou le row id sinon
    public long addOrUpdateProduct(Product product) {
        long result = -1;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_BARCODE, product.getBarcode());
        values.put(KEY_PRODUCT_NAME, product.getName());
        values.put(KEY_PRODUCT_BRAND, product.getBrand());
        values.put(KEY_PRODUCT_QUANTITY, product.getQuantity());
        values.put(KEY_PRODUCT_IMAGE_URL, product.getImageUrl());

        // Ajoute ou et a jour le produit s'il existe déjà.
        result = db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        return result;
    }

    public long addStore(Store store) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STORE_NAME, store.getName());
        values.put(KEY_STORE_LOCATION, store.getLocation());

        // Insertion du magasin dans la table "stores"
        long storeId = db.insert(TABLE_STORES, null, values);

        // Retourne l'ID du magasin nouvellement inséré
        return storeId;
    }

    public long addRecordSheet(RecordSheet recordSheet) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RECORD_SHEET_NAME, recordSheet.getName());
        // Insérer la date actuelle au format DATETIME
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long currentDate = System.currentTimeMillis();
        values.put(KEY_RECORD_SHEET_DATE, currentDate);

        values.put(KEY_RECORD_SHEET_STORE_ID,recordSheet.getStoreId());

        // Insertion de la liste de relevés de prix dans la table "price_records_lists"
        long recordSheetId = db.insert(TABLE_RECORD_SHEETS, null, values);

        // Retourne l'ID de la liste de relevés de prix nouvellement insérée
        return recordSheetId;
    }

    public long addPriceRecord(PriceRecord priceRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRICE_RECORD_PRICE, priceRecord.getPrice());
        values.put(KEY_PRICE_RECORD_RECORD_SHEET_ID, priceRecord.getRecordSheetId());
        values.put(KEY_PRICE_RECORD_PRODUCT_BARCODE, priceRecord.getProductBarcode());

        // Insertion du relevé de prix dans la table "price_records"
        long priceRecordId = db.insert(TABLE_PRICE_RECORDS, null, values);

        // Retourne l'ID du relevé de prix nouvellement inséré
        return priceRecordId;
    }

    @SuppressLint("Range")
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setBarcode(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_BARCODE)));
                product.setName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                product.setBrand(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_BRAND)));
                product.setQuantity(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_QUANTITY)));
                product.setImageUrl(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_IMAGE_URL)));
                productList.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return productList;
    }

    @SuppressLint("Range")
    public List<RecordSheet> getAllRecordSheets() {
        List<RecordSheet> recordSheetList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Requête pour récupérer toutes les lignes de la table record_sheets
        String selectQuery = "SELECT * FROM " + TABLE_RECORD_SHEETS;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // Parcours du curseur pour récupérer les enregistrements
        if (cursor.moveToFirst()) {
            do {
                RecordSheet recordSheet = new RecordSheet();
                recordSheet.setId(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_ID)));
                recordSheet.setName(cursor.getString(cursor.getColumnIndex(KEY_RECORD_SHEET_NAME)));
                long dateMillis = cursor.getLong(cursor.getColumnIndex(KEY_RECORD_SHEET_DATE));
                Date date = new Date(dateMillis);
                recordSheet.setDate(date);
                recordSheet.setStoreId(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_STORE_ID)));

                recordSheetList.add(recordSheet);
            } while (cursor.moveToNext());
        }

        // Fermeture du curseur et de la base de données
        cursor.close();
        db.close();

        return recordSheetList;
    }

    @SuppressLint("Range")
    public List<Store> getAllStores() {
        List<Store> storeList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STORES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Store store = new Store();
                store.setId(cursor.getInt(cursor.getColumnIndex(KEY_STORE_ID)));
                store.setName(cursor.getString(cursor.getColumnIndex(KEY_STORE_NAME)));
                store.setLocation(cursor.getString(cursor.getColumnIndex(KEY_STORE_LOCATION)));

                storeList.add(store);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return storeList;
    }

    @SuppressLint("Range")
    public List<Product> getProductsOnRecordSheet(long recordSheetId) {
        List<Product> products = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.* FROM " + TABLE_PRODUCTS + " p INNER JOIN " + TABLE_PRICE_RECORDS + " pr ON p." + KEY_PRODUCT_BARCODE + " = pr." + KEY_PRICE_RECORD_PRODUCT_BARCODE +
                " WHERE pr." + KEY_PRICE_RECORD_ID + " IN (SELECT " + KEY_PRICE_RECORD_ID + " FROM " + TABLE_PRICE_RECORDS + " WHERE " + KEY_PRICE_RECORD_RECORD_SHEET_ID + " = " + recordSheetId + ")";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setBarcode(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_BARCODE)));
                product.setName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                product.setBrand(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_BRAND)));
                product.setQuantity(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_QUANTITY)));
                product.setImageUrl(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_IMAGE_URL)));
                products.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return products;
    }

    @SuppressLint("Range")
    public RecordSheet getRecordSheetById(long recordSheetId) {
        RecordSheet recordSheet = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_RECORD_SHEETS + " WHERE " + KEY_RECORD_SHEET_ID + " = " + recordSheetId;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            recordSheet = new RecordSheet();
            recordSheet.setId(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_ID)));
            recordSheet.setName(cursor.getString(cursor.getColumnIndex(KEY_RECORD_SHEET_NAME)));
            long dateMillis = cursor.getLong(cursor.getColumnIndex(KEY_RECORD_SHEET_DATE));
            Date date = new Date(dateMillis);
            recordSheet.setDate(date);
            recordSheet.setStoreId(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_STORE_ID)));
        }

        cursor.close();
        db.close();

        return recordSheet;
    }

}
