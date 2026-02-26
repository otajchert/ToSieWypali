-- sample categories
INSERT IGNORE INTO category (UniqueID, parent_category_id, category_name) VALUES
('00000000-0000-0000-0000-000000000001', NULL, 'Kubki'),
('00000000-0000-0000-0000-000000000002', NULL, 'Miski'),
('00000000-0000-0000-0000-000000000003', NULL, 'Wazony'),
('00000000-0000-0000-0000-000000000004', NULL, 'Dekoracyjne');

-- sample products (no category_id - categories assigned via product_category below)
INSERT IGNORE INTO product (UniqueID, name, description, photo, qty_in_stock, SKU, price, material, height, width, product_length) VALUES
('00000000-0000-0000-0000-000000001001', 'Kubek ceramiczny w pieski', 'Recznie robiony kubek z jasnej gliny z rysunkami pieskow.', NULL, 4, 'MUG-001', 65.00, 'Kamionka', '10cm', '8cm', '8cm'),
('00000000-0000-0000-0000-000000001002', 'Mydelniczka duza', 'Organiczna forma z zaglebieniem na mydlo', NULL, 8, 'DEC-001', 110.00, 'Kamionka', '5cm', '18cm', '13cm'),
('00000000-0000-0000-0000-000000001003', 'Miska do zupy', 'Sredniej wielkosci miska', NULL, 12, 'BWL-001', 55.00, 'Kamionka', '11cm', '16cm', '16cm'),
('00000000-0000-0000-0000-000000001004', 'Wazon Brzuszek', 'Duzy wazon - rzezba brzucha', NULL, 0, 'VAS-001', 120.00, 'Kamionka', '45cm', '25cm', '35cm');

-- product categories (many-to-many)
INSERT IGNORE INTO product_category (product_id, category_id) VALUES
('00000000-0000-0000-0000-000000001001', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000001002', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000001003', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000001004', '00000000-0000-0000-0000-000000000003');

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
