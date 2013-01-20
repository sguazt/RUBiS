MySQL:
UPDATE items SET start_date=NOW(), end_date=DATE_ADD(NOW(), INTERVAL 7 DAY);

PostGreSQL:
UPDATE items SET start_date=NOW(), end_date=(NOW()+7);