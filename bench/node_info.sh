#!/bin/tcsh

/bin/echo "Host  : "`/bin/hostname`
/bin/echo "Kernel: "`/bin/cat /proc/version`
/bin/grep net /proc/pci
/usr/bin/awk '$0 ~ /processor|vendor_id|model name|cpu MHz|cache/' /proc/cpuinfo
/bin/grep MemTotal /proc/meminfo
/bin/grep SwapTotal /proc/meminfo
