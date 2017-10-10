License
=======

Unless otherwise noted, This project is in the public domain in the United
States because it contains materials that originally came from the United
States Geological Survey, an agency of the United States Department of
Interior. For more information, see the official USGS copyright policy at
https://www2.usgs.gov/visual-id/credit_usgs.html#copyright

Additionally, we waive copyright and related rights in the work
worldwide through the CC0 1.0 Universal public domain dedication.


CC0 1.0 Universal Summary
-------------------------

This is a human-readable summary of the
[Legal Code (read the full text)][1].


### No Copyright

The person who associated a work with this deed has dedicated the work to
the public domain by waiving all of his or her rights to the work worldwide
under copyright law, including all related and neighboring rights, to the
extent allowed by law.

You can copy, modify, distribute and perform the work, even for commercial
purposes, all without asking permission.


### Other Information

In no way are the patent or trademark rights of any person affected by CC0,
nor are the rights that other persons may have in the work or in how the
work is used, such as publicity or privacy rights.

Unless expressly stated otherwise, the person who associated a work with
this deed makes no warranties about the work, and disclaims liability for
all uses of the work, to the fullest extent permitted by applicable law.
When using or citing the work, you should not imply endorsement by the
author or the affirmer.

* C++ Dependent libraries found in the `cpp/lib` directory are distributed under
the open source (or open source-like) licenses/agreements. Appropriate license
agreements for each library can be found in the `cpp/lib` directory.

* Java Dependent libraries found in the `java/lib` directory are distributed under
the open source (or open source-like) licenses/agreements. Appropriate license
agreements for each library can be found in the `java/lib` directory.

Libraries used at runtime
-------------------------

 - rapidJSON                  (https://github.com/miloyip/rapidjson)
     - Note that the problematic JSON license is avoided by excluding the
     jsonchecker which was the the only portion of rapidJSON under that license.
 - librdkafka                 (https://github.com/edenhill/librdkafka)
 - json-simple-1.1.1.jar      (https://code.google.com/p/json-simple/)
 - kafka_2.11-0.10.0.1.jar    (http://kafka.apache.org/)
 - kafka-clients-0.10.0.1.jar (http://kafka.apache.org/)
 - slf4j-api-1.7.21.jar       (http://www.slf4j.org/)
 - slf4j-log4j12-1.7.21.jar   (http://www.slf4j.org/)

Libraries used to build/test packages
------------------------------------

 - googletest                  (https://github.com/google/googletest)
 - cmake                       (http://www.cmake.org)
 - apache ant                  (http://ant.apache.org)

[1]: https://creativecommons.org/publicdomain/zero/1.0/legalcode
