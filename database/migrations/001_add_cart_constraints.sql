SELECT client_id, COUNT(*) AS cart_count
FROM shopping_cart
GROUP BY client_id
HAVING COUNT(*) > 1;

SELECT cart_id, product_id, COUNT(*) AS line_count, SUM(qty) AS combined_qty
FROM cart_item
GROUP BY cart_id, product_id
HAVING COUNT(*) > 1;

SELECT UniqueID, cart_id, product_id, qty
FROM cart_item
WHERE qty <= 0;

-- after backup:

ALTER TABLE shopping_cart
    ADD CONSTRAINT uk_shopping_cart_client UNIQUE (client_id);

ALTER TABLE cart_item
    ADD CONSTRAINT uk_cart_item_cart_product UNIQUE (cart_id, product_id),
    ADD CONSTRAINT chk_cart_item_qty_positive CHECK (qty > 0);
