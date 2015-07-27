#!/bin/sh


for i in 200* ; do
  if [ -d "$i"/ ]; then
    # Generate data files
    echo $i/
    gunzip $i"/db_server.gz"
    gunzip $i"/web_server.gz"
    gunzip $i"/servlets_server.gz"
    gunzip $i"/ejb_server.gz"
    format_cpu_output.awk $i"/db_server"
    format_cpu_output.awk $i"/web_server"
    gzip -9 $i"/db_server" &
    gzip -9 $i"/web_server" &
    gzip -9 $i"/servlets_server" &
    gzip -9 $i"/ejb_server" &

    grep "set" $i"/index.html"
    grep "Total number of clients" $i"/index.html"
    echo -n "DB :"
    cpu_avg.awk $i"/db_server.cpu.busy.dat"
    echo -n "WS :"
    cpu_avg.awk $i"/web_server.cpu.busy.dat"
    echo -n "Servlet :"
    cpu_avg.awk $i"/servlets_server.cpu.busy.dat"
    echo -n "EJB :"
    cpu_avg.awk $i"/ejb_server.cpu.busy.dat"    
    rm -f $i/*.dat
    echo "."
  fi
done
