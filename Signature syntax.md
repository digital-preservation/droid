# Signature syntax

## Types
There are three basic types of signature in DROID:

  * file extensions (least reliable, just matches the file extension of a file)
  * binary signatures (very reliable, looks for complex patterns inside a file)
  * container signatures (extremely reliable - looks for internal files and patterns inside them) 

Binary signatures are compiled by the PRONOM registry into binary signature XML for publication to DROID.  Container signatures cannot currently be compiled by PRONOM into signature XML files.  This can make developing container signatures harder than binary signatures, as the signature file XML is hand written (see `sigtool` description in [README](README.md#sigtool) for details of a tool which can produce this XML automatically).

## Syntax
The signature syntax compiled into XML by PRONOM is the original syntax defined for DROID, and should be backwards compatible with all versions of DROID.  This syntax is used in binary signature files.

Container signatures support a wider syntax than binary signatures, for example allowing whitespace and ASCII strings. For the most part, this extended syntax just makes the signatures more human readable.  In a few cases some new capabilities are supported that PRONOM can't currently compile for binary signatures.

All the syntax can be used in either binary or container signatures in DROID itself, but PRONOM won't be able to compile container syntax into XML for binary signatures if you want to submit them to TNA.

Container specific syntax is marked in the descriptions below.

### Value matching
The following syntax can be used to match bytes.

#### Bytes:  00 FF
Bytes to match are written as two-digit, case-insensitive hex values.
```
ff Fe A1 00
```

#### Strings:  'Latin1'
**Container signature syntax**
Strings of ISO-8859-1 characters (Latin1) can be written enclosed in single quotation marks.
```
'version:4'
```

A single character enclosed in single quotes can also be used as in place of a byte value:
```

30 '0'   # These both encode the character zero, one is a hex byte, the other is the character directly.
```
_Note:_ due to a bug in the byteseek library, only ASCII characters are currently supported by DROID.  Characters over 127 may cause incorrect compilation, as it attempts to render the characters in the system default character set, which is probably not ISO-8859-1.

#### Byte ranges: \[n:m]
To match a byte within a range of values, we can use a byte range. This is written as two byte values separated with a colon, all enclosed in square brackets.  Bytes are specified as 2-digit hex values.
```
[30:39]
```

**Container signature syntax**
Using a hyphen is also permitted in range separators for container signatures, and you can specify the range using single character string literals:
```
[30-39]
['0'-'9']
```

#### Inverted ranges: \[!n:m]
To match a byte which is outside a range of values, we can invert a byte range, by placing an exclamation mark immediately after the first square bracket.
```
[!61:7a]
```
**Container signature syntax**
Using a hyphen is also permitted in inverted range separators for container signatures, and you can specify the range using single character string literals:
```
[!61-7a]
[!'a'-'z']
```

#### Bitmask: \[&FF]
To match a particular set of bits in a byte, we can use a bitmask, which is a hex value.  It will match all bytes which have the same bits set to one in the bitmask.  For example, to match all the bytes which have the first bit set to one, we could write:
```
[&01]
```
And to match all the bytes without the first bit set, we can invert it like this:
```
[!&01]
```
This will match all the bytes which have the 8th or 4th bit set (10001000):
```
[&88]
```
_Note:_ the bitmask is not a standard part of the original PRONOM syntax; it originates in the byteseek matching library used by DROID.  However, it is now being used in both binary and container signatures, although older versions of DROID won't be able to parse it in binary signatures.

### Alternatives: (00|F0|3C)
If there is a set of different values that can match, they can be specified inside round brackets, with the alternatives separated by the `|` character.
```
(00|F0|3C)
```
Alternatives can be longer than just a single byte. In this example, three byte sequences could be matched. Alternative sequences don't have to be the same length as each other:
```
(00 01 | B0 B1 B2 | C0 C1 C2 C3)
```
**Container signature syntax**
Older binary signatures should only use hex bytes inside alternatives.  Container syntax supports any byte matching values inside an alternative, except another nested set of alternatives.
```
('start' | 'end' | 01 FF 32 'EOF' [30:39])
```

#### Multi-byte sets: (00|C2|DE) or \[00 C2 DE]
The standard PRONOM binary syntax only supports multi-byte sets by specifying them as a set of alternative bytes. For example:
```
(00|C2|DE)
```

**Container signature syntax**
Multi-byte sets let you specify any set of bytes within square brackets.  For example:
```
[00 C2 DE]
```
They can be inverted just like ranges (which the alternatives syntax doesn't support):
```
[!00 C2 DE]
```
It can also include ranges or any other value matching syntax within the set:
```
[00 C2 DE 'A'-'Z' &01]
```
Strings can be used to specify particular byte values. Note that the string itself doesn't match - the set matches any of the characters in the string:
```
['A'-'Z' 'aeiou']
```

### Wildcards
The following syntax can be used to specify sequences of unknown bytes, including variable or unlimited ranges of unknown bytes which cause DROID to search for the next part of the expression.

#### Any byte ??
To match any byte, we can use two question marks:
```
01 02 ?? 04
```

#### Unlimited gaps *
To specify that there's an unlimited gap between two parts of an expression (subject to how far DROID is configured to actually scan or the end of the data, whichever comes first), we can add an asterisk.

The following expression will first match _30 31 23 33 4E_, and it will then search for _43 2A B1 D4 CC EF_ until the end of the data:
```
30 31 23 33 4E * 43 2A B1 D4 CC EF
```

#### Fixed gaps {n}
To specify that there's a gap of one or more bytes we don't care about, we can write the size of the gap as a decimal number, surrounded by curly braces:
```
01 02 03 {128} FF FE
```

Note that writing _{1}_ is equivalent to writing _??_.

#### Variable gaps {n-m}
To specify that there's a range of possible bytes we don't care about, we can write the range as two numbers separated by a hyphen, surrounded by curly braces:
```
01 02 03 {128-256} FF FE
```

#### Min to many gaps {n-*}
To specify there must be a minimum gap, but after that it could be unlimited, we can write:
```
01 02 03 {128-*} FF FE
```
This is equivalent to writing a fixed gap followed by an unlimited gap:
```
01 02 03 {128} * FF FE
```

#### Whitespace
**Container signature syntax**
All whitespace (space, tab, newline, carriage return) between elements is ignored when parsing.  You could write a single expression as:

```
00 01
'version:' ??
FE FF
```

