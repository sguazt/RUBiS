#!/bin/tcsh

# Go back to RUBiS root directory
cd ..

# Browse only

cp ./workload/browse_only_transitions_7.txt ./workload/transitions.txt

foreach i (rubis.properties_100 rubis.properties_200 rubis.properties_300 rubis.properties_400 rubis.properties_500 rubis.properties_600 rubis.properties_700 rubis.properties_800 rubis.properties_900 rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500) 
  cp bench/$i rubis.properties
  bench/flush_cache 490000
  rsh sci23 RUBiS/bench/flush_cache 490000 	# web server
  rsh sci22 RUBiS/bench/flush_cache 490000	# database
  rsh sci6 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci7 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci12 RUBiS/bench/flush_cache 490000	# remote client
  make emulator
end

# Default

cp ./workload/default_transitions_7.txt ./workload/transitions.txt

foreach i (rubis.properties_100 rubis.properties_200 rubis.properties_300 rubis.properties_400 rubis.properties_500 rubis.properties_600 rubis.properties_700 rubis.properties_800 rubis.properties_900 rubis.properties_1000 rubis.properties_1100 rubis.properties_1200 rubis.properties_1300 rubis.properties_1400 rubis.properties_1500) 
  cp bench/$i rubis.properties
  bench/flush_cache 490000
  rsh sci23 RUBiS/bench/flush_cache 490000 	# web server
  rsh sci22 RUBiS/bench/flush_cache 490000	# database
  rsh sci6 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci7 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci8 RUBiS/bench/flush_cache 490000	# remote client
  rsh sci12 RUBiS/bench/flush_cache 490000	# remote client
  make emulator
end
