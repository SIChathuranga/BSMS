package com.example.spareparts.config;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.example.spareparts.model.Category;
import com.example.spareparts.model.Product;
import com.example.spareparts.repository.CategoryRepository;
import com.example.spareparts.service.ProductService;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {
    
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    
    public DataInitializer(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }
    
    @PostConstruct
    public void initializeData() {
        // Only initialize if database is empty
        if (productService.getAll().isEmpty()) {
            createCategories();
            createSampleProducts();
        }
    }
    
    private void createCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "engine"));
            categoryRepository.save(new Category(null, "brake"));
            categoryRepository.save(new Category(null, "suspension"));
            categoryRepository.save(new Category(null, "electrical"));
            categoryRepository.save(new Category(null, "body"));
        }
    }
    
    private void createSampleProducts() {
        Category engineCategory = categoryRepository.findByName("engine").orElse(null);
        Category brakeCategory = categoryRepository.findByName("brake").orElse(null);
        Category suspensionCategory = categoryRepository.findByName("suspension").orElse(null);
        Category electricalCategory = categoryRepository.findByName("electrical").orElse(null);
        Category bodyCategory = categoryRepository.findByName("body").orElse(null);
        
        // Engine Parts
        Product engineOil = new Product();
        engineOil.setName("High Performance Engine Oil");
        engineOil.setCategory(engineCategory);
        engineOil.setPrice(new BigDecimal("45.99"));
        engineOil.setStock(50);
        engineOil.setDescription("Premium synthetic engine oil for high-performance motorcycles");
        engineOil.setImages(Arrays.asList("https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"));
        productService.save(engineOil);
        
        Product airFilter = new Product();
        airFilter.setName("Racing Air Filter");
        airFilter.setCategory(engineCategory);
        airFilter.setPrice(new BigDecimal("29.99"));
        airFilter.setStock(25);
        airFilter.setDescription("High-flow air filter for improved engine performance");
        airFilter.setImages(Arrays.asList("https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=400"));
        productService.save(airFilter);
        
        // Brake System
        Product brakePads = new Product();
        brakePads.setName("Ceramic Brake Pads");
        brakePads.setCategory(brakeCategory);
        brakePads.setPrice(new BigDecimal("79.99"));
        brakePads.setStock(30);
        brakePads.setDescription("High-performance ceramic brake pads for superior stopping power");
        brakePads.setImages(Arrays.asList("https://images.unsplash.com/photo-1609034227675-6c4123dd4e01?w=400"));
        productService.save(brakePads);
        
        Product brakeFluid = new Product();
        brakeFluid.setName("DOT 4 Brake Fluid");
        brakeFluid.setCategory(brakeCategory);
        brakeFluid.setPrice(new BigDecimal("12.99"));
        brakeFluid.setStock(3); // Low stock
        brakeFluid.setDescription("High-quality DOT 4 brake fluid for motorcycle brake systems");
        brakeFluid.setImages(Arrays.asList("https://images.unsplash.com/photo-1612796196802-e0be5b78a0fb?w=400"));
        productService.save(brakeFluid);
        
        // Suspension
        Product shockAbsorber = new Product();
        shockAbsorber.setName("Adjustable Rear Shock");
        shockAbsorber.setCategory(suspensionCategory);
        shockAbsorber.setPrice(new BigDecimal("299.99"));
        shockAbsorber.setStock(15);
        shockAbsorber.setDescription("Premium adjustable rear shock absorber for sport bikes");
        shockAbsorber.setImages(Arrays.asList("https://images.unsplash.com/photo-1558618047-fe98c3eb8b30?w=400"));
        productService.save(shockAbsorber);
        
        // Electrical
        Product sparkPlug = new Product();
        sparkPlug.setName("Iridium Spark Plugs");
        sparkPlug.setCategory(electricalCategory);
        sparkPlug.setPrice(new BigDecimal("18.99"));
        sparkPlug.setStock(40);
        sparkPlug.setDescription("Long-lasting iridium spark plugs for optimal ignition");
        sparkPlug.setImages(Arrays.asList("https://images.unsplash.com/photo-1558146862-1c93b85dd43b?w=400"));
        productService.save(sparkPlug);
        
        Product ledHeadlight = new Product();
        ledHeadlight.setName("LED Headlight Assembly");
        ledHeadlight.setCategory(electricalCategory);
        ledHeadlight.setPrice(new BigDecimal("159.99"));
        ledHeadlight.setStock(0); // Out of stock
        ledHeadlight.setDescription("Bright LED headlight assembly for improved visibility");
        ledHeadlight.setImages(Arrays.asList("https://images.unsplash.com/photo-1588593821210-d0b6bb2e1329?w=400"));
        productService.save(ledHeadlight);
        
        // Body Parts
        Product windscreen = new Product();
        windscreen.setName("Sport Windscreen");
        windscreen.setCategory(bodyCategory);
        windscreen.setPrice(new BigDecimal("89.99"));
        windscreen.setStock(20);
        windscreen.setDescription("Aerodynamic sport windscreen for reduced wind resistance");
        windscreen.setImages(Arrays.asList("https://images.unsplash.com/photo-1558618047-c3cf8e01dbf1?w=400"));
        productService.save(windscreen);
        
        Product rearFender = new Product();
        rearFender.setName("Carbon Fiber Rear Fender");
        rearFender.setCategory(bodyCategory);
        rearFender.setPrice(new BigDecimal("199.99"));
        rearFender.setStock(8);
        rearFender.setDescription("Lightweight carbon fiber rear fender for weight reduction");
        rearFender.setImages(Arrays.asList("https://images.unsplash.com/photo-1558618059-be7e3e4b9e92?w=400"));
        productService.save(rearFender);
        
        System.out.println("✅ Sample products initialized successfully!");
    }
}
