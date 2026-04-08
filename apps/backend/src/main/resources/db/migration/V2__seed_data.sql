INSERT INTO users (username, password_hash, role, full_name, email, phone)
VALUES
    ('student1', '$2a$10$VLz4hQ2Np8GCWi31nfQh0e5S4UDVQhQ2xJ2j4HC3f.3S2jTH1Mq8S', 'STUDENT', 'Sample Student', 'student1@upes.ac.in', '9999991001'),
    ('student2', '$2a$10$VLz4hQ2Np8GCWi31nfQh0e5S4UDVQhQ2xJ2j4HC3f.3S2jTH1Mq8S', 'STUDENT', 'Sample Student 2', 'student2@upes.ac.in', '9999991002'),
    ('vendor1', '$2a$10$VLz4hQ2Np8GCWi31nfQh0e5S4UDVQhQ2xJ2j4HC3f.3S2jTH1Mq8S', 'VENDOR', 'Campus Fresh Vendor', 'vendor1@upes.ac.in', '9999992001'),
    ('vendor2', '$2a$10$VLz4hQ2Np8GCWi31nfQh0e5S4UDVQhQ2xJ2j4HC3f.3S2jTH1Mq8S', 'VENDOR', 'Night Bites Vendor', 'vendor2@upes.ac.in', '9999992002'),
    ('admin1', '$2a$10$VLz4hQ2Np8GCWi31nfQh0e5S4UDVQhQ2xJ2j4HC3f.3S2jTH1Mq8S', 'ADMIN', 'System Admin', 'admin1@upes.ac.in', '9999993001');

INSERT INTO delivery_zones (code, name)
VALUES
    ('BIDHOLI-A', 'Bidholi Hostel A Block'),
    ('BIDHOLI-B', 'Bidholi Hostel B Block'),
    ('KANDOLI-C', 'Kandoli C Block');

INSERT INTO vendor_profiles (user_id, shop_name, vertical)
VALUES
    ((SELECT id FROM users WHERE username = 'vendor1'), 'Campus Fresh Mart', 'GROCERY'),
    ((SELECT id FROM users WHERE username = 'vendor2'), 'Night Bites Cafe', 'RESTAURANT');

INSERT INTO wallets (user_id, current_balance)
VALUES
    ((SELECT id FROM users WHERE username = 'student1'), 250.00),
    ((SELECT id FROM users WHERE username = 'student2'), 80.00);

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
VALUES
    ((SELECT id FROM users WHERE username = 'vendor1'), 'Aashirvaad Atta 5kg', 'Whole wheat flour', 'ESSENTIALS', 'GROCERY', 320.00, 289.00, 'IN_STOCK', TRUE, 0),
    ((SELECT id FROM users WHERE username = 'vendor1'), 'Amul Toned Milk 1L', 'Fresh milk packet', 'DAIRY', 'GROCERY', 62.00, 58.00, 'LOW_STOCK', TRUE, 5),
    ((SELECT id FROM users WHERE username = 'vendor2'), 'Veg Sandwich', 'Grilled sandwich', 'READY_TO_EAT', 'RESTAURANT', 120.00, 99.00, 'IN_STOCK', TRUE, 0),
    ((SELECT id FROM users WHERE username = 'vendor2'), 'Cold Coffee', 'Chilled beverage', 'BEVERAGES', 'RESTAURANT', 110.00, 95.00, 'IN_STOCK', TRUE, 10);
