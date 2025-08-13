-- Schema initialization
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    firebase_uid VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(12,2) NOT NULL,
    stock INT NOT NULL,
    category_id INT REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS product_images (
    product_id INT REFERENCES products(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    total NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

-- Seed data
INSERT INTO categories (name) VALUES
 ('Engine'), ('Brakes'), ('Tires'), ('Body'), ('Electrical')
ON CONFLICT DO NOTHING;

INSERT INTO users (firebase_uid, email, display_name, is_admin, created_at) VALUES
 ('test-admin-uid', 'admin@example.com', 'Admin User', TRUE, NOW()),
 ('test-user-uid', 'user@example.com', 'Customer User', FALSE, NOW())
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO products (name, description, price, stock, category_id) VALUES
 ('Spark Plug', 'High performance spark plug', 10.99, 50, (SELECT id FROM categories WHERE name='Engine')),
 ('Brake Pad', 'Durable brake pads', 25.50, 30, (SELECT id FROM categories WHERE name='Brakes')),
 ('Front Tire', 'All-weather front tire', 60.00, 15, (SELECT id FROM categories WHERE name='Tires')),
 ('Rear Tire', 'All-weather rear tire', 65.00, 20, (SELECT id FROM categories WHERE name='Tires')),
 ('Headlight', 'Bright LED headlight', 45.00, 25, (SELECT id FROM categories WHERE name='Electrical')),
 ('Mirror Set', 'Left and right mirrors', 20.00, 40, (SELECT id FROM categories WHERE name='Body')),
 ('Oil Filter', 'Premium oil filter', 8.99, 80, (SELECT id FROM categories WHERE name='Engine')),
 ('Chain', 'Durable bike chain', 35.00, 12, (SELECT id FROM categories WHERE name='Engine')),
 ('Battery', 'Long-life battery', 80.00, 10, (SELECT id FROM categories WHERE name='Electrical')),
 ('Seat Cover', 'Comfort seat cover', 15.00, 60, (SELECT id FROM categories WHERE name='Body'))
ON CONFLICT DO NOTHING;

-- Sample images (placeholders)
INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://via.placeholder.com/600x400?text=Product+' || id FROM products;

-- Sample orders
INSERT INTO orders (user_id, total, status, created_at) VALUES
 ((SELECT id FROM users WHERE firebase_uid='test-user-uid'), 36.98, 'PENDING', NOW()),
 ((SELECT id FROM users WHERE firebase_uid='test-user-uid'), 85.00, 'CONFIRMED', NOW()),
 ((SELECT id FROM users WHERE firebase_uid='test-user-uid'), 140.00, 'DELIVERED', NOW());

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
 (1, (SELECT id FROM products WHERE name='Spark Plug'), 2, 10.99),
 (2, (SELECT id FROM products WHERE name='Front Tire'), 1, 60.00),
 (2, (SELECT id FROM products WHERE name='Brake Pad'), 1, 25.00),
 (3, (SELECT id FROM products WHERE name='Rear Tire'), 2, 65.00);

-- Sample messages
INSERT INTO messages (user_id, content, created_at) VALUES
 ((SELECT id FROM users WHERE firebase_uid='test-user-uid'), 'When will my order ship?', NOW());
