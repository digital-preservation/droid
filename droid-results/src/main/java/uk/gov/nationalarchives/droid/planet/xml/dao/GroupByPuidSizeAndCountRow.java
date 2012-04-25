/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
