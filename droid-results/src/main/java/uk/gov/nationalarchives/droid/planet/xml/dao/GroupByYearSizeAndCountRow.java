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
public class GroupByYearSizeAndCountRow {

    private int year;
    private BigDecimal size;
    private BigInteger count;
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
    
}
