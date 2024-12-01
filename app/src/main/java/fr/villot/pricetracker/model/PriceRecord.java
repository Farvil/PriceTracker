package fr.villot.pricetracker.model;

public class PriceRecord {
    private int id;
    private double price;
    private long recordSheetId;
    private String productBarcode;

    public PriceRecord() {
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

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public long getRecordSheetId() {
        return recordSheetId;
    }

    public void setRecordSheetId(long recordSheetId) {
        this.recordSheetId = recordSheetId;
    }
}
