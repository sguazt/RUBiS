# Main Makefile #

include config.mk

####################
#   EJB versions   #
####################
#
#db_id:
#	cd EJB_DB_id ; make all
#
#eb_id:
#	cd EJB_EntityBean_id ; make all


#########################
#    Servlets version   #
#########################

servlets: 
	cd Servlets ; make all

####################
#       Client     #
####################

client:
	cd Client ; make all

initDB:
	${JAVA} -classpath ./Client:./setup/db edu.rice.rubis.client.InitDB ${PARAM}

emulator:
	${JAVA} -classpath ./Client edu.rice.rubis.client.ClientEmulator


############################
#       Global rules       #
############################

#DIRS = Client Servlets EJB_DB_id EJB_EntityBean_id EJB_SessionBean EJB_Session_facade
DIRS = Client Servlets

all: flush_cache
	-for d in ${DIRS}; do (cd $$d ; ${MAKE} all); done

world: all javadoc

javadoc :
	-for d in ${DIRS}; do (cd $$d ; ${MAKE} javadoc); done

clean:
	-for d in ${DIRS}; do (cd $$d ; ${MAKE} clean); done

flush_cache: bench/flush_cache.c
	$(CC) $(CFLAGS) bench/flush_cache.c -o bench/flush_cache
