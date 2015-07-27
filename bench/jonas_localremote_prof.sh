#!/bin/tcsh

# Go back to RUBiS root directory
cd ..

# Default JOnAS

cp ./workload/default_transitions_7.txt ./workload/transitions.txt


setenv EJBDIR /users/margueri/RUBiS/EJB_local_remote
foreach i (  rubis.properties_jonas_rmi_lr_prof)
  cp bench/$i Client/rubis.properties
  rsh sci21 -n -l root ${EJBDIR}/tomcat_stop.sh 
  rsh sci20 ${EJBDIR}/jonas_stop.sh
  sleep 4
  rsh sci22 ${EJBDIR}/update_ids.sh
  rsh sci21 -n -l root ${EJBDIR}/tomcat_jonas_start.sh &
  rsh sci20 "cd ${EJBDIR} ; jonas_profile_start.sh" &
  sleep 10
  bench/flush_cache 190000
  rsh sci31 RUBiS/bench/flush_cache 190000	# remote client
  rsh sci20 RUBiS/bench/flush_cache 880000      # ejb server
  rsh sci21 RUBiS/bench/flush_cache 880000 	# servlet server
  rsh sci23 RUBiS/bench/flush_cache 880000 	# web server
  rsh sci22 RUBiS/bench/flush_cache 880000	# database
  make emulator
end

rsh sci20 ${EJBDIR}/jonas_stop.sh
exit
  
