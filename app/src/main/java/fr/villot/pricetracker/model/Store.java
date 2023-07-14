package fr.villot.pricetracker.model;

public class Store {
    private int id;

    private String name;
    private String location;

    public Store(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public Store() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setId(int anInt) {
        id = anInt;
    }
}
