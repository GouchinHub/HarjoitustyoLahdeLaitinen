package com.example.harjoitustyo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Profile implements Serializable {
    //Profile has default attributes apart from name
    private String name;
    private double weight = 70;
    private double BMI = 21.60;
    private int height = 180;
    private int age = 30;
    private int points = 0;
    //LocalDate cant be stored to SharedPreferences in string format, so date parameter is a string
    private String birthday = "01.01.1991";
    private String email = "Email not set";
    private String pictureUri = null;
    private String residence = "Helsinki";

    public Profile(String username) {
        name = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    //Setter also calculates age from given birthday and current date
    public void setBirthday(LocalDate birthday) {
        LocalDate now = LocalDate.now();
        Period period = Period.between(birthday, now);
        age = period.getYears();
        this.birthday = birthday.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public double getBMI() {
        return BMI;
    }

    public void setBMI(double BMI) {
        this.BMI = BMI;
    }

    public void setPictureUri(String pictureUri) {
        this.pictureUri = pictureUri;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public int getHeight() {
        return height;
    }

    public int getAge() {
        return age;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getResidence() {
        return residence;
    }

    public String getPictureUri() {
        return pictureUri;
    }
}
