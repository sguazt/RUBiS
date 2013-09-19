#!/bin/bash

## Script to run when the native RUBiS client is used

cwd=$(dirname $0)
mysqladmin -f -urubis -prubis drop rubis
mysqladmin -urubis -prubis create rubis
mysql -urubis -prubis rubis <$cwd/rubis-no_fk.sql
mysql -urubis -prubis rubis <$cwd/categories.sql
mysql -urubis -prubis rubis <$cwd/regions.sql
#mysql -urubis -prubis rubis <$cwd/update_ids.sql
