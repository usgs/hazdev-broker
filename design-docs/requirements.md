# hazdev-broker Small Rapid Data Distribution System Requirements
Last Updated August 11, 2016

A system is needed to distribute small (often unassociated data) such as
near-real-time picks, beam back azimuth/slowness, correlation detections, glass
detections, and other appropriate messages. This data is relatively small in
size and high in volume compared to earthquake product data distributed by
existing systems such as PDL. This unassociated data is also relatively large
and low in volume compared to waveform data distributed by seedlink and other
methods. While the NEIC (Hydra) has existing methods for distribution of
near-real-time picks, these methods are problematic, unwieldy (UDP and Multicast
broadcast), hard to maintain and configure, and language or platform (Windows)
dependent. Small, unassociated data does not to fit well into any of the
existing NEIC distribution systems. What follows are the identified requirements
for a new unassociated data distribution system.

## Requirements:
* The system should be optimized to distribute small messages of approximately
between 1 to 10kb in size.
* The system should support distributing high volumes of messages, approximately
between 40,000 to 100,000 messages a day. Note the volume levels can be bursty,
in that during the hours following a large earthquake there are significantly
more picks, but there is also  nearly continuous flow of picks on the order of a
handful every few seconds.
* The system should distribute data in a Live / Near-real-time manner, to
support timely event detection. In other words significant system added
latencies (such as polling) should be avoided when possible.  It is conceivable
that given the size and frequency of the messages, that individual messages
(5 arrive within a second) could be combined into a single aggregate message,
based on a configurable threshold
* The system should support a source (or creator/sender) of unassociated data
distributing to many consumers.
* The system should support consumers of unassociated data receiving from
multiple sources (or senders).
The system should be implemented as a library or libraries to support
applications directly acting as either a source/sender of data for the system
(for example, the filter picker) or acting as a consumer of the data (for
example, GLASS or Hydra).  These libraries should be compatible with at least
C/C++ and Java, to support the use of the system both cross-platform and
cross-language.
* The system should use message signatures or some equivalent method for
authentication, with optional verification, to ensure messages are not modified
between when they are sent and received.

## Desired Additional Features:
* If possible, the system should support backfilling upon reconnection, i.e.
when a consumer connects to the source, the consumer can request a start time
for data from a source based on a configurable span of time before the current
time (Example go back 1 day to cover a data outage, etc.).
* If possible the system will be designed to be generic enough to readily
support new kinds of unassociated data/messages and be applicable to distribute
any data that meets these basic size and volume characteristics.
