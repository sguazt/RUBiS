#!/bin/tcsh

###############################################################################
#
# This script runs first the RUBiS browsing mix, then the bidding mix 
# for each rubis.properties_XX specified where XX is the number of emulated
# clients. Note that the rubis.properties_XX files must be configured
# with the corresponding number of clients.
# In particular set the following variables in rubis.properties_XX:
# httpd_use_version = EJB
# workload_number_of_clients_per_node = XX/number of client machines
# workload_transition_table = yourPath/RUBiS/workload/transitions.txt 
#
# This script should be run from the RUBiS/bench directory on the local 
# client machine. 
# Results will be generated in the RUBiS/bench directory.
#
################################################################################

setenv EJBDIR /users/margueri/RUBiS/EJB_EntityBean_id

# Go back to RUBiS root directory
cd ..

# Browse only JBoss

cp ./workload/browse_only_transitions_7.txt ./workload/transitions.txt

foreach i ( rubis.properties_20 rubis.properties_40 rubis.properties_60 rubis.properties_80 rubis.properties_100 rubis.properties_120 rubis.properties_140 rubis.properties_160 rubis.properties_180 rubis.properties_200 rubis.properties_220 rubis.properties_240 rubis.properties_260 rubis.properties_280 rubis.properties_300)
  cp bench/$i Client/build/rubis.properties
  ssh sci21 -n -l margueri ${EJBDIR}/tomcat_stop.sh
  ssh sci20 ${EJBDIR}/jboss_stop.sh
  sleep 4
  ssh sci22 ${EJBDIR}/update_ids.sh
  ssh sci21 -n -l margueri ${EJBDIR}/tomcat_jboss_start.sh &
  ssh sci20 "cd ${EJBDIR} ; jboss_start.sh" &
  sleep 10
  bench/flush_cache 490000
  ssh sci8 RUBiS/bench/flush_cache 490000	# remote client
  ssh sci20 RUBiS/bench/flush_cache 880000  # ejb server
  ssh sci21 RUBiS/bench/flush_cache 880000 	# servlet server
  ssh sci23 RUBiS/bench/flush_cache 880000 	# web server
  ssh sci22 RUBiS/bench/flush_cache 880000	# database
  make emulator
end

# Default JBoss

cp ./workload/default_transitions_7.txt ./workload/transitions.txt


foreach i ( rubis.properties_20 rubis.properties_40 rubis.properties_60 rubis.properties_80 rubis.properties_100 rubis.properties_120 rubis.properties_140 rubis.properties_160 rubis.properties_180 rubis.properties_200 rubis.properties_220 rubis.properties_240 rubis.properties_260 rubis.properties_280 rubis.properties_300)
  cp bench/$i Client/build/rubis.properties
  ssh sci21 -n -l margueri ${EJBDIR}/tomcat_stop.sh
  ssh sci20 ${EJBDIR}/jboss_stop.sh
  sleep 4
  ssh sci22 ${EJBDIR}/update_ids.sh
  ssh sci21 -n -l margueri ${EJBDIR}/tomcat_jboss_start.sh &
  ssh sci20 "cd ${EJBDIR} ; jboss_start.sh" &
  sleep 10
  bench/flush_cache 490000
  ssh sci8 RUBiS/bench/flush_cache 490000	# remote client
  ssh sci20 RUBiS/bench/flush_cache 880000      # ejb server
  ssh sci21 RUBiS/bench/flush_cache 880000 	# servlet server
  ssh sci23 RUBiS/bench/flush_cache 880000 	# web server
  ssh sci22 RUBiS/bench/flush_cache 880000	# database
  make emulator
end

ssh sci20 ${EJBDIR}/jonas_stop.sh
sleep 4
