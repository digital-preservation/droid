/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.core.interfaces;

/**
 * @author a-mpalmer
 *
 */

public enum TextEncoding {
    
    /** US-ASCII.  */
    USASCII("US-ASCII", "x-chr/1", "US English encoded text."),
    
    /** UTF-8. */
    UTF8("UTF-8", "chr/1", "Unicode variable byte encoded text."),

    /** UTF-16BE Big Endian. */
    UTF16BE("UTF-16BE", "chr/2", "Unicode double byte encoded text, big endian."),

    /** UTF-16LE Little Endian. */
    UTF16LE("UTF-16LE", "chr/3", "Unicode double byte encoded text, little endian."),

    /** UTF-32BE Big Endian. */
    UTF32BE("UTF-32BE", "chr/4", "Unicode four byte encoded text, big endian."),

    /** UTF-32LE Little Endian. */
    UTF32LE("UTF-32LE", "chr/5", "Unicode four byte encoded text, little endian."),
   
    /** SHIFT-JIS. */
    SHIFT_JIS("SHIFT_JIS", "chr/6", "Shift_JIS encoded Japanese text."),
    
    /** ISO-2022-JP. */
    ISO2022JP("ISO-2022-JP", "chr/7", "ISO-2022-JP encoded Japanese text."),
    
    /** ISO-2022-CN. */
    ISO2022CN("ISO-2022-CN", "chr/8", "ISO-2022-CN encoded simplified Chinese text."),
    
    /** ISO-2022-KR. */
    ISO2022KR("ISO-2022-KR", "chr/9", "ISO-2022-KR encoded Korean text."),
    
    /** GB18030. */
    GB18030("GB18030", "chr/10", "GB18030 encoded Chinese text."),
    
    /** EUC-JP. */
    EUCJP("EUC-JP", "chr/11", "EUC-JP encoded Japanese text."),
    
    /** EUC-KR. */
    EUCKR("EUC-KR", "chr/12", "EUC-KR encoded Korean text."),
    
    /** Big5. */
    Big5("Big5", "chr/13", "Big5 encoded Chinese text."),
    
    /** ISO-8859-1. */
    ISO88591("ISO-8859-1", "chr/14", "ISO-8859-1 encoded text for the Danish, Dutch, English,"
            + "French, German, Italian, Norwegian, Portugese, Swedish languages."),
    
    /** ISO-8859-2. */
    ISO88592("ISO-8859-2", "chr/15", "ISO-8859-2 encoded text for the Czech, Hungarian, Polish, Romanian languages."),

    /** ISO-8859-5. */
    ISO88595("ISO-8859-5", "chr/16", "ISO-8859-5 encoded Russian text."),

    /** ISO-8859-6. */
    ISO88596("ISO-8859-6", "chr/17", "ISO-8859-6 encoded Arabic text."),

    /** ISO-8859-7. */
    ISO88597("ISO-8859-7", "chr/18", "ISO-8859-7 encoded Greek text."),

    /** ISO-8859-8. */
    ISO88598("ISO-8859-8", "chr/19", "ISO-8859-8 encoded Hebrew text."),

    /** ISO-8859-9. */
    ISO88599("ISO-8859-9", "chr/23", "ISO-8859-9 encoded Turkish text."),
    
    /** Windows-1251. */
    Windows1251("Windows-1251", "chr/20", "Windows-1251 encoded Russian text."),
    
    /** Windows-1256. */
    Windows1256("Windows-1256", "chr/21", "Windows-1251 encoded Arabic text."),
    
    /** KOI8-R. */
    KOI8R("KOI8-R", "chr/22", "KOI8-R encoded Russian text."),

    /** IBM424_rtl. */
    IBM424rtl("IBM424_rtl", "chr/24", "EBCDIC IBM424 code page encoded Hebrew text, right-to-left."),
    
    /** IBM424_ltr. */
    IBM424ltr("IBM424_ltr", "chr/25", "EBCDIC IBM424 code page encoded Hebrew text, left-to-right."),
    
    /** IBM420_rtl. */
    IBM420rtl("IBM420_rtl", "chr/26", "EBCDIC IBM420 code page encoded Arabic text, right-to-left."),
    
    /** IBM420_ltr. */
    IBM420ltr("IBM420_ltr", "chr/27", "EBCDIC IBM420 code page encoded Arabic text, left-to-right.");
    
    
    private String encoding;
    private String puid;
    private String encodingDescription;
    
    
    /**
     * Constructor for TextEncoding  
     * @param encoding The text encoding in an international standard form.
     * @param puid The PRONOM puid for this text encoding.
     * @param encodingDescription - a description for the text encoding.
     */
    TextEncoding(String encoding, String puid, String encodingDescription) {
        this.encoding = encoding;
        this.puid = puid;
        this.encodingDescription = encodingDescription;
    }

    /**
     * @return the id
     */
    public long getId() {
        return ordinal();
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @return the puid
     */
    public String getPuid() {
        return puid;
    }
    
    
    /**
     * @return the encoding Description
     */
    public String getEncodingDescription() {
        return encodingDescription;
    }
    
}

