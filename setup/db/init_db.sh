#!/bin/bash

cwd=$(dirname $0)
mysqladmin -f -uroot -prubis drop rubis
mysqladmin -uroot -prubis create rubis
mysql -uroot rubis <$cwd/rubis.sql
mysql -uroot rubis <$cwd/categories.sql
mysql -uroot rubis <$cwd/regions.sql
mysql -uroot rubis <$cwd/update_ids.sql
