package com.example.spareparts.config;

import com.example.spareparts.model.firestore.*;
import com.example.spareparts.repository.firestore.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Seeds initial data into Firestore for development/testing.
 * Only runs when 'seed' profile is active.
 */
@Component
@Profile("seed")
public class FirestoreDataSeeder implements CommandLineRunner {

    private final CategoryFirestoreRepository categoryRepository;
    private final BrandFirestoreRepository brandRepository;
    private final ProductFirestoreRepository productRepository;
    private final SupplierFirestoreRepository supplierRepository;

    public FirestoreDataSeeder(CategoryFirestoreRepository categoryRepository,
            BrandFirestoreRepository brandRepository,
            ProductFirestoreRepository productRepository,
            SupplierFirestoreRepository supplierRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Seeding Firestore with initial data...");

        // Seed categories
        seedCategories();

        // Seed brands
        seedBrands();

        // Seed suppliers
        seedSuppliers();

        // Seed products
        seedProducts();

        System.out.println("Firestore seeding complete!");
    }

    private void seedCategories() throws Exception {
        List<CategoryDocument> categories = Arrays.asList(
                createCategory("Engine Parts", "engine", "Engine components and parts", "bi-gear-fill", 1),
                createCategory("Brake System", "brake", "Brakes and brake accessories", "bi-disc-fill", 2),
                createCategory("Suspension", "suspension", "Suspension parts and shocks", "bi-arrow-up-down", 3),
                createCategory("Electrical", "electrical", "Electrical components and wiring", "bi-lightning-fill", 4),
                createCategory("Body Parts", "body", "Fairings, tanks, and body parts", "bi-car-front-fill", 5),
                createCategory("Exhaust", "exhaust", "Exhaust systems and mufflers", "bi-wind", 6),
                createCategory("Wheels & Tires", "wheels", "Wheels, rims, and tires", "bi-circle", 7),
                createCategory("Accessories", "accessories", "General accessories and add-ons", "bi-wrench", 8));

        for (CategoryDocument cat : categories) {
            categoryRepository.save(cat);
        }
        System.out.println("Categories seeded: " + categories.size());
    }

    private CategoryDocument createCategory(String name, String slug, String description, String icon, int order) {
        CategoryDocument cat = new CategoryDocument();
        cat.setName(name);
        cat.setSlug(slug);
        cat.setDescription(description);
        cat.setIcon(icon);
        cat.setDisplayOrder(order);
        cat.setActive(true);
        return cat;
    }

    private void seedBrands() throws Exception {
        List<BrandDocument> brands = Arrays.asList(
                createBrand("Honda", "Japan", Arrays.asList(
                        createModel("CBR600RR", 2003, 2024, "Inline-4", 599),
                        createModel("CBR1000RR", 2004, 2024, "Inline-4", 999),
                        createModel("CB650R", 2019, 2024, "Inline-4", 649),
                        createModel("CRF150L", 2017, 2024, "Single", 149))),
                createBrand("Yamaha", "Japan", Arrays.asList(
                        createModel("YZF-R6", 1999, 2024, "Inline-4", 599),
                        createModel("YZF-R1", 1998, 2024, "Inline-4", 998),
                        createModel("MT-07", 2014, 2024, "Parallel-Twin", 689),
                        createModel("MT-09", 2014, 2024, "Inline-3", 847))),
                createBrand("Kawasaki", "Japan", Arrays.asList(
                        createModel("Ninja ZX-6R", 1995, 2024, "Inline-4", 636),
                        createModel("Ninja ZX-10R", 2004, 2024, "Inline-4", 998),
                        createModel("Z900", 2017, 2024, "Inline-4", 948),
                        createModel("Versys 650", 2010, 2024, "Parallel-Twin", 649))),
                createBrand("Suzuki", "Japan", Arrays.asList(
                        createModel("GSX-R600", 1992, 2024, "Inline-4", 599),
                        createModel("GSX-R1000", 2001, 2024, "Inline-4", 999),
                        createModel("V-Strom 650", 2004, 2024, "V-Twin", 645))),
                createBrand("KTM", "Austria", Arrays.asList(
                        createModel("Duke 390", 2012, 2024, "Single", 373),
                        createModel("Duke 690", 2008, 2024, "Single", 690),
                        createModel("RC 390", 2014, 2024, "Single", 373))),
                createBrand("BMW", "Germany", Arrays.asList(
                        createModel("S1000RR", 2009, 2024, "Inline-4", 999),
                        createModel("R1250GS", 2019, 2024, "Boxer-Twin", 1254),
                        createModel("F850GS", 2018, 2024, "Parallel-Twin", 853))),
                createBrand("Ducati", "Italy", Arrays.asList(
                        createModel("Panigale V4", 2018, 2024, "V4", 1103),
                        createModel("Monster 821", 2014, 2024, "L-Twin", 821),
                        createModel("Scrambler", 2015, 2024, "L-Twin", 803))));

        for (BrandDocument brand : brands) {
            brandRepository.save(brand);
        }
        System.out.println("Brands seeded: " + brands.size());
    }

    private BrandDocument createBrand(String name, String country, List<BrandDocument.ModelInfo> models) {
        BrandDocument brand = new BrandDocument();
        brand.setName(name);
        brand.setCountry(country);
        brand.setModels(models);
        brand.setActive(true);
        return brand;
    }

    private BrandDocument.ModelInfo createModel(String name, int yearFrom, int yearTo, String engineType,
            int displacement) {
        BrandDocument.ModelInfo model = new BrandDocument.ModelInfo();
        model.setName(name);
        model.setSlug(name.toLowerCase().replace(" ", "-").replace("/", "-"));
        model.setYearFrom(yearFrom);
        model.setYearTo(yearTo);
        model.setEngineType(engineType);
        model.setDisplacement(displacement);
        return model;
    }

    private void seedSuppliers() throws Exception {
        List<SupplierDocument> suppliers = Arrays.asList(
                createSupplier("Tokyo Parts Co.", "supplier@tokyoparts.jp", "+81-3-1234-5678", "Takeshi Honda",
                        Arrays.asList("engine", "brake", "suspension"), 14),
                createSupplier("European Moto Supply", "info@euromoto.eu", "+49-30-9876-5432", "Hans Mueller",
                        Arrays.asList("body", "electrical", "exhaust"), 21),
                createSupplier("Global Bike Parts", "sales@globalbike.com", "+1-555-123-4567", "John Smith",
                        Arrays.asList("wheels", "accessories", "body"), 7));

        for (SupplierDocument supplier : suppliers) {
            supplierRepository.save(supplier);
        }
        System.out.println("Suppliers seeded: " + suppliers.size());
    }

    private SupplierDocument createSupplier(String name, String email, String phone, String contact,
            List<String> categories, int leadTime) {
        SupplierDocument supplier = new SupplierDocument();
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone(phone);
        supplier.setContactPerson(contact);
        supplier.setProductCategories(categories);
        supplier.setLeadTimeDays(leadTime);
        supplier.setRating(4.5);
        supplier.setActive(true);
        return supplier;
    }

    private void seedProducts() throws Exception {
        List<ProductDocument> products = Arrays.asList(
                // Engine Parts
                createProduct("High-Performance Piston Kit",
                        "Premium forged piston kit for enhanced compression and durability. Includes piston, rings, and wrist pin.",
                        249.99, 25, "engine", "Honda", Arrays.asList("CBR600RR", "CB650R"), "ENG-PST-001",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),
                createProduct("Racing Camshaft Set",
                        "High-lift performance camshafts for increased power output. Stage 2 profile.",
                        389.99, 12, "engine", "Yamaha", Arrays.asList("YZF-R6", "YZF-R1"), "ENG-CAM-002",
                        "https://images.unsplash.com/photo-1449426468159-d96dbf08f19f?w=400"),
                createProduct("Oil Filter Premium Pack (3)", "Pack of 3 high-quality oil filters with extended life.",
                        34.99, 150, "engine", null, null, "ENG-FLT-003",
                        "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400"),

                // Brake System
                createProduct("Brembo Brake Caliper Set",
                        "Premium 4-piston radial mount brake calipers. Superior stopping power.",
                        599.99, 8, "brake", null, null, "BRK-CAL-001",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),
                createProduct("EBC Sintered Brake Pads", "High-performance sintered brake pads for aggressive riding.",
                        79.99, 45, "brake", null, null, "BRK-PAD-002",
                        "https://images.unsplash.com/photo-1449426468159-d96dbf08f19f?w=400"),
                createProduct("Stainless Steel Brake Lines",
                        "Braided stainless steel brake lines for improved feel and response.",
                        129.99, 30, "brake", "Kawasaki", Arrays.asList("Ninja ZX-6R", "Z900"), "BRK-LIN-003",
                        "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400"),

                // Suspension
                createProduct("Ohlins TTX GP Rear Shock",
                        "Top-of-the-line racing suspension with adjustable compression and rebound.",
                        1899.99, 5, "suspension", null, null, "SUS-SHK-001",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),
                createProduct("Progressive Fork Springs", "Progressive rate fork springs for improved handling.",
                        149.99, 20, "suspension", "Suzuki", Arrays.asList("GSX-R600", "GSX-R1000"), "SUS-SPR-002",
                        "https://images.unsplash.com/photo-1449426468159-d96dbf08f19f?w=400"),

                // Electrical
                createProduct("LED Headlight Conversion Kit", "Full LED headlight upgrade with improved visibility.",
                        189.99, 35, "electrical", null, null, "ELE-LED-001",
                        "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400"),
                createProduct("Lithium Battery YTX14-BS", "Lightweight lithium-ion battery with 4x longer life.",
                        249.99, 18, "electrical", null, null, "ELE-BAT-002",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),
                createProduct("Quick Shifter Kit", "Electronic quick shifter for clutchless upshifts.",
                        299.99, 15, "electrical", "BMW", Arrays.asList("S1000RR"), "ELE-QSH-003",
                        "https://images.unsplash.com/photo-1449426468159-d96dbf08f19f?w=400"),

                // Body Parts
                createProduct("Carbon Fiber Tank Cover",
                        "Genuine carbon fiber tank cover with UV-protective clear coat.",
                        279.99, 10, "body", "Ducati", Arrays.asList("Panigale V4"), "BDY-TNK-001",
                        "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400"),
                createProduct("Racing Fairing Kit - OEM Fit", "Complete fairing kit with precision OEM fitment.",
                        699.99, 6, "body", "Honda", Arrays.asList("CBR1000RR"), "BDY-FAR-002",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),

                // Exhaust
                createProduct("Akrapovic Full Titanium Exhaust",
                        "Full titanium racing exhaust system with carbon end cap.",
                        2499.99, 3, "exhaust", "KTM", Arrays.asList("Duke 390", "RC 390"), "EXH-FUL-001",
                        "https://images.unsplash.com/photo-1449426468159-d96dbf08f19f?w=400"),
                createProduct("Yoshimura Alpha T Slip-On", "High-flow slip-on exhaust with titanium construction.",
                        649.99, 12, "exhaust", null, null, "EXH-SLP-002",
                        "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400"),

                // Accessories
                createProduct("Clip-On Handlebar Set", "Adjustable clip-on handlebars for sportbikes.",
                        159.99, 22, "accessories", null, null, "ACC-HND-001",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),
                createProduct("Tank Bag - Magnetic Mount", "Expandable tank bag with magnetic mounting system.",
                        89.99, 40, "accessories", null, null, "ACC-BAG-002",
                        "https://images.unsplash.com/photo-1449426468159-d96dbf08f19f?w=400"),
                createProduct("CNC Brake/Clutch Levers", "Precision CNC machined adjustable levers.",
                        79.99, 55, "accessories", null, null, "ACC-LEV-003",
                        "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400"));

        for (ProductDocument product : products) {
            productRepository.save(product);
        }
        System.out.println("Products seeded: " + products.size());
    }

    private ProductDocument createProduct(String name, String description, double price, int stock,
            String category, String brand, List<String> models,
            String sku, String imageUrl) {
        ProductDocument product = new ProductDocument();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setReorderLevel(5);
        product.setCategory(category);
        product.setBrand(brand);
        product.setCompatibleModels(models);
        product.setSku(sku);
        product.setImages(Arrays.asList(imageUrl));
        product.setAverageRating(0);
        product.setReviewCount(0);
        product.setActive(true);
        return product;
    }
}
