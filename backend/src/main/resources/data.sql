-- sample order statuses
INSERT IGNORE INTO order_status (UniqueID, name) VALUES
('00000000-0000-0000-0000-000000000021', 'Zamówienie złożone'),
('00000000-0000-0000-0000-000000000022', 'W realizacji'),
('00000000-0000-0000-0000-000000000023', 'Wysłane'),
('00000000-0000-0000-0000-000000000024', 'Dostarczone'),
('00000000-0000-0000-0000-000000000025', 'Anulowane');

UPDATE order_status SET name = 'Zamówienie złożone' WHERE UniqueID = '00000000-0000-0000-0000-000000000021' AND name = 'Nowe';

-- default shipping method
INSERT IGNORE INTO shipping_method (UniqueID, name, price, active)
SELECT '00000000-0000-0000-0000-000000000033', 'Odbiór osobisty', 0.00, TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM shipping_method
    WHERE active = TRUE
);

UPDATE shipping_method
SET name = 'Odbiór osobisty'
WHERE UniqueID = '00000000-0000-0000-0000-000000000033'
AND name = 'Odbior osobisty';
