package fr.villot.pricetracker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordSheet {

    private int id = -1;

    private String name;
    private Date date;
    private int storeId;

    private String logo;

    private List<PriceRecord> priceRecords;

    public RecordSheet() {
        priceRecords = new ArrayList<>();
    }

    public RecordSheet(String name, Date date, int storeId, String logo) {
        this.name = name;
        this.date = date;
        this.storeId = storeId;
        this.logo = logo;
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

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
