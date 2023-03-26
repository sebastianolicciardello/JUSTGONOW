package com.sebastiano.licciardello.justgonowtoadventure;

import java.util.HashMap;

public class Item{

    private String name, description, image, information, mapy, key;
    private Integer price;
    private HashMap<String, Float> reviews;
    private HashMap<String, String> favorites;

    public Item() {}

    public Item(String name, String description, String image,
                String information, String mapy, Integer price, String key) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.information = information;
        this.mapy = mapy;
        this.price = price;
        this.key = key;
    }

    // getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMapy() {
        return mapy;
    }

    public void setMapy(String mapy) {
        this.mapy = mapy;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public HashMap<String, Float> getReviews() {
        return reviews;
    }

    public void setReviews(HashMap<String, Float> reviews) {
        this.reviews = reviews;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public HashMap<String, String> getFavorites() {
        return favorites;
    }

    public void setFavorites(HashMap<String, String> favorites) {
        this.favorites = favorites;
    }
}
