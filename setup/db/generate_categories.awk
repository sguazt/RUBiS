#!/bin/awk -f

BEGIN {
  FS = "[\(\)]";
  SQLoutput = "categories.sql";
  print "# Categories for RUBiS database" > SQLoutput;
  print "# This file has been automatically generated by generate_categories.awk script using" FILENAME "as input file\n" > SQLoutput;
}
{
  print "INSERT INTO categories VALUES (NULL,'" $1 "');" > SQLoutput;
}
