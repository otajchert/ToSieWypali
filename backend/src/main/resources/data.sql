-- sample order statuses
INSERT IGNORE INTO order_status (UniqueID, name) VALUES
('00000000-0000-0000-0000-000000000021', 'Zamówienie złożone'),
('00000000-0000-0000-0000-000000000022', 'W realizacji'),
('00000000-0000-0000-0000-000000000023', 'Wysłane'),
('00000000-0000-0000-0000-000000000024', 'Dostarczone'),
('00000000-0000-0000-0000-000000000025', 'Anulowane');

UPDATE order_status SET name = 'Zamówienie złożone' WHERE UniqueID = '00000000-0000-0000-0000-000000000021' AND name = 'Nowe';

-- sample shipping methods
INSERT IGNORE INTO shipping_method (UniqueID, name, price) VALUES
('00000000-0000-0000-0000-000000000031', 'Kurier DPD', 15.99),
('00000000-0000-0000-0000-000000000032', 'Poczta Polska', 12.50),
('00000000-0000-0000-0000-000000000033', 'Odbior osobisty', 0.00);
