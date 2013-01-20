DELETE FROM ids;
INSERT INTO ids (category,region,users,item,comment,bid,buyNow) SELECT MAX(categories.id)+1,MAX(regions.id)+1,MAX(users.id)+1,MAX(items.id)+1,MAX(comments.id)+1,MAX(bids.id)+1,MAX(buy_now.id)+1 FROM categories,regions,users,items,comments,bids,buy_now;
