package fr.villot.pricetracker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordSheet {

    private int id = -1;

    private String name;
    private Date date;
    private int storeId;
    private List<PriceRecord> priceRecords;

    public RecordSheet() {
        priceRecords = new ArrayList<>();
    }

    public RecordSheet(String name, Date date, int storeId) {
        this.name = name;
        this.date = date;
        this.storeId = storeId;
        priceRecords = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<PriceRecord> getPriceRecords() {
        return priceRecords;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addPriceRecord(PriceRecord priceRecord) {
        priceRecords.add(priceRecord);
    }

    public void removePriceRecord(PriceRecord priceRecord) {
        priceRecords.remove(priceRecord);
    }

    public int getStoreId() {
        return storeId;
    }

    public String getStoreLogo() {
        return "auchan";
    }


    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
}
