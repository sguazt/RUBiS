#!/bin/bash

cwd=$(dirname $0)
mysql -uroot rubis <$cwd/rubis.sql
mysql -uroot rubis <$cwd/categories.sql
mysql -uroot rubis <$cwd/regions.sql
mysql -uroot rubis <$cwd/update_ids.sql
