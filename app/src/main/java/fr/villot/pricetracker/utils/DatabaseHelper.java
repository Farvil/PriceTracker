package fr.villot.pricetracker.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.villot.pricetracker.model.PriceRecord;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.Store;

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
    private static final String KEY_STORE_LOGO = "store_logo";


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

    // Table des enseignes
    private static final String TABLE_BRANDS = "brands";
    private static final String BRAND_ID = "brand_id";
    private static final String BRAND_NAME = "brand_name";


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
                + KEY_STORE_LOCATION + " TEXT,"
                + KEY_STORE_LOGO + " TEXT" + ")";
        db.execSQL(createStoresTableQuery);

        String createRecordSheetsTableQuery = "CREATE TABLE " + TABLE_RECORD_SHEETS + " (" +
                KEY_RECORD_SHEET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_RECORD_SHEET_NAME + " TEXT," +
                KEY_RECORD_SHEET_DATE + " DATETIME," +
                KEY_RECORD_SHEET_STORE_ID + " INTEGER," +
                "FOREIGN KEY(" + KEY_RECORD_SHEET_STORE_ID + ") REFERENCES " + TABLE_STORES + "(" + KEY_STORE_ID + ")" +
        ")";
        db.execSQL(createRecordSheetsTableQuery);

        String createPriceRecordsTableQuery = "CREATE TABLE " + TABLE_PRICE_RECORDS + "("
                + KEY_PRICE_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PRICE_RECORD_PRICE + " NUMERIC(10, 2),"
                + KEY_PRICE_RECORD_RECORD_SHEET_ID + " INTEGER,"
                + KEY_PRICE_RECORD_PRODUCT_BARCODE + " TEXT,"
                + "FOREIGN KEY(" + KEY_PRICE_RECORD_RECORD_SHEET_ID + ") REFERENCES " + TABLE_RECORD_SHEETS + "(" + KEY_RECORD_SHEET_ID + "),"
                + "FOREIGN KEY(" + KEY_PRICE_RECORD_PRODUCT_BARCODE + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_PRODUCT_BARCODE + "),"
                + "UNIQUE (" + KEY_PRICE_RECORD_RECORD_SHEET_ID + ", " + KEY_PRICE_RECORD_PRODUCT_BARCODE + ")"  // Contrainte d'unicité
                + ")";
        db.execSQL(createPriceRecordsTableQuery);

        // Méthode pour créer la table BRANDS
        String createBrandTableQuery = "CREATE TABLE " + TABLE_BRANDS + "("
                + BRAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BRAND_NAME + " TEXT)";
        db.execSQL(createBrandTableQuery);

        insertBrands(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Suppression et recréation de la table lors d'une mise à jour de la base de données
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD_SHEETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANDS);
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

    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_NAME, product.getName());
        values.put(KEY_PRODUCT_BRAND, product.getBrand());
        values.put(KEY_PRODUCT_QUANTITY, product.getQuantity());
        values.put(KEY_PRODUCT_IMAGE_URL, product.getImageUrl());

        // Clause WHERE pour spécifier quel produit mettre à jour en fonction du code-barres
        String whereClause = KEY_PRODUCT_BARCODE + " = ?";
        String[] whereArgs = { product.getBarcode() };

        // Effectuer la mise à jour et obtenir le nombre de lignes affectées
        int rowsAffected = db.update(TABLE_PRODUCTS, values, whereClause, whereArgs);

        db.close();

        return rowsAffected;
    }


    public long addStore(Store store) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STORE_NAME, store.getName());
        values.put(KEY_STORE_LOCATION, store.getLocation());
        values.put(KEY_STORE_LOGO, store.getLogo());

        // Insertion du magasin dans la table "stores"
        long storeId = db.insert(TABLE_STORES, null, values);
        db.close();

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
        db.close();

        // Retourne l'ID de la liste de relevés de prix nouvellement insérée
        return recordSheetId;
    }

    public long addOrUpdatePriceRecord(PriceRecord priceRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRICE_RECORD_PRICE, priceRecord.getPrice());
        values.put(KEY_PRICE_RECORD_RECORD_SHEET_ID, priceRecord.getRecordSheetId());
        values.put(KEY_PRICE_RECORD_PRODUCT_BARCODE, priceRecord.getProductBarcode());

        // Ajoute ou met a jour le relevé de prix s'il existe déjà
        long priceRecordId = db.insertWithOnConflict(TABLE_PRICE_RECORDS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

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
        db.close();

        return productList;
    }


    @SuppressLint("Range")
    public List<Product> getProductsNotInRecordSheet(long recordSheetId) {
        List<Product> productList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Sélectionnez tous les produits qui ne sont pas liés à la recordsheet spécifiée
        String query = "SELECT * FROM " + TABLE_PRODUCTS +
                " WHERE " + KEY_PRODUCT_BARCODE + " NOT IN " +
                "(SELECT " + KEY_PRICE_RECORD_PRODUCT_BARCODE + " FROM " + TABLE_PRICE_RECORDS +
                " WHERE " + KEY_PRICE_RECORD_RECORD_SHEET_ID + " = ?)";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recordSheetId)});

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
        db.close();

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

                Store store = getStoreById(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_STORE_ID)));
                recordSheet.setStore(store);

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
                store.setLogo(cursor.getString(cursor.getColumnIndex(KEY_STORE_LOGO)));

                storeList.add(store);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return storeList;
    }

    @SuppressLint("Range")
    public List<Product> getProductsOnRecordSheet(long recordSheetId) {
        List<Product> products = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.*,  pr.* FROM " + TABLE_PRODUCTS + " p INNER JOIN " + TABLE_PRICE_RECORDS + " pr ON p." + KEY_PRODUCT_BARCODE + " = pr." + KEY_PRICE_RECORD_PRODUCT_BARCODE +
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
                product.setPrice(cursor.getDouble(cursor.getColumnIndex(KEY_PRICE_RECORD_PRICE)));
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
            Store store = getStoreById(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_STORE_ID)));
            recordSheet.setStore(store);
        }

        cursor.close();
        db.close();

        return recordSheet;
    }

    @SuppressLint("Range")
    public List<RecordSheet> getRecordSheetsOnProduct(String barcode) {
        List<RecordSheet> recordSheetsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Requête pour récupérer toutes les lignes de la table record_sheets associées à un produit donné
        String selectQuery = "SELECT rs.* FROM " + TABLE_RECORD_SHEETS + " rs INNER JOIN " + TABLE_PRICE_RECORDS + " pr ON rs." +
                KEY_RECORD_SHEET_ID + " = pr." + KEY_PRICE_RECORD_RECORD_SHEET_ID +
                " WHERE pr." + KEY_PRICE_RECORD_PRODUCT_BARCODE + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{barcode});

        // Parcours du curseur pour récupérer les enregistrements
        if (cursor.moveToFirst()) {
            do {
                RecordSheet recordSheet = new RecordSheet();
                recordSheet.setId(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_ID)));
                recordSheet.setName(cursor.getString(cursor.getColumnIndex(KEY_RECORD_SHEET_NAME)));
                long dateMillis = cursor.getLong(cursor.getColumnIndex(KEY_RECORD_SHEET_DATE));
                Date date = new Date(dateMillis);
                recordSheet.setDate(date);

                Store store = getStoreById(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_STORE_ID)));
                recordSheet.setStore(store);

                recordSheetsList.add(recordSheet);
            } while (cursor.moveToNext());
        }

        // Fermeture du curseur et de la base de données
        cursor.close();
        db.close();

        return recordSheetsList;
    }


    @SuppressLint("Range")
    public List<RecordSheet> getRecordSheetsOnStore(long storeId) {
        List<RecordSheet> recordSheets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_RECORD_SHEETS + " WHERE " + KEY_RECORD_SHEET_STORE_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(storeId)});

        if (cursor.moveToFirst()) {
            do {
                RecordSheet recordSheet = new RecordSheet();
                recordSheet.setId(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_ID)));
                recordSheet.setName(cursor.getString(cursor.getColumnIndex(KEY_RECORD_SHEET_NAME)));
                long dateMillis = cursor.getLong(cursor.getColumnIndex(KEY_RECORD_SHEET_DATE));
                Date date = new Date(dateMillis);
                recordSheet.setDate(date);
                Store store = getStoreById(cursor.getInt(cursor.getColumnIndex(KEY_RECORD_SHEET_STORE_ID)));
                recordSheet.setStore(store);

                recordSheets.add(recordSheet);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return recordSheets;
    }

    public boolean hasRecordSheetsOnStore(long storeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT COUNT(*) FROM " + TABLE_RECORD_SHEETS + " WHERE " + KEY_RECORD_SHEET_STORE_ID + " = ?";
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(storeId)});

        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            db.close();
            return count > 0;
        }

        db.close();
        return false;
    }

    public boolean hasPriceRecordsOnProduct(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT COUNT(*) FROM " + TABLE_PRICE_RECORDS + " WHERE " + KEY_PRICE_RECORD_PRODUCT_BARCODE + " = ?";
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(barcode)});

        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            db.close();
            return count > 0;
        }

        db.close();
        return false;
    }

    @SuppressLint("Range")
    public Store getStoreById(int storeId) {
        Store store = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_STORES + " WHERE " + KEY_STORE_ID + " = " + storeId;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            store = new Store();
            store.setId(cursor.getInt(cursor.getColumnIndex(KEY_STORE_ID)));
            store.setName(cursor.getString(cursor.getColumnIndex(KEY_STORE_NAME)));
            store.setLocation(cursor.getString(cursor.getColumnIndex(KEY_STORE_LOCATION)));
            store.setLogo(cursor.getString(cursor.getColumnIndex(KEY_STORE_LOGO)));
        }

        cursor.close();
        db.close();

        return store;
    }

    @SuppressLint("Range")
    public Product getProductFromBarCode(String barcode) {
        Product product = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + KEY_PRODUCT_BARCODE + " = " + barcode;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            product = new Product();
            product.setBarcode(barcode);
            product.setName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
            product.setBrand(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_BRAND)));
            product.setQuantity(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_QUANTITY)));
            product.setImageUrl(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_IMAGE_URL)));
        }

        cursor.close();
        db.close();

        return product;
    }

    public void deleteProduct(String barcode) {
        // Suppression uniquement s'il n'y a pas de priceRecords
        if (!hasPriceRecordsOnProduct(barcode)) {
            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(TABLE_PRODUCTS, KEY_PRODUCT_BARCODE + " = ?", new String[]{String.valueOf(barcode)});

            db.close();
        }
    }

    public void deleteStore(long storeId) {
        // Suppression uniquement s'il n'y a pas de recordsheets dépendantes de ce magasin
        if (!hasRecordSheetsOnStore(storeId)) {
            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(TABLE_STORES, KEY_STORE_ID + " = ?", new String[]{String.valueOf(storeId)});

            db.close();
        }
    }

    public void deleteRecordSheet(long recordSheetId) throws Exception {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Suppression des price records associés à la recordsheet
            db.delete(TABLE_PRICE_RECORDS, KEY_PRICE_RECORD_RECORD_SHEET_ID + " = ?", new String[]{String.valueOf(recordSheetId)});

            // Suppression de la recordsheet
            db.delete(TABLE_RECORD_SHEETS, KEY_RECORD_SHEET_ID + " = ?", new String[]{String.valueOf(recordSheetId)});

            // Validez la transaction si tout s'est bien passé
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Erreur deleteRecordSheet() : " + e.getMessage());
            throw new Exception(e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    public void deleteProductOnRecordSheet(String barcode, Long recordSheetId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Supprimer les enregistrements dans TABLE_PRICE_RECORDS
        db.delete(TABLE_PRICE_RECORDS,
                KEY_PRICE_RECORD_PRODUCT_BARCODE + " = ? AND " + KEY_PRICE_RECORD_RECORD_SHEET_ID + " = ?",
                new String[]{barcode, String.valueOf(recordSheetId)});

        db.close();
    }

    public void insertBrands(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // Marque Aldi
        values.put(BRAND_NAME, "aldi");
        db.insert(TABLE_BRANDS, null, values);

        // Marque Auchan
        values.clear(); // Effacer les valeurs précédentes
        values.put(BRAND_NAME, "auchan");
        db.insert(TABLE_BRANDS, null, values);

        // Marque Carrefour
        values.clear();
        values.put(BRAND_NAME, "carrefour");
        db.insert(TABLE_BRANDS, null, values);

        // Marque Casino
        values.clear();
        values.put(BRAND_NAME, "casino");
        db.insert(TABLE_BRANDS, null, values);

        // Marque G20
        values.clear();
        values.put(BRAND_NAME, "g20");
        db.insert(TABLE_BRANDS, null, values);

        // Marque Leclerc
        values.clear();
        values.put(BRAND_NAME, "leclerc");
        db.insert(TABLE_BRANDS, null, values);

        // Marque Lidl
        values.clear();
        values.put(BRAND_NAME, "lidl");
        db.insert(TABLE_BRANDS, null, values);

        // Marque Mousquetaires
        values.clear();
        values.put(BRAND_NAME, "mousquetaires");
        db.insert(TABLE_BRANDS, null, values);

        // Marque Systeme U
        values.clear();
        values.put(BRAND_NAME, "systeme_u");
        db.insert(TABLE_BRANDS, null, values);

    }

    @SuppressLint("Range")
    // Méthode pour récupérer les marques depuis la table BRANDS
    public List<String> getBrands() {
        List<String> brandsList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // La requête SQL pour récupérer les marques distinctes de la table BRANDS
        String query = "SELECT DISTINCT " + BRAND_NAME + " FROM " + TABLE_BRANDS;

        Cursor cursor = db.rawQuery(query, null);

        // Parcourir le curseur et ajouter les marques à la liste
        if (cursor.moveToFirst()) {
            do {
                String brandName = cursor.getString(cursor.getColumnIndex(BRAND_NAME));
                brandsList.add(brandName);
            } while (cursor.moveToNext());
        }

        // Fermer le curseur et la base de données
        cursor.close();
        db.close();

        return brandsList;
    }

    public int updateStore(Store store) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STORE_NAME, store.getName());
        values.put(KEY_STORE_LOCATION, store.getLocation());
        values.put(KEY_STORE_LOGO, store.getLogo());

        // Clause WHERE pour spécifier quel magasin mettre à jour en fonction de l'id
        String whereClause = KEY_STORE_ID + " = ?";
        String[] whereArgs = {Integer.toString(store.getId()) };

        // Effectuer la mise à jour et obtenir le nombre de lignes affectées
        int rowsAffected = db.update(TABLE_STORES, values, whereClause, whereArgs);

        db.close();

        return rowsAffected;
    }

}
