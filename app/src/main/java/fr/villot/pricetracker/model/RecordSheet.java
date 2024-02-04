package fr.villot.pricetracker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheet {

    private int id = -1;

    private String name;
    private Date date;

    private Store store;

    private List<PriceRecord> priceRecords;

    public RecordSheet() {
        priceRecords = new ArrayList<>();
    }

    public RecordSheet(String name, Date date, Store store) {
        this.name = name;
        this.date = date;
        this.store = store;
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

    public int getId() {
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
        return store.getId();
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Store getStore() {
        return store;
    }

}
