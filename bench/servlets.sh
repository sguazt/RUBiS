#!/bin/tcsh

###############################################################################
#
# This script runs first the RUBiS browsing mix, then the bidding mix 
# for each rubis.properties_XX specified where XX is the number of emulated
# clients. Note that the rubis.properties_XX files must be configured
# with the corresponding number of clients.
# In particular set the following variables in rubis.properties_XX:
# httpd_use_version = Servlets
# workload_number_of_clients_per_node = XX/number of client machines
# workload_transition_table = yourPath/RUBiS/workload/transitions.txt 
#
# This script should be run from the RUBiS/bench directory on the local 
# client machine. 
# Results will be generated in the RUBiS/bench directory.
#
################################################################################

setenv SERVLETDIR /users/margueri/RUBiS/Servlets

# Go back to RUBiS root directory
cd ..

# Browse only mix

cp ./workload/browse_only_transitions_7.txt ./workload/transitions.txt

# rubis.properties_100 rubis.properties_200 rubis.properties_300 rubis.properties_400 rubis.properties_500 rubis.properties_600 rubis.properties_700 rubis.properties_800 rubis.properties_900 rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500 rubis.properties_1600 rubis.properties_1700 rubis.properties_1800 rubis.properties_1900 rubis.properties_2000

foreach i (rubis.properties_100 rubis.properties_200)
  cp bench/$i Client/build/rubis.properties
  ssh sci21 -n -l margueri ${SERVLETDIR}/tomcat_stop.sh 
  sleep 4
  ssh sci22 ${SERVLETDIR}/update_ids.sh
  ssh sci21 -n -l margueri ${SERVLETDIR}/tomcat_start.sh &
  sleep 4
  bench/flush_cache 190000
  ssh sci31 RUBiS/bench/flush_cache 190000	# remote client
  ssh sci21 RUBiS/bench/flush_cache 780000 	# servlet server
  ssh sci23 RUBiS/bench/flush_cache 780000 	# web server
  ssh sci22 RUBiS/bench/flush_cache 780000	# database
  make emulator
end

# Bidding mix

cp ./workload/default_transitions_7.txt ./workload/transitions.txt

# rubis.properties_100 rubis.properties_200 rubis.properties_300 rubis.properties_400 rubis.properties_500 rubis.properties_600 rubis.properties_700 rubis.properties_800 rubis.properties_900 rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500 rubis.properties_1600 rubis.properties_1700 rubis.properties_1800 rubis.properties_1900 rubis.properties_2000

foreach i ( rubis.properties_100)
  cp bench/$i Client/build/rubis.properties
  ssh sci21 -n -l margueri ${SERVLETDIR}/tomcat_stop.sh 
  sleep 4
  ssh sci22 ${SERVLETDIR}/update_ids.sh
  ssh sci21 -n -l margueri ${SERVLETDIR}/tomcat_start.sh &
  bench/flush_cache 190000
  ssh sci31 RUBiS/bench/flush_cache 190000	# remote client
  ssh sci21 RUBiS/bench/flush_cache 780000 	# servlet server
  ssh sci23 RUBiS/bench/flush_cache 780000 	# web server
  ssh sci22 RUBiS/bench/flush_cache 780000	# database
  make emulator
end

sleep 4

