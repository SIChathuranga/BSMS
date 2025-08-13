package com.example.spareparts.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private String category; // category name as string
    private List<String> images;

    // Default constructor
    public ProductRequest() {}

    // Constructor
    public ProductRequest(String name, String description, BigDecimal price, int stock, String category, List<String> images) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.images = images;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
