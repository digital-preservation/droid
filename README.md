DROID (Digital Record Object Identification) 
=====
The latest binary file can be downloaded from [The National Archives website](http://www.nationalarchives.gov.uk/information-management/projects-and-work/droid.htm "The National Archives website").

[![Build Status](https://secure.travis-ci.org/digital-preservation/droid.png)](http://travis-ci.org/digital-preservation/droid)
[![Build status](https://ci.appveyor.com/api/projects/status/hrr6c3ckbghjvd7h/branch/master?svg=true)](https://ci.appveyor.com/project/AdamRetter/droid/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.gov.nationalarchives/droid/badge.svg)](https://search.maven.org/search?q=g:uk.gov.nationalarchives)

More information can be found on the DROID github pages here: http://digital-preservation.github.com/droid/

## General Information

DROID is a software tool developed by [The National Archives](http://www.nationalarchives.gov.uk/ "The National Archives Website") to perform automated batch identification of file formats. Developed by its Digital Preservation Department as part of its broader digital preservation activities, DROID is designed to meet the fundamental requirement of any digital repository to be able to identify the precise format of all stored digital objects, and to link that identification to a central registry of technical information about that format and its dependencies.

DROID uses internal signatures to identify and report the specific file format versions of digital files. These signatures are stored in an XML signature file, generated from information recorded in the [PRONOM technical registry](http://www.nationalarchives.gov.uk/PRONOM/Default.aspx "PRONOM Technical Registry"). New and updated signatures are regularly added to PRONOM, and DROID can be configured to automatically download updated signature files.

DROID is a platform-independent Java application. It can be invoked from two interfaces:

* Java Swing GUI
* Command line interface

DROID allows files and folders to be selected from a file system for identification. This file list can be saved at any point. After the identification process had been run, the results can be output in various report formats, including CSV.

DROID is made available under the New BSD License: https://raw.github.com/digital-preservation/droid/master/license.md

## Signatures Management

### New capabilities

Since version 6.5, DROID can compile signatures itself, without needing a full XML specification in either the binary or container signature files. Inside signature files, the actual sequences to match are specified under `<ByteSequence>` elements. For example:

  ```xml
<ByteSequence Reference="BOFoffset">
    <SubSequence SubSeqMinOffset="10" SubSeqMaxOffset="1024">
        <Sequence>0102030405[00:30]</Sequence>
    </SubSequence>
    <SubSequence>
        <Sequence>01 02 03</Sequence>
    </SubSequence>
</ByteSequence>
```

DROID can now simply take a full PRONOM signature inside a new Sequence attribute on the ByteSequence. For example, the signature above could be written as:

```xml
<ByteSequence Reference="BOFoffset" Sequence="{10-1024} 01 02 03 04 05 [00:30] * 01 02 03" />
```

<br/>
<br/>

The full syntax can be used in either binary or container signatures. However, if you use the new syntax, to submit them to TNA and get those signatures included in PRONOM registry, you will need to compile those signatures for PRONOM using Sigtool.

### Sigtool

To work further on signatures, we provide Sigtool, packaged with DROID. Sigtool is a command line interface to convert signatures in different formats, generate reports and test signatures on files.
More details are provided in [Sigtool's user guide](droid-binary/bin/Using sigtool.txt).

[PRONOM Syntax](PRONOM%20syntax.md) also provides details on the regular expression syntax supported by DROID. All of the syntax can be used in either binary or container signatures (but PRONOM won't be able to parse them for binary signatures if you want to submit them to TNA)

## Packaging

DROID can be built simply from source using Maven. Executing `mvn clean install` inside the `droid` folder should be enough. Two archives are provided inside the `droid-binary/target` folder.


#### Linux / OSX / Windows users

Archive `droid-binary-${VERSION}-bin-unix.zip`

You will need JAVA 8 to 11 installed to run it.

once unpacked, use the `droid.sh` or `droid.bat` script to run the application
#### Windows users
Archive  `droid-binary-${VERSION}-bin-win32-with-jre.zip`

For Windows users who might not be able to install JAVA, the provided bundle includes JAVA 11.

once unpacked, use the `droid.bat` script to run the application  
