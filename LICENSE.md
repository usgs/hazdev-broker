Unless otherwise noted, This software is in the public domain because it
contains materials that originally came from the United States Geological
Survey (USGS), an agency of the United States Department of Interior. For
more information, see the official USGS copyright policy at
http://www.usgs.gov/visual-id/credit_usgs.html#copyright

=======
* The Dependent libraries found in the "\lib" directory are distributed under
the open source (or open source-like) licenses/agreements. Appropriate license
agreements for each library can be found in the "\lib" directory.

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


Disclaimers
-----------
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Information provided by this software may be preliminary or provisional and is
subject to revision. It is being provided to meet the need for timely best
science. The information has not received final approval by the U.S. Geological
Survey (USGS) and is provided on the condition that neither the USGS nor the U.S.
Government shall be held liable for any damages resulting from the authorized or
unauthorized use of the information.
