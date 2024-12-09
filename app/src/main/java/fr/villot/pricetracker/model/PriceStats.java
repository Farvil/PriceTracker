package fr.villot.pricetracker.model;

import java.util.Locale;

// Modifiez cette classe en conséquence
public class PriceStats {
    private final double minPrice;
    private final double maxPrice;
    private final double avgPrice;

    public PriceStats(double minPrice, double maxPrice, double avgPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.avgPrice = avgPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public String getMinPriceFormated() {
        return "Min : " + String.format(Locale.FRANCE,"%.2f", minPrice) + " €";
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public String getMaxPriceFormated() {
        return "Max : " + String.format(Locale.FRANCE,"%.2f", maxPrice) + " €";
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public String getAvgPriceFormated() {
        return "Moy : " + String.format(Locale.FRANCE,"%.2f", avgPrice) + " €";
    }
}