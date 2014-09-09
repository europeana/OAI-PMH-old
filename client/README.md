OAI-PMH Client
==============

This is a client application (harvester) for [OAI-PMH] server.

Introduction
------------
The client was created by [Ontotext] to test and harvest records from the [Europeana] [OAI-PMH] server
and import them into [Ontotext GraphDB][owlim] (former OWLIM) repository.

**Note:** *The client is still prototype and the server is still not public.*
Architecture
------------

The client is (almost) single-threaded - downloading is followed by processing downloaded content. This is to be changed.

A second WatchDog thread was added to prevent hanging when network connection is interrupted. This is a workaround because underlying librarians don't time-out. Main thread calls a `reset()` method after each download to inform the WatchDog it's not hanging.
WatchDog thread wakes-up on fixed interval (currently hard-coded 1 minute) and decrements a counter (currently reset to 10). When 0 is reached, WatchDog calls `System.extit(1)`. The class WatchDog is parametrized, but it's creation is hard-codded.

Configuration
-------------

### client.properties
This is the default name for the main configuration file. It may be changed by setting the system property *config*, passed to *java* command line:

	-Dconfig="/my-place/my-cfg.cfg"

#### server

Link to the OAI-PMH server. Example:

	server=http://sandbox12.isti.cnr.it:8080/oaicat/OAIHandler

#### QueryListRecords

This is the class, that holds a single OAI-PMH query. Setting

	QueryListRecords.from=2013-02-24T12:35:46Z
	QueryListRecords.until=2014-08-12T14:22:27Z
	QueryListRecords.set=04202_L_BE_UniLibGent_googlebooks
	#default metadata prefix is 'edm'
	#QueryListRecords.prefix=edm

If from, until and set are not set, the single query is not executed.

#### processor
One can create own processor for records, or/and navigation pages.
The class list is in the following format:

	processor.1=com.ontotext.process.list.TraceListProcessor
	processor.2=com.ontotext.process.record.DateStats
	processor.3=com.ontotext.process.OwlimUpdater

- No gaps are allowed between numbers. Start index is 1.
- Class names should be fully-qualified and implementing ListProcessor and/or RecordProcessor.
- Classes should have constructor accepting Properties - (client.properties).

### sets.txt
This is a file, that if present in the current directory is read line by line (UTF-8) and for each line is executed one ListRecords query with filter only by set and with fixed metadata prefix 'edm'. Execution is after the single query (if not empty).

[oai-pmh]: <http://www.openarchives.org/OAI/openarchivesprotocol.html> "OAI-PMH 2.0"
[europeana]: <http://www.europeana.eu/> "Europeana"
[ontotext]: <http://www.ontotext.com/> "Ontotext"
[owlim]: <http://www.ontotext.com/products/ontotext-graphdb/> "Ontotext GraphDB"

