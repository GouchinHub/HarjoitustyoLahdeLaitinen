package com.example.harjoitustyo;

public class THLData {
    private final String title;
    private final int ageScaleID;
    private final String location;
    private final int locationID;
    private final String group;
    private final String percentage;

    public THLData(String title, int ageScaleID, String location, int locationID, String group, String percentage) {
        this.title = title;
        this.ageScaleID = ageScaleID;
        this.location = location;
        this.locationID = locationID;
        this.group = group;
        this.percentage = percentage;
    }

    public String getTitle() {
        return title;
    }

    public int getAgeScaleID() {
        return ageScaleID;
    }

    public String getLocation() {
        return location;
    }

    public int getLocationID() {
        return locationID;
    }

    public String getGroup() {
        return group;
    }

    public String getPercentage() {
        return percentage;
    }

}
