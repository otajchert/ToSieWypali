SET @active_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'shipping_method'
      AND column_name = 'active'
);

SET @add_active_column = IF(
    @active_column_exists = 0,
    'ALTER TABLE shipping_method ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);

PREPARE add_active_column_statement FROM @add_active_column;
EXECUTE add_active_column_statement;
DEALLOCATE PREPARE add_active_column_statement;

UPDATE shipping_method
SET active = TRUE
WHERE active IS NULL;

ALTER TABLE shipping_method
    MODIFY COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE shipping_method
SET active = FALSE
WHERE UniqueID IN (
    '00000000-0000-0000-0000-000000000031',
    '00000000-0000-0000-0000-000000000032'
);

SELECT UniqueID, name, price, active
FROM shipping_method
ORDER BY name, UniqueID;
