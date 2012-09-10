DROID (Digital Record Object Identification) 
=====

[![Build Status](https://secure.travis-ci.org/digital-preservation/droid.png)](http://travis-ci.org/digital-preservation/droid)

More information can be found on the DROID github pages here: http://digital-preservation.github.com/droid/

DROID is a software tool developed by [The National Archives](http://www.nationalarchives.gov.uk/ "The National Archives Website") to perform automated batch identification of file formats. Developed by its Digital Preservation Department as part of its broader digital preservation activities, DROID is designed to meet the fundamental requirement of any digital repository to be able to identify the precise format of all stored digital objects, and to link that identification to a central registry of technical information about that format and its dependencies.

DROID uses internal signatures to identify and report the specific file format versions of digital files. These signatures are stored in an XML signature file, generated from information recorded in the [PRONOM technical registry](http://www.nationalarchives.gov.uk/PRONOM/Default.aspx "PRONOM Technical Registry"). New and updated signatures are regularly added to PRONOM, and DROID can be configured to automatically download updated signature files.

DROID is a platform-independent Java application. It can be invoked from two interfaces:

* Java Swing GUI
* Command line interface

DROID allows files and folders to be selected from a file system for identification. This file list can be saved at any point. After the identification process had been run, the results can be output in various report formats, including CSV.

DROID is made available under the New BSD License: https://raw.github.com/digital-preservation/droid/master/license.md

DROID can be built simply from source using Maven. Executing "mvn clean install" inside the droid folder should be enough. The end result is available inside the droid-binary/target folder.
