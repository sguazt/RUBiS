#!/bin/tcsh

###############################################################################
#
# This script runs first the RUBiS browsing mix, then the bidding mix 
# for each rubis.properties_XX specified where XX is the number of emulated
# clients. Note that the rubis.properties_XX files must be configured
# with the corresponding number of clients.
# In particular set the following variables in rubis.properties_XX:
# httpd_use_version = PHP
# workload_number_of_clients_per_node = XX/number of client machines
# workload_transition_table = yourPath/RUBiS/workload/transitions.txt 
#
# This script should be run from the RUBiS/bench directory on the local 
# client machine. 
# Results will be generated in the RUBiS/bench directory.
#
################################################################################


# Go back to RUBiS root directory 
cd ..

# Read only workload

cp ./workload/browse_only_transitions_7.txt ./workload/transitions.txt

foreach i (rubis.properties_100 rubis.properties_200 rubis.properties_300 rubis.properties_400 rubis.properties_500 rubis.properties_600 rubis.properties_700 rubis.properties_800 rubis.properties_900 rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500) 
  cp bench/$i Client/rubis.properties
# flush the cache on the local client machine
  bench/flush_cache 490000
# flush the cache on the severs and remote client machines
  rsh sci23 RUBiS/bench/flush_cache 490000 	# web server
  rsh sci22 RUBiS/bench/flush_cache 490000	# database server
  rsh sci6 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci7 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci12 RUBiS/bench/flush_cache 490000	# remote client
  make emulator
end

# Read/write workload

cp ./workload/default_transitions_7.txt ./workload/transitions.txt

foreach i (rubis.properties_100 rubis.properties_200 rubis.properties_300 rubis.properties_400 rubis.properties_500 rubis.properties_600 rubis.properties_700 rubis.properties_800 rubis.properties_900 rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500) 
  cp bench/$i Client/rubis.properties
  bench/flush_cache 490000
  rsh sci23 RUBiS/bench/flush_cache 490000 	# web server
  rsh sci22 RUBiS/bench/flush_cache 490000	# database server
  rsh sci6 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci7 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci12 RUBiS/bench/flush_cache 490000	# remote client
  make emulator
end
