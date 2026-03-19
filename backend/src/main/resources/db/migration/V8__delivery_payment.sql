-- Delivery and payment options

-- Delivery method: PACKETA_PICKUP (výdajné miesto), PACKETA_COURIER (kuriér cez Packeta), COURIER (vlastný kuriér)
ALTER TABLE orders ADD COLUMN delivery_method VARCHAR(30) DEFAULT 'COURIER';

-- Packeta pickup point ID (for PACKETA_PICKUP)
ALTER TABLE orders ADD COLUMN packeta_point_id VARCHAR(20);
ALTER TABLE orders ADD COLUMN packeta_point_name VARCHAR(255);

-- Delivery price
ALTER TABLE orders ADD COLUMN delivery_price DECIMAL(10, 2) DEFAULT 0;

-- Payment method: CASH_ON_DELIVERY (dobierka), CARD_ON_DELIVERY (kartou kurierovi), IN_APP (v appke - neskôr)
ALTER TABLE orders ADD COLUMN payment_method VARCHAR(30) DEFAULT 'CASH_ON_DELIVERY';
