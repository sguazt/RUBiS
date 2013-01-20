dcsj-rubis
==========

A java implementation of the _Rice University Bidding System_ (RUBiS).

RUBiS is a free, open source initiative.
This implementation is a fork of version 1.4.3 of [http://rubis.ow2.org](OW2 RUBiS).
As of 2012, the original 1.4.3 version can still be downloaded at the [http://forge.objectweb.org/project/showfiles.php?group_id=44](OW2 site)


## Overview

RUBiS is an auction site prototype modeled after [eBay](http://www.ebay.com/) site (one of the largest online marketplace) that is especially used to evaluate application design patterns and application servers performance scalability.

The auction site benchmark implements the core functionality of an auction site: selling, browsing and bidding.
Complementary services like instant messaging or newsgroups are not currently implemented.
There are three kinds of user sessions: visitor, buyer, and seller.
For a visitor session, users need not register but are only allowed to browse.
Buyer and seller sessions require registration.
In addition to the functionality provided during visitor sessions, during a buyer session users can bid on items and consult a summary of their current bids, rating and comments left by other users.
Seller sessions require a fee before a user is allowed to put up an item for sale.
An auction starts immediately and lasts typically for no more than a week.
The seller can specify a reserve (minimum) price for an item.


## Comparison with OW2 RUBiS
 
In the _OW2 RUBiS_, several versions of RUBiS are implemented by using three different technologies: PHP, Java servlets and Enterprise Java Bean (EJB).
The main purpose is to compare the trade-off between performance and complexity obtained with these technologies, as shown in the paper [(Checchet et al.,2002)][checchet-2002-performance].

In our implementation, unlike the OW2 one, we do not focus on the performance evaluation of different technologies and design patterns.
Instead, our aim is to provide a fully functional implementation that can be used to benchmark real computing systems and evaluate their performance.
For these reasons, currently we only provide an implementation based on Java servlets.

## Compile and Install

To compile RUBiS you need Java &ge; 1.5 and [Apache Ant](http://ant.apache.org).
To run RUBiS you need a Java Servlet container &ge; 2.5, like [Apache Tomcat](http://tomcat.apache.org), and a DBMS like [MySQL](http://www.mysql.com).

### Compilation Steps

In the following we refer to the variable `$RUBIS_HOME` as a variable containing the path pointing to this version of RUBiS.

1. Move to the `$RUBIS_HOME' directory.

	$ cd $RUBIS_HOME

2. Copy the template property file `setup/user.properties.template` to file `user.properties`

	$ cp setup/user.properties.template user.properties

3. Edit the file `user.properties` to set properties to a proper value (e.g., the path to Apache Tomcat).

	$ vi user.properties

4. Move to the `servlets` directory.

	$ cd servlets

5. Edit the file `mysql.properties` to set properties to a proper value (e.g., the host name where the DBMS runs).

	$ vi src/conf/mysql.properties

6. Run Ant:

	$ ant clean all

## References

[checchet-2002-performance]: _Emmanuel Cecchet, Julie Marguerite and Willy Zwaenepoel_, __Performance and Scalability of EJB Applications__. In Proc. of the OOPSLA'02, Seattle, WA, USA, Nov. 4-8, 2002
