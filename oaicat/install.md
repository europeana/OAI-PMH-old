Get the source and build
========================

The client and the server are independent but currently share one Git repository.
The name of the server project is still oaicat (the name of the library used), but it may be changed later.

	git clone https://github.com/europeana/OAI-PMH.git
	cd OAI-PMH/oaicat
	mvn install
	cd ../client
	mvn install

Server webapp installation (Tomcat 7)
=====================================

1. Copy the the war file from OAI-PMH/oaicat/target/oaicat.war to tomcat webapps folder and restart tomcat from the root folder:

		bin/catalina.sh stop ; bin/catalina.sh start

2. Edit the configuration file webapps/oaicat/WEB-INF/oaicat.properties to set proper databases. Example:
	
		SolrRegistry.server=http://data2.eanadev.org:9191/solr
		RecordsDb.host=data1.eanadev.org

3. Enable tomcat SSI (Server Side Includes) - Optional
This step is not required. It is just to improve UI - adds header and footer to the HTML pages.

(from tomcat root) 

* edit conf/web.xml 
enable ssi servlet by uncommenting

		<servlet>
		<servlet-name>ssi</servlet-name>
		...
		</servlet>
* Enable *.shtml file extension for SSI processing by uncommenting this fragment: <!-- The mapping for the SSI servlet -->

		<servlet-mapping>
		<servlet-name>ssi</servlet-name>
		<url-pattern>*.shtml</url-pattern>
		</servlet-mapping>
* Give privileged permission to the oaicat web application
Create file conf/Catalina/localhost/oaicat.xml with the following content:

		<?xml version="1.0" encoding="UTF-8"?>
		<Context privileged="true" />
		
4. Restart the tomcat again.

Client usage
============

* Install


Just copy the jar file (OAI-PMH/client/target/Ooai-pmh-client-1.0-SNAPSHOT.jar) and the dependencies (OAI-PMH/client/target/dependency/) to a clean folder.

* Configure


Create client.properties file in the install directory and set the OAI-PMH server. And add some record/recordList processors.Example: 

		server=http://sandbox12.isti.cnr.it:8080/oaicat/OAIHandler
 
		processor.1=com.ontotext.process.list.TraceListProcessor
		processor.2=com.ontotext.process.record.DateStats
		processor.3=com.ontotext.process.OwlimUpdater

* Single query: *client.properites*

Edit client.properties.

		QueryListRecords.from=2013-02-24T12:35:46Z
		QueryListRecords.until=2014-08-12T14:22:27Z
		QueryListRecords.set=04202_L_BE_UniLibGent_googlebooks
		#default metadata prefix is 'edm'
		#QueryListRecords.prefix=edm

* Set-by-set queries: sets.txt

Create file sets.txt in the root directory

* Both query types are run. First is the single query.

* Run

		java -Xms8g -Xmx16g -cp './dependency:oai4jTest-1.0-SNAPSHOT.jar' com.ontotext.Main

Authentication
==============

Set password
------------

To enable authentication, after oaicat server install, edit webapps/oaicat/WEB-INF/web.xml

1. Search for <security-constraint> and comment out the section below.

2. Edit the tomcat configuration file conf/tomcat-users.xml to add role 'harvester' and add it to a user (as described in the comment too).

Enable SSL on Tomcat
--------------------

**Note:** Enabling SSL with untrusted key may block the automated clients. Better skip this step!

For more details look here: [SSL Configuration HOW-TO](http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html)

* Generate untrusted RSA-key

		$JAVA_HOME/bin/keytool -genkey -alias tomcat -keyalg RSA

**Note:** The 'alias' param is unique name for the key.

		root@sandbox12:~# keytool -genkey -alias tomcat -keyalg RSA
		Enter keystore password:
		Re-enter new password:
		What is your first and last name?
		 [Unknown]:  Simo Simov
		What is the name of your organizational unit?
		 [Unknown]:
		What is the name of your organization?
		 [Unknown]:  Ontotext
		What is the name of your City or Locality?
		[Unknown]:  Sofia
		What is the name of your State or Province?
		[Unknown]:
		What is the two-letter country code for this unit?
		[Unknown]:  BG
		Is CN=Simo Simov, OU=Unknown, O=Ontotext, L=Sofia, ST=Unknown, C=BG correct?
		[no]:  yes
		
		Enter key password for <tomcat>
				(RETURN if same as keystore password):
		root@sandbox12:~# ls -al ~/.keystore
		-rw-r--r-- 1 root root 2237 Oct  9 10:39 /root/.keystore

* Edit Tomcat configuration file conf/server.xml

 Uncomment the "SSL HTTP/1.1 Connector" entry (search for "https") and add attributes and values.
 Main attributes: **keystorePass** (default password is "changeit" if not provided), **keyAlias** (="tomcat").
 Other attributes: **keyPass** - if provided on last step, **keystoreFile** - if file is not in default location (~/.keystore).
 More attributes here: [The HTTP Connector Attributes](http://tomcat.apache.org/tomcat-7.0-doc/config/http.html#Common_Attributes)

* Edit webapps/oaicat/WEB-INF/web.xml
 Search for <web-resource-name>Europeana OAI-PMH Server</web-resource-name>


 Change `<transport-guarantee>` (security-constraint/user-data-constraint/transport-guarantee) from 'NONE' to 'CONFIDENTIAL' 
 
		<transport-guarantee>CONFIDENTIAL</transport-guarantee>

to prevent user from sending unencrypted password when logging.

* Restart Tomcat.