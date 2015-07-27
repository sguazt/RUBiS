#!/bin/tcsh

setenv EJBDIR /users/cecchet/RUBiS/EJB_SessionBean

# Go back to RUBiS root directory
cd ..

# Browse only JBoss

cp ./workload/browse_only_transitions_7.txt ./workload/transitions.txt

foreach i (rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500)
  cp bench/$i Client/rubis.properties
  rsh sci21 -n -l root ${EJBDIR}/tomcat_stop.sh
  rsh sci20 ${EJBDIR}/jboss_stop.sh
  sleep 4
  rsh sci22 ${EJBDIR}/update_ids.sh
  rsh sci21 -n -l root ${EJBDIR}/tomcat_jboss_start.sh &
  rsh sci20 "cd ${EJBDIR} ; jboss_start.sh" &
  sleep 10
  bench/flush_cache 490000
  rsh sci6 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci7 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci9 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci10 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci11 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci12 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci21 RUBiS/bench/flush_cache 880000      # servlet server
  rsh sci23 RUBiS/bench/flush_cache 880000      # web server
  rsh sci22 RUBiS/bench/flush_cache 880000      # database
  make emulator
end


# Default JBoss

cp ./workload/default_transitions_7.txt ./workload/transitions.txt

foreach i ( rubis.properties_100 rubis.properties_200 rubis.properties_300 rubis.properties_400 rubis.properties_500 rubis.properties_600 rubis.properties_700 rubis.properties_800 rubis.properties_900 rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500)
  cp bench/$i Client/rubis.properties
  rsh sci21 -n -l root ${EJBDIR}/tomcat_stop.sh
  rsh sci20 ${EJBDIR}/jboss_stop.sh
  sleep 4
  rsh sci22 ${EJBDIR}/update_ids.sh
  rsh sci21 -n -l root ${EJBDIR}/tomcat_jboss_start.sh &
  rsh sci20 "cd ${EJBDIR} ; jboss_start.sh" &
  sleep 10
  bench/flush_cache 490000
  rsh sci6 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci7 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci9 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci10 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci11 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci12 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci21 RUBiS/bench/flush_cache 880000      # servlet server
  rsh sci23 RUBiS/bench/flush_cache 880000      # web server
  rsh sci22 RUBiS/bench/flush_cache 880000      # database
  make emulator
end

