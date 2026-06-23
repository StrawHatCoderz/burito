-- Addresses for seed restaurants
INSERT INTO address (address_id, street, city, state, country, zipcode) VALUES
  (1,  '12 Brigade Road',         'Bangalore',   'Karnataka',   'India', '560025'),
  (2,  '5 Linking Road',          'Mumbai',      'Maharashtra', 'India', '400050'),
  (3,  '88 Connaught Place',      'New Delhi',   'Delhi',       'India', '110001'),
  (4,  '23 Banjara Hills',        'Hyderabad',   'Telangana',   'India', '500034'),
  (5,  '7 Anna Salai',            'Chennai',     'Tamil Nadu',  'India', '600002'),
  (6,  '14 FC Road',              'Pune',        'Maharashtra', 'India', '411004'),
  (7,  '9 Park Street',           'Kolkata',     'West Bengal', 'India', '700016'),
  (8,  '31 CG Road',              'Ahmedabad',   'Gujarat',     'India', '380009'),
  (9,  '55 MG Road',              'Bangalore',   'Karnataka',   'India', '560001'),
  (10, '18 Juhu Beach Road',      'Mumbai',      'Maharashtra', 'India', '400049'),
  (11, '3 Sector 17',             'Chandigarh',  'Punjab',      'India', '160017'),
  (12, '77 Church Street',        'Bangalore',   'Karnataka',   'India', '560001'),
  (13, '41 Residency Road',       'Bangalore',   'Karnataka',   'India', '560025'),
  (14, '6 Whitefield Main Road',  'Bangalore',   'Karnataka',   'India', '560066'),
  (15, '19 Indiranagar 100ft Rd', 'Bangalore',   'Karnataka',   'India', '560038');

SELECT setval('address_address_id_seq', 15);

-- Seed restaurants across cuisines
INSERT INTO restaurant (restaurant_id, restaurant_name, description, cuisine_type, rating, est_delivery_minutes, is_open, created_at, address_id) VALUES
  (gen_random_uuid(), 'Spice Garden',    'Authentic North Indian curries and breads',          'INDIAN',        4.5, 30, true,  CURRENT_TIMESTAMP, 1),
  (gen_random_uuid(), 'Dosa Palace',     'Crispy South Indian dosas and idlis',                'SOUTH_INDIAN',  4.3, 25, true,  CURRENT_TIMESTAMP, 2),
  (gen_random_uuid(), 'Biryani House',   'Slow-cooked Hyderabadi dum biryani',                 'INDIAN',        4.7, 40, true,  CURRENT_TIMESTAMP, 3),
  (gen_random_uuid(), 'Dragon Palace',   'Traditional Cantonese dim sum and noodles',          'CHINESE',       4.1, 35, true,  CURRENT_TIMESTAMP, 4),
  (gen_random_uuid(), 'Wok & Noodle',    'Sichuan stir-fries and hand-pulled noodles',         'CHINESE',       3.9, 30, false, CURRENT_TIMESTAMP, 5),
  (gen_random_uuid(), 'La Bella Italia', 'Neapolitan wood-fired pizzas and fresh pasta',       'ITALIAN',       4.6, 45, true,  CURRENT_TIMESTAMP, 6),
  (gen_random_uuid(), 'Pizzeria Roma',   'Classic Roman-style thin-crust pizzas',              'ITALIAN',       4.2, 35, true,  CURRENT_TIMESTAMP, 7),
  (gen_random_uuid(), 'Sakura Sushi',    'Premium nigiri, sashimi and maki rolls',             'JAPANESE',      4.8, 50, true,  CURRENT_TIMESTAMP, 8),
  (gen_random_uuid(), 'Ramen Republic',  'Rich tonkotsu and shoyu ramen bowls',                'JAPANESE',      4.4, 30, true,  CURRENT_TIMESTAMP, 9),
  (gen_random_uuid(), 'Casa Mexicana',   'Street tacos, enchiladas and fresh guacamole',       'MEXICAN',       4.0, 35, true,  CURRENT_TIMESTAMP, 10),
  (gen_random_uuid(), 'Thai Orchid',     'Fragrant Thai curries and pad thai noodles',         'THAI',          4.3, 40, false, CURRENT_TIMESTAMP, 11),
  (gen_random_uuid(), 'Olive & Feta',    'Greek mezze platters and fresh salads',              'MEDITERRANEAN', 4.5, 30, true,  CURRENT_TIMESTAMP, 12),
  (gen_random_uuid(), 'The Burger Barn', 'Smash burgers, loaded fries and thick shakes',       'AMERICAN',      3.8, 20, true,  CURRENT_TIMESTAMP, 13),
  (gen_random_uuid(), 'Cedar Grove',     'Lebanese shawarma, hummus and falafel wraps',        'LEBANESE',      4.6, 35, true,  CURRENT_TIMESTAMP, 14),
  (gen_random_uuid(), 'Seoul Kitchen',   'Korean BBQ, bibimbap and kimchi stew',               'KOREAN',        4.4, 45, true,  CURRENT_TIMESTAMP, 15);
