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

## Building DROID

DROID can be built from source using Maven. The source code can be obtained from the Github repository at [https://github.com/digital-preservation/droid](https://github.com/digital-preservation/droid)
   
Once the code is cloned into a folder (e.g. `droid`), executing `mvn clean install` inside it will build the code. After a successful build, two archives are provided inside the `droid-binary/target` folder.

### Linux / OSX / Windows users
Archive `droid-binary-${VERSION}-bin-unix.zip`

You will need JAVA 9 to 11 installed to run it.

Once unpacked, use the `droid.sh` or `droid.bat` script to run the application.

### Windows users
Archive  `droid-binary-${VERSION}-bin-win32-with-jre.zip`

For Windows users who might not be able to install JAVA, the provided bundle includes JAVA 11.

Once unpacked, use the `droid.bat` script to run the application.

## Signatures
Since version 6.5, DROID adds some new capabilities to support developing and testing signatures.

[Signature syntax](Signature%20syntax.md) provides details on the types of signatures and regular expression syntax supported by DROID.

### sigtool
To aid work on signatures, we provide `sigtool`, packaged with DROID. `sigtool` is a simple command line application which can:

 * test binary or container signatures directly against files.
 * generate signature XML from binary or container signatures.
 * convert signatures between the original (binary) and the newer (container) syntax.
 * produce summaries of signature XML files, converting the XML back into signatures.
 * convert standard XML signature files into a simpler format, which uses the signatures directly.

More details are provided in [Sigtool's user guide](droid-binary/bin/Using%20sigtool.txt) .

### Simpler signature XML 
Since version 6.5, DROID can compile signatures itself, without needing a full XML specification. Inside current signature files, the actual sequences to match are specified in various sub-elements and attributes of `<ByteSequence>` elements. 

For example, the signature `{10-1024} 01 02 03 04 05 [00:30] * 01 02 03` is represented in signature XML by:

  ```xml
<ByteSequence Reference="BOFoffset">
    <SubSequence SubSeqMinOffset="10" SubSeqMaxOffset="1024">
        <Sequence>01 02 03 04 05</Sequence>
        <RightFragment MaxOffset="0" MinOffset="0" Position="1">[00:30]</RightFragment>
    </SubSequence>
    <SubSequence>
        <Sequence>01 02 03</Sequence>
    </SubSequence>
</ByteSequence>
```

DROID can now put a signature directly inside a `Sequence` attribute on the `<ByteSequence>` element, with no further XML required.  For example, the signature above can be simply written as:

```xml
<ByteSequence Reference="BOFoffset" Sequence="{10-1024} 01 02 03 04 05 [00:30] * 01 02 03" />
```

The full syntax can be used in either binary or container signature files.


