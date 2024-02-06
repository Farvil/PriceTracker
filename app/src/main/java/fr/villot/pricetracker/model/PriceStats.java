package fr.villot.pricetracker.model;

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

    public double getMaxPrice() {
        return maxPrice;
    }

    public double getAvgPrice() {
        return avgPrice;
    }
}