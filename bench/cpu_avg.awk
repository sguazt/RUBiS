#!/bin/awk -f

BEGIN {
  ramp = 400;
  runtime = 1500;
  count = 0;
  sum = 0;
}
{
  if ((NR > ramp) && (NR < ramp+runtime))
    {
      sum += $1;
      count++;
    }
}
END {
  print (sum/count)"%";
}
