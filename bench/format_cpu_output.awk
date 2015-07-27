#!/bin/awk -f

BEGIN {
}
{
  if ($1 ~ /Average/)
    { # Skip the Average values
      next;
    }

  if ($2 ~ /all/)
    { # This is the cpu info
      print 100-$7 > FILENAME".cpu.busy.dat";
    }
}
END {
}
