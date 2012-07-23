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
package uk.gov.nationalarchives.droid.planet.xml.dao;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Encapsulated one row at GroupByYear Size and count report. 
 * @author Alok Kumar Dash
 *
 */
public class GroupByPuidSizeAndCountRow {

    private int year;
    private BigDecimal size;
    private BigInteger count;
    
    private String mimeType = "";
    private String formatName = "";
    private String formatVersion = "";
    
    private String puid;
    
    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }
    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }
    /**
     * @return the size
     */
    public BigDecimal getSize() {
        return size;
    }
    /**
     * @param size the size to set
     */
    public void setSize(BigDecimal size) {
        this.size = size;
    }
    /**
     * @return the count
     */
    public BigInteger getCount() {
        return count;
    }
    /**
     * @param count the count to set
     */
    public void setCount(BigInteger count) {
        this.count = count;
    }
    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }
    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    /**
     * @return the formatName
     */
    public String getFormatName() {
        return formatName;
    }
    /**
     * @param formatName the formatName to set
     */
    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }
    /**
     * @return the formatVersion
     */
    public String getFormatVersion() {
        return formatVersion;
    }
    /**
     * @param formatVersion the formatVersion to set
     */
    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }
    /**
     * @return the puid
     */
    public String getPuid() {
        return puid;
    }
    /**
     * @param puid the puid to set
     */
    public void setPuid(String puid) {
        this.puid = puid;
    }
    
}
