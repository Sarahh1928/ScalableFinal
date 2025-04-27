package com.ecommerce.ProductService.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("clothing")
public class Clothing extends Product {
    private String size;
    private String material;
    private String gender;
    private String season;

    public Clothing() {
        super();
    }

    // Clothing-specific setters
    public void setDetails(String size, String material, String gender, String season) {
        this.size = size;
        this.material = material;
        this.gender = gender;
        this.season = season;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}