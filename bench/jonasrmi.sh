#!/bin/tcsh

setenv EJBDIR /users/cecchet/RUBiS/EJB_EntityBean_id

# Go back to RUBiS root directory
cd ..

# Default JOnAS

cp ./workload/default_transitions_7.txt ./workload/transitions.txt


foreach i (  rubis.properties_300)
  cp bench/$i Client/rubis.properties
  rsh sci21 -n -l root ${EJBDIR}/tomcat_stop.sh 
  rsh sci20 ${EJBDIR}/jonas_stop.sh
  sleep 4
  rsh sci22 ${EJBDIR}/update_ids.sh
  rsh sci21 -n -l root ${EJBDIR}/tomcat_jonas_start.sh &
  rsh sci20 "cd ${EJBDIR} ; jonas_start.sh" &
  sleep 4
  bench/flush_cache 490000
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci20 RUBiS/bench/flush_cache 880000      # ejb server
  rsh sci21 RUBiS/bench/flush_cache 880000 	# servlet server
  rsh sci23 RUBiS/bench/flush_cache 880000 	# web server
  rsh sci22 RUBiS/bench/flush_cache 880000	# database
  make emulator
end
