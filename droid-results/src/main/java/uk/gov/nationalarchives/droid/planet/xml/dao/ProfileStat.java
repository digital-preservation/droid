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
import java.util.Date;

/**
 * Profile Statistics.
 * 
 * @author Alok Kumar Dash
 */
public class ProfileStat {

    private Date profileStartDate;
    private Date profileSaveDate;
    private Date profileEndDate;

    private BigDecimal profileMeanSize;
    private BigInteger profileTotalSize;
    private BigInteger profileSmallestSize;
    private BigInteger profileLargestSize;

    private BigInteger profileTotalReadableFiles;
    private BigInteger profileTotalUnReadableFiles;
    private BigInteger profileTotalUnReadableFolders;

    /**
     * @return the profileStartDate
     */
    public Date getProfileStartDate() {
        return profileStartDate;
    }

    /**
     * @param profileStartDate
     *            the profileStartDate to set
     */
    public void setProfileStartDate(Date profileStartDate) {
        this.profileStartDate = profileStartDate;
    }

    /**
     * @return the profileSaveDate
     */
    public Date getProfileSaveDate() {
        return profileSaveDate;
    }

    /**
     * @param profileSaveDate
     *            the profileSaveDate to set
     */
    public void setProfileSaveDate(Date profileSaveDate) {
        this.profileSaveDate = profileSaveDate;
    }

    /**
     * @return the profileEndDate
     */
    public Date getProfileEndDate() {
        return profileEndDate;
    }

    /**
     * @param profileEndDate
     *            the profileEndDate to set
     */
    public void setProfileEndDate(Date profileEndDate) {
        this.profileEndDate = profileEndDate;
    }

    /**
     * @return the profileMeanSize
     */
    public BigDecimal getProfileMeanSize() {
        return profileMeanSize;
    }

    /**
     * @param profileMeanSize
     *            the profileMeanSize to set
     */
    public void setProfileMeanSize(BigDecimal profileMeanSize) {
        this.profileMeanSize = profileMeanSize;
    }

    /**
     * @return the profileTotalSize
     */
    public BigInteger getProfileTotalSize() {
        return profileTotalSize;
    }

    /**
     * @param profileTotalSize
     *            the profileTotalSize to set
     */
    public void setProfileTotalSize(BigInteger profileTotalSize) {
        this.profileTotalSize = profileTotalSize;
    }

    /**
     * @return the profileSmallestSize
     */
    public BigInteger getProfileSmallestSize() {
        return profileSmallestSize;
    }

    /**
     * @param profileSmallestSize
     *            the profileSmallestSize to set
     */
    public void setProfileSmallestSize(BigInteger profileSmallestSize) {
        this.profileSmallestSize = profileSmallestSize;
    }

    /**
     * @return the profileLargestSize
     */
    public BigInteger getProfileLargestSize() {
        return profileLargestSize;
    }

    /**
     * @param profileLargestSize
     *            the profileLargestSize to set
     */
    public void setProfileLargestSize(BigInteger profileLargestSize) {
        this.profileLargestSize = profileLargestSize;
    }

    /**
     * @return the profileTotalReadableFiles
     */
    public BigInteger getProfileTotalReadableFiles() {
        return profileTotalReadableFiles;
    }

    /**
     * @param profileTotalReadableFiles
     *            the profileTotalReadableFiles to set
     */
    public void setProfileTotalReadableFiles(
            BigInteger profileTotalReadableFiles) {
        this.profileTotalReadableFiles = profileTotalReadableFiles;
    }

    /**
     * @return the profileTotalUnReadableFiles
     */
    public BigInteger getProfileTotalUnReadableFiles() {
        return profileTotalUnReadableFiles;
    }

    /**
     * @param profileTotalUnReadableFiles
     *            the profileTotalUnReadableFiles to set
     */
    public void setProfileTotalUnReadableFiles(
            BigInteger profileTotalUnReadableFiles) {
        this.profileTotalUnReadableFiles = profileTotalUnReadableFiles;
    }

    /**
     * @return the profileTotalUnReadableFolders
     */
    public BigInteger getProfileTotalUnReadableFolders() {
        return profileTotalUnReadableFolders;
    }

    /**
     * @param profileTotalUnReadableFolders
     *            the profileTotalUnReadableFolders to set
     */
    public void setProfileTotalUnReadableFolders(
            BigInteger profileTotalUnReadableFolders) {
        this.profileTotalUnReadableFolders = profileTotalUnReadableFolders;
    }

}
