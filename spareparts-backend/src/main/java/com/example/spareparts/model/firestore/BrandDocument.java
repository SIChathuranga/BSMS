package com.example.spareparts.model.firestore;

import com.google.cloud.firestore.annotation.DocumentId;
import java.util.List;

/**
 * Brand document for Firestore.
 * Stores motorcycle brand and model compatibility information.
 */
public class BrandDocument {

    @DocumentId
    private String id;
    private String name;
    private String logoUrl;
    private String country;
    private List<ModelInfo> models;
    private boolean active;
    private long createdAt;
    private long updatedAt;

    public BrandDocument() {
    }

    // Nested Model Info
    public static class ModelInfo {
        private String name;
        private String slug;
        private int yearFrom;
        private int yearTo;
        private String engineType;
        private int displacement; // CC

        public ModelInfo() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public int getYearFrom() {
            return yearFrom;
        }

        public void setYearFrom(int yearFrom) {
            this.yearFrom = yearFrom;
        }

        public int getYearTo() {
            return yearTo;
        }

        public void setYearTo(int yearTo) {
            this.yearTo = yearTo;
        }

        public String getEngineType() {
            return engineType;
        }

        public void setEngineType(String engineType) {
            this.engineType = engineType;
        }

        public int getDisplacement() {
            return displacement;
        }

        public void setDisplacement(int displacement) {
            this.displacement = displacement;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<ModelInfo> getModels() {
        return models;
    }

    public void setModels(List<ModelInfo> models) {
        this.models = models;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
