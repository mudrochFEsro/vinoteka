-- Add image_url column to products
ALTER TABLE products ADD COLUMN image_url VARCHAR(500);

-- Update wines with placeholder images (using Unsplash wine images)
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400' WHERE id = 1;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1586370434639-0fe43b2d32e6?w=400' WHERE id = 2;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400' WHERE id = 3;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1553361371-9b22f78e8b1d?w=400' WHERE id = 4;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1516594915697-87eb3b1c14ea?w=400' WHERE id = 5;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1474722883778-792e7990302f?w=400' WHERE id = 6;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1558001373-7b93ee48ffa0?w=400' WHERE id = 7;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1558001373-7b93ee48ffa0?w=400' WHERE id = 8;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1584916201218-f4242ceb4809?w=400' WHERE id = 9;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1558001373-7b93ee48ffa0?w=400' WHERE id = 10;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1547595628-c61a29f496f0?w=400' WHERE id = 11;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1558001373-7b93ee48ffa0?w=400' WHERE id = 12;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1560148218-1a83060f7b32?w=400' WHERE id = 13;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400' WHERE id = 14;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1560512823-829485b8bf24?w=400' WHERE id = 15;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400' WHERE id = 16;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1571613316887-6f8d5cbf7ef7?w=400' WHERE id = 17;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1571613316887-6f8d5cbf7ef7?w=400' WHERE id = 18;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1594372365401-3b5ff14eaaed?w=400' WHERE id = 19;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1571613316887-6f8d5cbf7ef7?w=400' WHERE id = 20;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?w=400' WHERE id = 21;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1504279577054-acfeccf8fc52?w=400' WHERE id = 22;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1423483641154-5411ec9c0ddf?w=400' WHERE id = 23;
