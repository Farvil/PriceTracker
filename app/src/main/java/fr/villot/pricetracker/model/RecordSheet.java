package fr.villot.pricetracker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void setPriceRecords(List<PriceRecord> priceRecords) {
        this.priceRecords = priceRecords;
    }

    public double getLastPrice() {
        if (priceRecords != null && !priceRecords.isEmpty()) {
            return priceRecords.get(priceRecords.size() - 1).getPrice(); // Dernier prix enregistré
        }
        return 0.0; // Aucun prix disponible
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
