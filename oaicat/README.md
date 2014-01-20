OAICat
======

**NOTE**: This is a copy of the [original OAICat](http://www.oclc.org/research/activities/oaicat.html) version 1.5.61 with maven build support.

The OAICat Open Source Software (OSS) project is a Java Servlet web application providing a repository framework that conforms to the Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH) v2.0. This framework can be customized to work with arbitrary data repositories by implementing some Java interfaces. Demonstration implementations of these interfaces are included in the webapp distribution. For information about the OAICat-based software that allows museums to disclose descriptions of collection items and pointers to digital surrogates, see the Museum Data Exchange page.

## Background
OAICat was originally written to provide OAI-PMH harvesting access to theses and dissertations extracted from WorldCat as well as the union catalog of the Networked Digital Library of Theses and Dissertations (NDLTD).

## Impact
OAICat was written as open source and includes a number of abstractions that allow it to be customized and configured for use with a variety of data sources. For example, OAICat was customized for, and is included in, the DSpace distribution. According to the UIUC OAI-PMH registry, OAICat is used in 468 of 2242 known OAI-PMH repositories (See http://oai.grainger.uiuc.edu/registry/ListToolkits.asp).

Experience has shown the vital importance of collaboratively-developed conceptual jargon and the need for parsing the subtleties of formal specifications. Mapping this jargon as literally as possible to object-oriented code makes it easier for others to comprehend, which is important as OAICat is more of a framework than a standalone Web application.

## Details
OAICat was designed to support the full range of OAI-PMH v2.0 functionality with minimal assumptions about the environment and storage mechanisms in which it would be used. This was done by adopting a DAO-like interface for accessing “items” and using bean-like configuration to customize the framework.

Generalization was further enhanced by modeling the Java classes and methods in accord with the jargon of OAI-PMH to help software developers comprehend the application and anticipate where classes and methods needed to be customized.

## License

This software may be used without charge in accord with the terms of the Apache License, Version 2.0.
