-- sample categories
INSERT IGNORE INTO category (UniqueID, parent_category_id, category_name) VALUES
('00000000-0000-0000-0000-000000000001', NULL, 'Kubki'),
('00000000-0000-0000-0000-000000000002', NULL, 'Miski'),
('00000000-0000-0000-0000-000000000003', NULL, 'Wazony'),
('00000000-0000-0000-0000-000000000004', NULL, 'Dekoracje');

-- sample products (no category_id - categories assigned via product_category below)
INSERT IGNORE INTO product (UniqueID, name, description, photo, qty_in_stock, SKU, price, material, height, width, product_length) VALUES
('00000000-0000-0000-0000-000000001001', 'Kubek ceramiczny w pieski', 'Recznie robiony kubek z jasnej gliny z rysunkami pieskow.', 'https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-16%20151806.png', 4, 'MUG-001', 65.00, 'Kamionka', '10cm', '8cm', '8cm'),
('00000000-0000-0000-0000-000000001002', 'Mydelniczka duża', 'Organiczna forma z zaglebieniem na mydlo', 'https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-14%20215009.png', 8, 'DEC-001', 110.00, 'Kamionka', '5cm', '18cm', '13cm'),
('00000000-0000-0000-0000-000000001003', 'Miska do zupy', 'Sredniej wielkosci miska', NULL, 12, 'BWL-001', 55.00, 'Kamionka', '11cm', '16cm', '16cm'),
('00000000-0000-0000-0000-000000001004', 'Wazon Brzuszek', 'Duzy wazon - rzezba brzucha', NULL, 0, 'VAS-001', 120.00, 'Kamionka', '45cm', '25cm', '35cm');

-- product categories (many-to-many)
INSERT IGNORE INTO product_category (product_id, category_id) VALUES
('00000000-0000-0000-0000-000000001001', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000001002', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000001003', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000001004', '00000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000001004', '00000000-0000-0000-0000-000000000004');

-- product gallery images
INSERT IGNORE INTO product_image (UniqueID, product_id, image_url, sort_order) VALUES
('00000000-0000-0000-0000-000000002001', '00000000-0000-0000-0000-000000001001', 'https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-14%20215052.png', 0),
('00000000-0000-0000-0000-000000002002', '00000000-0000-0000-0000-000000001002', 'https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-16%20151924.png', 0);


-- sample order statuses
INSERT IGNORE INTO order_status (UniqueID, name) VALUES
('00000000-0000-0000-0000-000000000021', 'Nowe'),
('00000000-0000-0000-0000-000000000022', 'W realizacji'),
('00000000-0000-0000-0000-000000000023', 'Wyslane'),
('00000000-0000-0000-0000-000000000024', 'Dostarczone'),
('00000000-0000-0000-0000-000000000025', 'Anulowane');

-- sample shipping methods
INSERT IGNORE INTO shipping_method (UniqueID, name, price) VALUES
('00000000-0000-0000-0000-000000000031', 'Kurier DPD', 15.99),
('00000000-0000-0000-0000-000000000032', 'Poczta Polska', 12.50),
('00000000-0000-0000-0000-000000000033', 'Odbior osobisty', 0.00);
