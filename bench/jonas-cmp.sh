#!/bin/tcsh

setenv EJBDIR /users/margueri/RUBiS/EJB_EntityBean_id

# Go back to RUBiS root directory
cd ..

# Browse only JOnAS

cp ./workload/browse_only_transitions_7.txt ./workload/transitions.txt

# rubis.properties_20 rubis.properties_40 rubis.properties_60 rubis.properties_80 rubis.properties_100 rubis.properties_120 rubis.properties_140 rubis.properties_160 rubis.properties_180 rubis.properties_200 rubis.properties_220 rubis.properties_240  rubis.properties_260 rubis.properties_280 rubis.properties_300

foreach i ( rubis.properties_140 rubis.properties_160 rubis.properties_180 rubis.properties_200 rubis.properties_220 rubis.properties_240  rubis.properties_260 rubis.properties_280)
  cp bench/$i Client/rubis.properties
  ssh sci21 -n -l root ${EJBDIR}/tomcat_stop.sh 
  ssh sci20 ${EJBDIR}/jonas_stop.sh
  sleep 4
  ssh sci22 ${EJBDIR}/update_ids.sh
  ssh sci21 -n -l root ${EJBDIR}/tomcat_jonas_start.sh &
  ssh sci20 "cd ${EJBDIR} ; jonas2.6_start.sh" &
  sleep 4
  bench/flush_cache 190000
  ssh sci31 RUBiS/bench/flush_cache 190000      # remote client
  ssh sci20 RUBiS/bench/flush_cache 880000      # ejb server
  ssh sci21 RUBiS/bench/flush_cache 880000 	# servlet server
  ssh sci23 RUBiS/bench/flush_cache 880000 	# web server
  ssh sci22 RUBiS/bench/flush_cache 880000	# database
  make emulator
end

# Default JOnAS

cp ./workload/default_transitions_7.txt ./workload/transitions.txt

#rubis.properties_20 rubis.properties_40 rubis.properties_60 rubis.properties_80 rubis.properties_100 rubis.properties_120 rubis.properties_140 rubis.properties_160 rubis.properties_180 rubis.properties_200 rubis.properties_220 rubis.properties_240 rubis.properties_260 rubis.properties_280 rubis.properties_300

foreach i (  )
  cp bench/$i Client/rubis.properties
  ssh sci21 -n -l root ${EJBDIR}/tomcat_stop.sh 
  ssh sci20 ${EJBDIR}/jonas2.6_stop.sh
  sleep 4
  ssh sci22 ${EJBDIR}/update_ids.sh
  ssh sci21 -n -l root ${EJBDIR}/tomcat_jonas_start.sh &
  ssh sci20 "cd ${EJBDIR} ; jonas2.6_start.sh" &
  sleep 4
  bench/flush_cache 190000
  ssh sci31 RUBiS/bench/flush_cache 190000	# remote client
  ssh sci20 RUBiS/bench/flush_cache 880000      # ejb server
  ssh sci21 RUBiS/bench/flush_cache 880000 	# servlet server
  ssh sci23 RUBiS/bench/flush_cache 880000 	# web server
  ssh sci22 RUBiS/bench/flush_cache 880000	# database
  make emulator
end

ssh sci20 ${EJBDIR}/jonas2.6_stop.sh
sleep 4
