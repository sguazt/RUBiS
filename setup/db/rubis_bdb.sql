CREATE DATABASE rubis_bdb;
connect rubis_bdb;

CREATE TABLE categories (
   id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
   name VARCHAR(50),
   PRIMARY KEY(id)
) TYPE=BDB;

CREATE TABLE regions (
   id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
   name VARCHAR(25),
   PRIMARY KEY(id)
) TYPE=BDB;

CREATE TABLE users (
   id            INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
   firstname     VARCHAR(20),
   lastname      VARCHAR(20),
   nickname      VARCHAR(20) NOT NULL UNIQUE,
   password      VARCHAR(20) NOT NULL,
   email         VARCHAR(50) NOT NULL,
   rating        INTEGER,
   balance       FLOAT,
   creation_date DATETIME,
   region        INTEGER UNSIGNED NOT NULL,
   PRIMARY KEY(id),
   INDEX auth (nickname,password),
   INDEX region_id (region)
) TYPE=BDB;

CREATE TABLE items (
   id            INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
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
   PRIMARY KEY(id),
   INDEX seller_id (seller),
   INDEX category_id (category)
) TYPE=BDB;

CREATE TABLE old_items (
   id            INTEGER UNSIGNED NOT NULL UNIQUE,
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
   PRIMARY KEY(id),
   INDEX seller_id (seller),
   INDEX category_id (category)
) TYPE=BDB;

CREATE TABLE bids (
   id      INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
   user_id INTEGER UNSIGNED NOT NULL,
   item_id INTEGER UNSIGNED NOT NULL,
   qty     INTEGER UNSIGNED NOT NULL,
   bid     FLOAT UNSIGNED NOT NULL,
   max_bid FLOAT UNSIGNED NOT NULL,
   date    DATETIME,
   PRIMARY KEY(id),
   INDEX item (item_id),
   INDEX user (user_id)
) TYPE=BDB;

CREATE TABLE comments (
   id           INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
   from_user_id INTEGER UNSIGNED NOT NULL,
   to_user_id   INTEGER UNSIGNED NOT NULL,
   item_id      INTEGER UNSIGNED NOT NULL,
   rating       INTEGER,
   date         DATETIME,
   comment      TEXT,
   PRIMARY KEY(id),
   INDEX from_user (from_user_id),
   INDEX to_user (to_user_id),
   INDEX item (item_id)
) TYPE=BDB;

CREATE TABLE buy_now (
   id       INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
   buyer_id INTEGER UNSIGNED NOT NULL,
   item_id  INTEGER UNSIGNED NOT NULL,
   qty      INTEGER UNSIGNED NOT NULL,
   date     DATETIME,
   PRIMARY KEY(id),
   INDEX buyer (buyer_id),
   INDEX item (item_id)
) TYPE=BDB;
