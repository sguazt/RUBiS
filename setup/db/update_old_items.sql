INSERT INTO old_items SELECT * FROM items WHERE end_date < "2001-10-18 16:17:00";
DELETE FROM items WHERE end_date < "2001-10-18 16:17:00";
