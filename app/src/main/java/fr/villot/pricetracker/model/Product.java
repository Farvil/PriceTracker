package fr.villot.pricetracker.model;

import java.io.Serializable;

public class Product implements Serializable {
    private String barcode;
    private String name;
    private String brand;
    private String quantity;
    private String origin;
    private String imageUrl;
    private Double price;

    public Product(String barcode, String name, String brand, String quantity, String origin, String imageUrl) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.origin = origin;
        this.imageUrl = imageUrl;
    }

    public Product() {

    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
