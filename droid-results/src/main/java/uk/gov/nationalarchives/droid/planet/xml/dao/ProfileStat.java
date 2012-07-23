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
