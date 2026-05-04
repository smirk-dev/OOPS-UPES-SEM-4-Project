-- Expanded product catalog across all categories
-- vendor1 (Campus Fresh Mart) = GROCERY   vendor2 (Night Bites Cafe) = RESTAURANT

-- ──────────────────────────────────────────────────────────────────
-- CAMPUS FRESH MART — GROCERY
-- ──────────────────────────────────────────────────────────────────

-- ESSENTIALS
INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Tata Salt 1 kg', 'Iodised table salt', 'ESSENTIALS', 'GROCERY', 30.00, 27.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Maggi 2-Minute Noodles (4-pack)', 'Classic masala flavour', 'ESSENTIALS', 'GROCERY', 70.00, 65.00, 'IN_STOCK', TRUE, 5
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Britannia Brown Bread 400 g', 'Whole wheat sliced bread', 'ESSENTIALS', 'GROCERY', 50.00, 45.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Fortune Sona Masoori Rice 5 kg', 'Premium aged rice', 'ESSENTIALS', 'GROCERY', 295.00, 275.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Saffola Gold Oil 1 L', 'Blended edible oil', 'ESSENTIALS', 'GROCERY', 185.00, 169.00, 'IN_STOCK', TRUE, 8
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Kissan Mixed Fruit Jam 500 g', 'Fruity breakfast spread', 'ESSENTIALS', 'GROCERY', 125.00, 110.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Tata Tea Premium 250 g', 'Strong CTC black tea', 'ESSENTIALS', 'GROCERY', 130.00, 115.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Nescafé Classic 50 g', 'Instant coffee powder', 'ESSENTIALS', 'GROCERY', 165.00, 149.00, 'LOW_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

-- DAIRY
INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Amul Butter 100 g', 'Pasteurised salted butter', 'DAIRY', 'GROCERY', 60.00, 56.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Mother Dairy Paneer 200 g', 'Fresh cottage cheese', 'DAIRY', 'GROCERY', 95.00, 88.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Amul Ghee 500 ml', 'Pure cow ghee', 'DAIRY', 'GROCERY', 320.00, 295.00, 'IN_STOCK', TRUE, 5
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Britannia Cheese Slices 200 g', 'Processed cheddar slices', 'DAIRY', 'GROCERY', 140.00, 125.00, 'LOW_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Amul Curd 400 g', 'Set dahi — smooth & creamy', 'DAIRY', 'GROCERY', 42.00, 38.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

-- PRODUCE
INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Tomatoes 1 kg', 'Farm-fresh red tomatoes', 'PRODUCE', 'GROCERY', 45.00, 38.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Onions 1 kg', 'Red onions — medium size', 'PRODUCE', 'GROCERY', 38.00, 32.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Potatoes 2 kg', 'Washed white potatoes', 'PRODUCE', 'GROCERY', 50.00, 44.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Spinach / Palak Bunch', 'Tender green leaves', 'PRODUCE', 'GROCERY', 22.00, 18.00, 'LOW_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Banana Dozen', 'Ripe elaichi bananas', 'PRODUCE', 'GROCERY', 60.00, 52.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

-- SNACKS
INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Lay''s Classic Salted 52 g', 'Crispy potato chips', 'SNACKS', 'GROCERY', 30.00, 28.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Kurkure Masala Munch 92 g', 'Spicy corn puffs', 'SNACKS', 'GROCERY', 30.00, 28.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Haldiram''s Bhujia Sev 200 g', 'Traditional namkeen mix', 'SNACKS', 'GROCERY', 85.00, 75.00, 'IN_STOCK', TRUE, 10
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Oreo Original Cookies 300 g', 'Chocolate sandwich biscuits', 'SNACKS', 'GROCERY', 95.00, 82.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Parle-G Biscuits 800 g', 'Classic glucose biscuits', 'SNACKS', 'GROCERY', 72.00, 65.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor1';

-- ──────────────────────────────────────────────────────────────────
-- NIGHT BITES CAFE — RESTAURANT
-- ──────────────────────────────────────────────────────────────────

-- READY_TO_EAT
INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Masala Maggi Bowl', 'Spiced noodles with veggies', 'READY_TO_EAT', 'RESTAURANT', 80.00, 70.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Aloo Paratha × 2 with Butter', 'Stuffed wheat flatbread', 'READY_TO_EAT', 'RESTAURANT', 130.00, 115.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Paneer Kathi Roll', 'Grilled paneer wrap with sauce', 'READY_TO_EAT', 'RESTAURANT', 150.00, 130.00, 'IN_STOCK', TRUE, 5
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Chicken Biryani', 'Fragrant long-grain rice with chicken', 'READY_TO_EAT', 'RESTAURANT', 240.00, 210.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Dal Makhani with Steamed Rice', 'Slow-cooked black lentils', 'READY_TO_EAT', 'RESTAURANT', 180.00, 160.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Pav Bhaji', 'Spiced mashed veggies with toasted buns', 'READY_TO_EAT', 'RESTAURANT', 110.00, 95.00, 'LOW_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Egg Bhurji with Bread', 'Spiced scrambled eggs — 2 slices', 'READY_TO_EAT', 'RESTAURANT', 130.00, 115.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Paneer Fried Rice', 'Wok-tossed rice with cottage cheese', 'READY_TO_EAT', 'RESTAURANT', 160.00, 140.00, 'IN_STOCK', TRUE, 8
FROM users WHERE username = 'vendor2';

-- SNACKS
INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'French Fries Regular', 'Crispy salted fries with ketchup', 'SNACKS', 'RESTAURANT', 85.00, 75.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Samosa × 2', 'Crispy fried potato samosas', 'SNACKS', 'RESTAURANT', 30.00, 25.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Bread Pakora × 4', 'Deep-fried stuffed bread slices', 'SNACKS', 'RESTAURANT', 55.00, 48.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Veg Spring Rolls × 3', 'Crispy rolls with mixed veg filling', 'SNACKS', 'RESTAURANT', 95.00, 82.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

-- BEVERAGES
INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Masala Chai', 'Cardamom & ginger spiced tea', 'BEVERAGES', 'RESTAURANT', 22.00, 18.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Fresh Lime Soda', 'Sweet / salt — your choice', 'BEVERAGES', 'RESTAURANT', 65.00, 55.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Mango Lassi', 'Thick Alphonso mango yogurt drink', 'BEVERAGES', 'RESTAURANT', 90.00, 78.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Chocolate Milkshake', 'Rich cocoa milk blended cold', 'BEVERAGES', 'RESTAURANT', 125.00, 108.00, 'IN_STOCK', TRUE, 12
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Fresh Orange Juice 300 ml', 'Cold-pressed, no added sugar', 'BEVERAGES', 'RESTAURANT', 95.00, 82.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';

INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
SELECT id, 'Sugarcane Juice 350 ml', 'Fresh-pressed with ginger & lemon', 'BEVERAGES', 'RESTAURANT', 45.00, 38.00, 'IN_STOCK', TRUE, 0
FROM users WHERE username = 'vendor2';
