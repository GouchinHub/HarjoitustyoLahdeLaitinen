package com.example.harjoitustyo;

public class FoodEmissions {
    private final String dairy;
    private final String meat;
    private final String plant;
    private final String total;

    public FoodEmissions(String dairy, String meat, String plant, String total) {
        this.dairy = dairy;
        this.meat = meat;
        this.plant = plant;
        this.total = total;
    }

    public String getDairy() {
        return dairy;
    }

    public String getMeat() {
        return meat;
    }

    public String getPlant() {
        return plant;
    }

    public String getTotal() {
        return total;
    }
}
