#!/bin/tcsh

# Go back to RUBiS root directory
cd ..

# Default Jboss

cp ./workload/browse_only_transitions_7.txt ./workload/transitions.txt


setenv EJBDIR /users/cecchet/RUBiS/EJB_EntityBean_id_BMP
cp ${EJBDIR}/rubis.jar /SciOS/opt/JBoss-2.4.4/deploy/
foreach i (  rubis.properties_jboss_ebbmp_prof)
  cp bench/$i Client/rubis.properties
  rsh sci21 -n -l root ${EJBDIR}/tomcat_stop.sh 
  rsh sci20 ${EJBDIR}/jboss_stop.sh
  sleep 4
  rsh sci22 ${EJBDIR}/update_ids.sh
  rsh sci21 -n -l root ${EJBDIR}/tomcat_jboss_start.sh &
  rsh sci20 "cd ${EJBDIR} ; jboss_profile_start.sh" &
  sleep 30
  bench/flush_cache 490000
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci20 RUBiS/bench/flush_cache 880000      # ejb server
  rsh sci21 RUBiS/bench/flush_cache 880000 	# servlet server
  rsh sci23 RUBiS/bench/flush_cache 880000 	# web server
  rsh sci22 RUBiS/bench/flush_cache 880000	# database
  make emulator
end

rsh sci20 ${EJBDIR}/jboss_stop.sh
exit

