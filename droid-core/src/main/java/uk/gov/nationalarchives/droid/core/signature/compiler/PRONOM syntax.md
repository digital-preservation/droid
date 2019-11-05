# PRONOM Syntax
The syntax supported by PRONOM binary and container signatures is described here.

## Syntax
Some syntax here is specific to container signatures.  PRONOM and earlier versions of DROID cannot process this syntax if they appear in binary signatures published by PRONOM.  Container specific syntax is marked in the descriptions below.

### Bytes:  00 FF
Bytes to match are written as two digit, case insensitive hex values.
```
ff Fe A1 00
```

### Strings:  'ISO-8859-1'
**Container signature syntax**
Strings of ISO-8859-1 characters can be written enclosed in single quotation marks.
```
'version:4'
```

A single character enclosed in single quotes can also be used as in place of a byte value:
```

30 '0'   # These both encode the character zero, one is a hex byte, the other is the character directly.
```

## Byte ranges: \[n:m]
To match a byte within a range of values, we can use a byte range. This is written as two byte values separated with a colon, all enclosed in square brackets.  Bytes are specified as 2 digit hex values.
```
[30:39]
```

**Container signature syntax**
Using a hyphen is also permitted in range separators for container signatures, and you can specify the range using single character string literals:
```
[30-39]
['0'-'9']
```

## Inverted ranges: \[!n:m]
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

### Any byte ??
To match any byte, we can use two question marks:
```
01 02 ?? 04
```

### Fixed gaps {n}
To specify that there's a gap of one or more bytes we don't care about, we can write the size of the gap as a decimal number, surrounded by curly brackets:
```
01 02 03 {128} FF FE
```

### Variable gaps {n-m}
To specify that there's a range of possible bytes we don't care about, we can write the range as two numbers separted by a hyphen, surrounded by curly brackets:
```
01 02 03 {128-256} FF FE
```

### Unlimited gaps *
To specify that there's a unlimited gap between two parts of an expression (subject to how far DROID is configured to actually scan or the end of the data, whichever comes first), we can write an asterisk:
```
30 31 23 33 4E * 43 2A B1 D4 CC EF
```

### Minimum to unlimited gaps {n-*}
To specify that there's a minimum gap which must exist, but then it's unlimited after that, we can use a number and an asterisk separated by a hyphen, enclosed in curly brackets:
```
01 02 03 {128-*} FF FE
```

### Whitespace
**Container signature syntax**
All whitespace (space, tab, newline, carriage return) between elements is ignored when parsing.  You could write a single expression as:

```
00 01
'version:' ??
FE FF
```

