package fr.villot.pricetracker.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Product implements Serializable {
    private String barcode;
    private String name;
    private String brand;
    private String quantity;
    private String origin;
    private String imageUrl;
    private Double price;
    private Boolean originVerified;

    public Product(String barcode, String name, String brand, String quantity, String origin, String imageUrl, Boolean originVerified) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.origin = origin;
        this.imageUrl = imageUrl;
        this.originVerified = originVerified;
    }

    public Product() {
        this.originVerified = Boolean.FALSE;
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

    public String getFormattedPrice() {
        return price == null ? "" : String.format(Locale.FRANCE, "%.2f", price);
    }
    public void setPrice(Double price) { this.price = price; }

    public Boolean getOriginVerified() { return originVerified; }

    public String getFormattedOriginVerified() {
        String formatedOriginValue = "Non";
        if (originVerified != null && originVerified) {
            formatedOriginValue = "Oui";
        }
        return formatedOriginValue ;
    }
    public void setOriginVerified(Boolean verified) { this.originVerified = verified; }

}
