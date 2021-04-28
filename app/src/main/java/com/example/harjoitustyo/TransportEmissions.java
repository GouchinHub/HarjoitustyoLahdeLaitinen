package com.example.harjoitustyo;

public class TransportEmissions {
    private final String flight;
    private final String train;
    private final String bus;
    private final String total;

    public TransportEmissions(String flight, String train, String bus, String total) {
        this.flight = flight;
        this.train = train;
        this.bus = bus;
        this.total = total;
    }

    public String getFlight() {
        return flight;
    }

    public String getTrain() {
        return train;
    }

    public String getBus() {
        return bus;
    }

    public String getTotal() { return total;}
}
