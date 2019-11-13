# sigTool

sigTool can convert:

* signatures into signature XML
* signatures between binary and container syntax
* signature XML files into new signature XML files using the new syntax.
* signature XML files into a tab-delimited signature summary using the new syntax.

## Usage
To use sigTool:

```
sigTool [Options] {expressions}
```

### Options
The options control how signatures are processed, or print help.

| Short | Long         | Description                                                                                                                                                                                         |
|-------|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -h    | --help       | Prints sigTool help.                                                                                                                                                                                |
| -d    | --droid      | Compile signatures for DROID. This can mean longer sequences to search for, which is usually faster. (Default)                                                                                      |
| -p    | --pronom     | Compile signatures for PRONOM. PRONOM only allows bytes in the main search sequences.                                                                                                               |
| -b    | --binary     | Render expressions as closely as possible to binary signature format. This attempts to make signatures compatible with older versions of DROID.                                                     |
| -c    | --container  | Render expressions using the full container signature syntax.   This is more powerful and readable, but is not compatible with versions of DROID that don't support container signatures. (Default) |
| -s    | --space      | Render spaces between elements for greater readability.  Older versions of DROID don't support whitespace.                                                                                          |
| -x    | --xml        | Output an xml representation of an expression, or a transformed signature file using the new syntax. (Default)                                                                                      |
| -e    | --expression | Output a tab delimited format containing PRONOM syntax compiled from another expression or signature file.                                                                                          |
| -f    | --file       | Specify a signature file to process.  The next argument is the filename of the signature file.  Both binary and container signatures files can be specified.                                        |
| -a    | --anchor     | Specify whether an expression is anchored to BOFoffset, EOFoffset or Variable.  For example: "--offset bofoffset"                                                                                   |
| -n    | --notabs     | Don't output tab delimited metadata along with a compiled expression - just output the result of compiling on its own.                                                                              |


### Expressions
Expressions are PRONOM syntax regular expressions we want to convert. For example, two expressions are given in the command below:

```
sigTool "01 02 03 (B1 B2 | C1 C2)" "'start:'(22|27)[01:2F]"
```

These aren't required if we're processing a signature file (the -f option).

## Convert signatures into signature file XML
To convert a PRONOM regular expression signature into its signature file XML, just run sigTool with one or more expressions:
```
sigTool "01 02 03 (B1 B2 | C1 C2)" "'start:'(22|27)[01:2F]"
```
This gives a tab delimited output consisting of each expression, a tab, and the signature XML for the expression:
```
01 02 03 (B1 B2 | C1 C2)	<ByteSequence Reference="BOFoffset"><SubSequence Position="1" SubSeqMaxOffset="0" SubSeqMinOffset="0"><Sequence>010203</Sequence><RightFragment MaxOffset="0" MinOffset="0" Position="1">B1B2</RightFragment><RightFragment MaxOffset="0" MinOffset="0" Position="1">C1C2</RightFragment></SubSequence></ByteSequence>
'start:'(22|27)[01:2F]	<ByteSequence Reference="BOFoffset"><SubSequence Position="1" SubSeqMaxOffset="0" SubSeqMinOffset="0"><Sequence>'start:'[2227][01:'/']</Sequence></SubSequence></ByteSequence>
```
Note that the signatures are using container syntax (e.g. they use strings and multi-byte sets).  If we wanted a more backwards compatible binary signature, we can specify the --binary option:
```
sigTool --binary "01 02 03 (B1 B2 | C1 C2)" "'start:'(22|27)[01:2F]"
```
This gives a more backwards compatible output, where strings are represented as hex byte sequences:
```
01 02 03 (B1 B2 | C1 C2)	<ByteSequence Reference="BOFoffset"><SubSequence Position="1" SubSeqMaxOffset="0" SubSeqMinOffset="0"><Sequence>010203</Sequence><RightFragment MaxOffset="0" MinOffset="0" Position="1">B1B2</RightFragment><RightFragment MaxOffset="0" MinOffset="0" Position="1">C1C2</RightFragment></SubSequence></ByteSequence>
'start:'(22|27)[01:2F]	<ByteSequence Reference="BOFoffset"><SubSequence Position="1" SubSeqMaxOffset="0" SubSeqMinOffset="0"><Sequence>73746172743A(22|27)[01:2F]</Sequence></SubSequence></ByteSequence>
```

## Convert between signature syntax
Signatures can use different syntax depending on whether they're a traditional binary signature, or a container signature.  You can convert between these formats using the --expression option.  For example, running:
```
sigTool --expression --binary "'Microsoft Word'" --notabs
```
Gives the tab delimited output of the original expression and its conversion to binary signature syntax:
```
'Microsoft Word'	4D6963726F736F667420576F7264
```
Conversely, if we ran:
```
sigTool --expression --container "65617369657220746F207265616420696E20636F6E7461696E65722073796E746178" --notabs
```
We would get this:
```
'easier to read in container syntax'
```

## Transform a signature file to use PRONOM syntax
You can read an existing binary or container signature file, and output a new signature file that strips out all the old XML objects under the ByteSequence element, and replaces it with a compiled PRONOM syntax expression in the "Sequence" attribute of each ByteSequence.

For example, if you ran:
```
sigTool --file "DROID_SignatureFile_V91.xml"
```
It would write a new XML file to the console.  You can pipe this into a new file using your shell.  The signature file will be transformed to use the new syntax, e.g.:
```
   ...
   <InternalSignature ID="18" Specificity="Specific">
      <ByteSequence Reference="BOFoffset" Sequence="'GIF87a'"/>
      <ByteSequence Reference="EOFoffset" Sequence="3B{0-4}"/>
   </InternalSignature>
   <InternalSignature ID="20" Specificity="Specific">
      <ByteSequence Reference="BOFoffset" Sequence="'%PDF-1.4'"/>
      <ByteSequence Reference="EOFoffset" Sequence="'%%EOF'{0-1024}"/>
   </InternalSignature>
   ...
```
You can see that it's transformed a binary signature file using container syntax (the default).  If we wanted to use the older binary syntax for this transformation, we could write:
```
sigTool --binary --file "DROID_SignatureFile_V91.xml"
```
It would give this output (snippet):
```
   ...
   <InternalSignature ID="18" Specificity="Specific">
      <ByteSequence Reference="BOFoffset" Sequence="474946383761"/>
      <ByteSequence Reference="EOFoffset" Sequence="3B{0-4}"/>
   </InternalSignature>
   <InternalSignature ID="20" Specificity="Specific">
      <ByteSequence Reference="BOFoffset" Sequence="255044462D312E34"/>
      <ByteSequence Reference="EOFoffset" Sequence="2525454F46{0-1024}"/>
   </InternalSignature>
   ...
```

## Summarise all signatures in a syntax file.
You can obtain a summary of all signatures in a signature file into a tab-delimited format, by specifying the --expression option.  For example, running:
```
sigTool --expression --file "DROID_SignatureFile_V91.xml"
```
Would give the following output (snippet):
```
Version	Sig ID	Reference	Sequence
91	    18	    EOFoffset	3B{0-4}
91	    20	    BOFoffset	'%PDF-1.4'
91	    20	    EOFoffset	'%%EOF'{0-1024}
91	    21	    BOFoffset	'%PDF-1.6'
91	    21	    EOFoffset	'%%EOF'{0-1024}
91	    22	    BOFoffset	'%PDF-1.5'
91	    22	    EOFoffset	'%%EOF'{0-1024}
91	    23	    BOFoffset	'%PDF-1.3'
91	    23	    EOFoffset	'%%EOF'{0-1024}
```
You can also specify --binary signature syntax or use --container syntax (the default).  This also works for container files, where you get a little more metadata:
```
sigTool --expression --file "container-signature-20170330.xml"
```
Gives the following output (snippet):
```
Description	                        Sig ID	Container File	    Internal Sig ID	Reference	Sequence
Microsoft Word 6.0/95 OLE2	        1000	CompObj     	    306	            BOFoffset	{40-1024}10000000'Word.Document.'['6':'7']00
Microsoft Word 97 OLE2	            1020	CompObj	            300	            BOFoffset	{40-1024}10000000'Word.Document.8'00
Microsoft Word OOXML	            1030	[Content_Types].xml	302	            BOFoffset	{0-32768}'ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"'
Microsoft Word Macro enabled OOXML	1060	[Content_Types].xml	1060	        Variable	{0-4096}'ContentType="application/vnd.ms-word.document.macroEnabled.main+xml"'
Microsoft Word OOXML Template	    1070	[Content_Types].xml	1070	        Variable	{0-4096}'ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.template.main+xml"'
Microsoft Word 97 OLE2 Template	    1100	CompObj	            1100	        BOFoffset	{40-1024}10000000'Word.Document.8'00
Microsoft Word 97 OLE2 Template	    1100	WordDocument	    1100	        BOFoffset	{10}[&01]
Microsoft Excel OOXML	            2030	[Content_Types].xml 317	            BOFoffset	{0-40000}'ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"'
...
```