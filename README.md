# DROID (Digital Record Object Identification)  

[![CI](https://github.com/digital-preservation/droid/workflows/CI/badge.svg)](https://github.com/digital-preservation/droid/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/digital-preservation/droid/badge.svg?branch=master)](https://coveralls.io/github/digital-preservation/droid?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.gov.nationalarchives/droid/badge.svg)](https://search.maven.org/search?q=g:uk.gov.nationalarchives)

## General Information

DROID is a software tool developed by [The National Archives](http://www.nationalarchives.gov.uk/ "The National Archives Website") to perform automated batch identification of file formats. Developed by its Digital Preservation Department as part of its broader digital preservation activities, DROID is designed to meet the fundamental requirement of any digital repository to be able to identify the precise format of all stored digital objects, and to link that identification to a central registry of technical information about that format and its dependencies.

DROID uses internal signatures to identify and report the specific file format versions of digital files. These signatures are stored in an XML signature file, generated from information recorded in the [PRONOM technical registry](http://www.nationalarchives.gov.uk/PRONOM/Default.aspx "PRONOM Technical Registry"). New and updated signatures are regularly added to PRONOM, and DROID can be configured to automatically download updated signature files.

DROID is a platform-independent Java application. It can be invoked from two interfaces:

* Java Swing GUI
* Command line interface

DROID allows files and folders to be selected from a file system for identification. This file list can be saved at any point. After the identification process had been run, the results can be output in various report formats, including CSV.

DROID is made available under the [New BSD License](https://raw.github.com/digital-preservation/droid/master/license.md).

More information can be found on the [DROID github pages](https://digital-preservation.github.io/droid/).

## Installing DROID

The latest binary file can be downloaded from [The National Archives website](https://www.nationalarchives.gov.uk/information-management/manage-information/preserving-digital-records/droid/ "The National Archives website").

## Running DROID

DROID can be run either as a graphical desktop application (GUI) or as a command line tool (CLI). GUI usage is explained in the [DROID User Guide](https://cdn.nationalarchives.gov.uk/documents/information-management/droid-user-guide.pdf).

### Basic command line examples

Assuming the `droid` executable (the `droid.sh` or `droid.bat` file) is in your PATH, run one of the following two commands to identify the file `file.jpg` or the directory `directory` (recursively, thanks to the `-R`, or `--recurse` switch), respectively:

~~~console
$ droid file.jpg
"ID","PARENT_ID","URI","FILE_PATH","NAME","METHOD","STATUS","SIZE","TYPE","EXT","LAST_MODIFIED","EXTENSION_MISMATCH","HASH","FORMAT_COUNT","PUID","MIME_TYPE","FORMAT_NAME","FORMAT_VERSION"
"1","","file:/home/user/file.jpg","/home/user/file.jpg","file.jpg","Signature","Done","1689552","File","jpg","2017-11-21T06:52:50","false","","1","fmt/1507","image/jpeg","Exchangeable Image File Format (Compressed)","2.3.x"

$ droid -R directory
...
~~~

If you don't need every bit of information that DROID outputs by default you can make it print only a limited set of columns using the `-co`, or `--columns` option (note the `-a`, it's required in this case!):

~~~console
$ droid -co NAME PUID -a file.jpg
"NAME","PUID"
"file.jpg","fmt/1507"
~~~

To remove even more noise from the output you can use the `-qc`, or `--quote-commas` switch which tells DROID to use quotes only where necessary:

~~~console
$ droid -qc -co NAME PUID -a file.jpg
NAME,PUID
file.jpg,fmt/1507
~~~

Beyond these basic invocations there are many additional options for advanced tweaking of DROID's operation. To learn about them run:

~~~console
$ droid --help
~~~

### Managing signature files in the command line interface

It's possible to update and switch signature files on the command line. Use `-c`, or `--check-signature-update` to check for updates; use `-d`, or `--download-signature-update` to download (and use) the latest signature files:

~~~console
$ droid -c
Container signature update Version 20231127 is available
Binary signature update Version 116 is available

$ droid -d
Signature update version 20.231.127 has been downloaded
Signature update version 116 has been downloaded
~~~

Use `-x`, or `--display-signature-file` to see the signature files that DROID currently uses for identification; use `-X`, or `--list-signature-files` to see all locally available signature files (i.e., all files that you have installed on your machine including those that are currently not in use):

~~~console
$ droid -x
Type: Container Version:  20231127  File name: container-signature-20231127.xml
Type: Binary Version:  116  File name: DROID_SignatureFile_V116.xml

$ droid -X
Type: Binary Version:  114  File name: DROID_SignatureFile_V114.xml
Type: Binary Version:  116  File name: DROID_SignatureFile_V116.xml
Type: Container Version:  20230822  File name: container-signature-20230822.xml
Type: Container Version:  20231127  File name: container-signature-20231127.xml
~~~

If for some reason you want to use another, possibly older signature file for identification you can change the default with the `-s`, or `--set-signature-file` option (remember to switch back to the latest version when you are done!):

~~~console
$ droid -s 114
Default signature file updated. Version: 114  File name: DROID_SignatureFile_V114.xml

$ droid -s 20230822
Default signature file updated. Version: 20230822  File name: container-signature-20230822.xml
~~~

## Building DROID

DROID can be built from source using Maven. The source code can be obtained from the Github repository at [https://github.com/digital-preservation/droid](https://github.com/digital-preservation/droid)
   
Once the code is cloned into a folder (e.g. `droid`), executing `mvn clean install` inside it will build the code. After a successful build, two archives are provided inside the `droid-binary/target` folder.

### Linux / OSX users

You will need JAVA 8 to 17 installed to run DROID.

Unpack the archive `droid-binary-${VERSION}-bin.zip`, then use the `droid.sh` script to run the application.

### Windows users
Archive  `droid-binary-${VERSION}-bin-win64-with-jre.zip`


You will need JAVA 8 to 17 installed to run DROID. For Windows users who might not be able to install JAVA, the provided bundle includes JAVA 17.

Unpack the archive `droid-binary-${VERSION}-bin-win64-with-jre.zip`, then use the `droid.bat` script to run the application.

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

## Developer related guidance

For any detais about contributing, testing, releasing, please check the [wiki](https://github.com/digital-preservation/droid/wiki)
