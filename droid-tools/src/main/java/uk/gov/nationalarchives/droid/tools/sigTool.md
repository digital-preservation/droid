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
sigTool --expression --binary "'Microsoft Word'"
```
Gives the tab delimited output of the original expression and its conversion to binary signature syntax:
```
'Microsoft Word'	4D6963726F736F667420576F7264
```
Conversely, if we ran:
```
sigTool --expression --container "4D6963726F736F667420576F7264"
```
We would get this:
```

```