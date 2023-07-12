package fr.villot.codebarre.model;

public class PriceRecord {
    private int id;
    private double price;
    private int recordSheetId;
    private int storeId;
    private String productBarcode;

    public PriceRecord() {
    }

    public PriceRecord(String date, double price, int storeId, String productBarcode) {
        this.price = price;
        this.storeId = storeId;
        this.productBarcode = productBarcode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public int getRecordSheetId() {
        return recordSheetId;
    }

    public void setRecordSheetId(int recordSheetId) {
        this.recordSheetId = recordSheetId;
    }
}
