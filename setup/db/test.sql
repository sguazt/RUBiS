INSERT INTO categories VALUES (1, 'Home');
INSERT INTO categories VALUES (2, 'Books');
INSERT INTO categories VALUES (3, 'Office/Business');
INSERT INTO categories VALUES (4, 'Movies');
INSERT INTO categories VALUES (5, 'Music');
INSERT INTO categories VALUES (6, 'Photo');
INSERT INTO categories VALUES (7, 'Clothing');
INSERT INTO categories VALUES (8, 'Sports');
INSERT INTO categories VALUES (9, 'Tickets/Travel');
INSERT INTO categories VALUES (10, 'Toys');
INSERT INTO categories VALUES (11, 'Hobbies');
INSERT INTO categories VALUES (12, 'Computers');
INSERT INTO categories VALUES (13, 'Electronics');
INSERT INTO categories VALUES (14, 'Automotive');
INSERT INTO categories VALUES (15, 'Everything else');
INSERT INTO categories VALUES (16, 'Coins');
INSERT INTO categories VALUES (17, 'Jewelry');
INSERT INTO categories VALUES (18, 'Antiques/Art');
INSERT INTO categories VALUES (19, 'Collectibles');

INSERT INTO regions VALUES (1, 'Alabany');
INSERT INTO regions VALUES (2, 'Atlanta');
INSERT INTO regions VALUES (3, 'Boston');
INSERT INTO regions VALUES (4, 'Buffalo');
INSERT INTO regions VALUES (5, 'Chicago');
INSERT INTO regions VALUES (6, 'Cleveland');
INSERT INTO regions VALUES (7, 'Detroit');
INSERT INTO regions VALUES (8, 'Honolulu');
INSERT INTO regions VALUES (9, 'Houston');
INSERT INTO regions VALUES (10, 'Los Angeles');
INSERT INTO regions VALUES (11, 'Milwaukee');
INSERT INTO regions VALUES (12, 'New Orleans');
INSERT INTO regions VALUES (13, 'Phoenix');
INSERT INTO regions VALUES (14, 'Portland');
INSERT INTO regions VALUES (15, 'San Antonio');
INSERT INTO regions VALUES (16, 'San Diego');
INSERT INTO regions VALUES (17, 'Seattle');
INSERT INTO regions VALUES (18, 'St Louis');
INSERT INTO regions VALUES (19, 'Washington');

INSERT INTO users VALUES (1, 'Emmanuel', 'Cecchet', 'manu', 'pouet', 'cecchet@rice.edu', 1000, 10.5, DATE_SUB(NOW(), INTERVAL 2 MONTH), 9);
INSERT INTO users VALUES (2, 'Julie', 'Marguerite', 'zuli', 'grenoble', 'julie.marguerite@inrialpes.fr', 1000, 0, DATE_SUB(NOW(), INTERVAL 20 DAY), 9);
INSERT INTO users VALUES (3, 'Willy', 'Zwaenopoel', 'willy', 'belgium', 'willy@rice.edu', 500, 250.0, DATE_SUB(NOW(), INTERVAL 5 HOUR), 9);

INSERT INTO items VALUES (1, 'Boris Vian - L\'automne a Pekin', 'Original edition', 30.0, 1, 50.0, 100.0, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 2, 2);
INSERT INTO items VALUES (2, 'Porsche 911 Turbo', 'Not a FLOOD car ! ;-)', 50000.0, 1, 0, 0, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, 14);
INSERT INTO items VALUES (3, 'VAX/1', 'Like new!', 1.0, 1, 0, 10.0, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 3, 12);

INSERT INTO bids VALUES (NULL, 1, 3, 1, 3.0, 20.0, NOW());

INSERT INTO comments VALUES (NULL, 1, 2, 1, 5, NOW(), 'That\'s really a great book and she is a great seller !');
