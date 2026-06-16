-- Seed menu items for 5 restaurants (joined by name since IDs are generated)

-- Spice Garden (INDIAN)
INSERT INTO menu_item (menu_item_id, name, description, price, category, is_available, restaurant_id)
SELECT gen_random_uuid(), 'Samosa (2 pcs)',      'Crispy pastry filled with spiced potato and peas',         79.00,  'STARTERS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Seekh Kebab',         'Minced lamb skewers with cumin and coriander',             189.00, 'STARTERS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Butter Chicken',      'Slow-cooked chicken in a rich tomato-cream sauce',         349.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Dal Makhani',         'Black lentils simmered overnight with butter and cream',   249.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Garlic Naan',         'Leavened bread with garlic and butter, baked in tandoor',  59.00,  'SIDES',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Steamed Basmati Rice','Long-grain basmati rice, steamed to perfection',           49.00,  'SIDES',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Gulab Jamun',         'Soft milk-solid dumplings soaked in rose-flavoured syrup', 119.00, 'DESSERTS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Mango Lassi',         'Chilled yoghurt drink blended with Alphonso mango',        99.00,  'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden'
UNION ALL
SELECT gen_random_uuid(), 'Masala Chai',         'Spiced milk tea brewed with cardamom and ginger',          49.00,  'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Spice Garden';

-- Dosa Palace (SOUTH_INDIAN)
INSERT INTO menu_item (menu_item_id, name, description, price, category, is_available, restaurant_id)
SELECT gen_random_uuid(), 'Medu Vada',           'Crispy lentil doughnuts served with coconut chutney',      89.00,  'STARTERS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace'
UNION ALL
SELECT gen_random_uuid(), 'Masala Dosa',         'Thin rice crepe filled with spiced potato and onion',      149.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace'
UNION ALL
SELECT gen_random_uuid(), 'Rava Dosa',           'Semolina crepe with pepper, cumin, and cashews',           159.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace'
UNION ALL
SELECT gen_random_uuid(), 'Idli Sambar (3 pcs)', 'Steamed rice cakes served with lentil stew and chutneys', 99.00,  'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace'
UNION ALL
SELECT gen_random_uuid(), 'Coconut Chutney',     'Fresh grated coconut blended with green chilli and curry', 29.00,  'SIDES',     false, restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace'
UNION ALL
SELECT gen_random_uuid(), 'Payasam',             'South Indian rice pudding with jaggery and cardamom',      99.00,  'DESSERTS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace'
UNION ALL
SELECT gen_random_uuid(), 'Filter Coffee',       'Strong South Indian drip coffee with frothed milk',        59.00,  'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace'
UNION ALL
SELECT gen_random_uuid(), 'Buttermilk',          'Salted and spiced chilled buttermilk with curry leaves',   39.00,  'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Dosa Palace';

-- Biryani House (INDIAN)
INSERT INTO menu_item (menu_item_id, name, description, price, category, is_available, restaurant_id)
SELECT gen_random_uuid(), 'Chicken 65',          'Deep-fried chicken with red chillies and curry leaves',    199.00, 'STARTERS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Biryani House'
UNION ALL
SELECT gen_random_uuid(), 'Hyderabadi Dum Biryani', 'Slow-cooked basmati rice layered with spiced mutton',  449.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Biryani House'
UNION ALL
SELECT gen_random_uuid(), 'Chicken Biryani',     'Aromatic basmati rice cooked with tender chicken pieces',  349.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Biryani House'
UNION ALL
SELECT gen_random_uuid(), 'Veg Biryani',         'Fragrant rice with seasonal vegetables and whole spices',  249.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Biryani House'
UNION ALL
SELECT gen_random_uuid(), 'Raita',               'Chilled yoghurt with cucumber, cumin, and mint',           59.00,  'SIDES',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Biryani House'
UNION ALL
SELECT gen_random_uuid(), 'Double Ka Meetha',    'Hyderabadi bread pudding with saffron and dry fruits',     129.00, 'DESSERTS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Biryani House'
UNION ALL
SELECT gen_random_uuid(), 'Rooh Afza Sharbat',   'Chilled rose and herb syrup drink',                        69.00,  'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Biryani House';

-- La Bella Italia (ITALIAN)
INSERT INTO menu_item (menu_item_id, name, description, price, category, is_available, restaurant_id)
SELECT gen_random_uuid(), 'Bruschetta',          'Toasted ciabatta with tomatoes, basil, and extra-virgin oil', 199.00, 'STARTERS', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'Arancini',            'Fried risotto balls stuffed with mozzarella and ragù',     249.00, 'STARTERS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'Margherita Pizza',    'Wood-fired pizza with San Marzano tomato and fior di latte',549.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'Tagliatelle al Ragù', 'Egg pasta ribbons with slow-braised Bolognese sauce',      649.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'Tiramisu',            'Espresso-soaked ladyfingers with mascarpone cream',         349.00, 'DESSERTS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'Panna Cotta',         'Vanilla cream dessert with wild berry compote',             299.00, 'DESSERTS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'Focaccia',            'Rosemary and sea salt flatbread drizzled with olive oil',   149.00, 'SIDES',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'San Pellegrino',      'Sparkling natural mineral water (500 ml)',                  99.00,  'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia'
UNION ALL
SELECT gen_random_uuid(), 'Espresso',            'Single shot of Italian espresso from Arabica beans',        149.00, 'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'La Bella Italia';

-- Sakura Sushi (JAPANESE)
INSERT INTO menu_item (menu_item_id, name, description, price, category, is_available, restaurant_id)
SELECT gen_random_uuid(), 'Edamame',             'Steamed salted young soybeans',                             149.00, 'STARTERS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Gyoza (6 pcs)',       'Pan-fried pork and cabbage dumplings with ponzu dip',       299.00, 'STARTERS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Salmon Nigiri (2 pcs)','Hand-pressed rice with premium Atlantic salmon',           349.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Dragon Roll (8 pcs)', 'Tempura prawn roll topped with avocado and eel sauce',      649.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Chirashi Bowl',       'Assorted sashimi over seasoned sushi rice',                 849.00, 'MAINS',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Miso Soup',           'Dashi broth with tofu, wakame, and spring onion',           99.00,  'SIDES',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Pickled Ginger',      'Sweet-cured gari ginger, served as a palate cleanser',      49.00,  'SIDES',     true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Mochi Ice Cream',     'Soft rice cake filled with matcha and red bean ice cream',  249.00, 'DESSERTS',  true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Matcha Latte',        'Ceremonial grade matcha whisked with steamed oat milk',     249.00, 'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi'
UNION ALL
SELECT gen_random_uuid(), 'Sake (100 ml)',        'Chilled junmai ginjo sake, light and fruity',               399.00, 'BEVERAGES', true,  restaurant_id FROM restaurant WHERE restaurant_name = 'Sakura Sushi';
