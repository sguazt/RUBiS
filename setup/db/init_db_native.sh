#!/bin/bash

## Script to run when the native RUBiS client is used

cwd=$(dirname $0)
mysqladmin -f -uroot -p drop rubis
mysqladmin -uroot -p create rubis
mysql -uroot rubis <$cwd/rubis-no_fk.sql
mysql -uroot rubis <$cwd/categories.sql
mysql -uroot rubis <$cwd/regions.sql
#mysql -uroot rubis <$cwd/update_ids.sql
