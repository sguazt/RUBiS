-- CREATE DATABASE rubis;
-- connect rubis;

DROP TABLE IF EXISTS categories;
CREATE TABLE categories
(
   id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   name VARCHAR(50),
   CONSTRAINT pk_categories PRIMARY KEY(id)
);

DROP TABLE IF EXISTS regions;
CREATE TABLE regions
(
   id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   name VARCHAR(25),
   CONSTRAINT pk_regions PRIMARY KEY(id)
);

DROP TABLE IF EXISTS users;
CREATE TABLE users (
   id            INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   firstname     VARCHAR(20),
   lastname      VARCHAR(20),
   nickname      VARCHAR(20) NOT NULL UNIQUE,
   password      VARCHAR(20) NOT NULL,
   email         VARCHAR(50) NOT NULL,
   rating        INTEGER,
   balance       FLOAT,
   creation_date DATETIME,
   region        INTEGER UNSIGNED NOT NULL,
   CONSTRAINT pk_users PRIMARY KEY (id),
   INDEX idx_users_auth (nickname,password),
   INDEX idx_users_region (region),
   CONSTRAINT fk_users_region FOREIGN KEY (region) REFERENCES regions (id)
);

DROP TABLE IF EXISTS items;
CREATE TABLE items
(
   id            INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   name          VARCHAR(100),
   description   TEXT,
   initial_price FLOAT UNSIGNED NOT NULL,
   quantity      INTEGER UNSIGNED NOT NULL,
   reserve_price FLOAT UNSIGNED DEFAULT 0,
   buy_now       FLOAT UNSIGNED DEFAULT 0,
   nb_of_bids    INTEGER UNSIGNED DEFAULT 0,
   max_bid       FLOAT UNSIGNED DEFAULT 0,
   start_date    DATETIME,
   end_date      DATETIME,
   seller        INTEGER UNSIGNED NOT NULL,
   category      INTEGER UNSIGNED NOT NULL,
   CONSTRAINT pk_items PRIMARY KEY (id),
   INDEX idx_items_seller (seller),
   INDEX idx_items_category (category),
   CONSTRAINT fk_items_seller FOREIGN KEY (seller) REFERENCES users (id),
   CONSTRAINT fk_items_category FOREIGN KEY (category) REFERENCES categories (id)
);

DROP TABLE IF EXISTS old_items;
CREATE TABLE old_items
(
   id            INTEGER UNSIGNED NOT NULL,
   name          VARCHAR(100),
   description   TEXT,
   initial_price FLOAT UNSIGNED NOT NULL,
   quantity      INTEGER UNSIGNED NOT NULL,
   reserve_price FLOAT UNSIGNED DEFAULT 0,
   buy_now       FLOAT UNSIGNED DEFAULT 0,
   nb_of_bids    INTEGER UNSIGNED DEFAULT 0,
   max_bid       FLOAT UNSIGNED DEFAULT 0,
   start_date    DATETIME,
   end_date      DATETIME,
   seller        INTEGER UNSIGNED NOT NULL,
   category      INTEGER UNSIGNED NOT NULL,
   CONSTRAINT pk_old_items PRIMARY KEY (id),
   INDEX idx_old_items_seller (seller),
   INDEX idx_old_items_category (category),
   CONSTRAINT fk_old_items_seller FOREIGN KEY (seller) REFERENCES users (id),
   CONSTRAINT fk_old_items_category FOREIGN KEY (category) REFERENCES categories (id)
);

DROP TABLE IF EXISTS bids;
CREATE TABLE bids (
   id      INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   user_id INTEGER UNSIGNED NOT NULL,
   item_id INTEGER UNSIGNED NOT NULL,
   qty     INTEGER UNSIGNED NOT NULL,
   bid     FLOAT UNSIGNED NOT NULL,
   max_bid FLOAT UNSIGNED NOT NULL,
   date    DATETIME,
   CONSTRAINT pk_bids PRIMARY KEY (id),
   INDEX idx_bids_item (item_id),
   INDEX idx_bids_user (user_id),
   CONSTRAINT fk_bids_user FOREIGN KEY (user_id) REFERENCES users (id),
   CONSTRAINT fk_bids_item FOREIGN KEY (item_id) REFERENCES items (id)
);

DROP TABLE IF EXISTS comments;
CREATE TABLE comments
(
   id           INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   from_user_id INTEGER UNSIGNED NOT NULL,
   to_user_id   INTEGER UNSIGNED NOT NULL,
   item_id      INTEGER UNSIGNED NOT NULL,
   rating       INTEGER,
   date         DATETIME,
   comment      TEXT,
   CONSTRAINT pk_comments PRIMARY KEY (id),
   INDEX idx_comments_from_user (from_user_id),
   INDEX idx_comments_to_user (to_user_id),
   INDEX idx_comments_item (item_id),
   CONSTRAINT fk_comments_from_user FOREIGN KEY (from_user_id) REFERENCES users (id),
   CONSTRAINT fk_comments_to_user FOREIGN KEY (to_user_id) REFERENCES users (id),
   CONSTRAINT fk_comments_item FOREIGN KEY (item_id) REFERENCES items (id)
);

DROP TABLE IF EXISTS buy_now;
CREATE TABLE buy_now
(
   id       INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   buyer_id INTEGER UNSIGNED NOT NULL,
   item_id  INTEGER UNSIGNED NOT NULL,
   qty      INTEGER UNSIGNED NOT NULL,
   date     DATETIME,
   CONSTRAINT pk_buy_now PRIMARY KEY (id),
   INDEX idx_buy_now_buyer (buyer_id),
   INDEX idx_buy_now_item (item_id),
   CONSTRAINT fk_buy_now_buyer FOREIGN KEY (buyer_id) REFERENCES users (id),
   CONSTRAINT fk_buy_now_item FOREIGN KEY (item_id) REFERENCES items (id)
);

DROP TABLE IF EXISTS ids;
CREATE TABLE ids
(
   id        INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
   category  INTEGER UNSIGNED NOT NULL,
   region    INTEGER UNSIGNED NOT NULL,
   users     INTEGER UNSIGNED NOT NULL,
   item      INTEGER UNSIGNED NOT NULL,
   comment   INTEGER UNSIGNED NOT NULL,
   bid       INTEGER UNSIGNED NOT NULL,
   buyNow    INTEGER UNSIGNED NOT NULL,
   CONSTRAINT pk_ids PRIMARY KEY (id)
);
