/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.03.22 at 11:40:59 AM GMT 
//


package uk.gov.nationalarchives.droid.report.planets.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for FileProfileType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FileProfileType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="profilingStartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="profilingEndDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="profilingSaveDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="totalReadableFiles" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *         &lt;element name="totalUnreadableFiles" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *         &lt;element name="totalUnreadableFolders" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *         &lt;element name="totalSize" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="smallestSize" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="largestSize" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="meanSize" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="pathsProcessed" type="{http://www.nationalarchives.gov.uk/CollectionProfile}PathsProcessedType"/&gt;
 *         &lt;element name="byYear" type="{http://www.nationalarchives.gov.uk/CollectionProfile}ByYearType"/&gt;
 *         &lt;element name="byFormat" type="{http://www.nationalarchives.gov.uk/CollectionProfile}ByFormatType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * @deprecated PLANETS XML is now generated using XSLT over normal report xml files. 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileProfileType", propOrder = {
    "profilingStartDate",
    "profilingEndDate",
    "profilingSaveDate",
    "totalReadableFiles",
    "totalUnreadableFiles",
    "totalUnreadableFolders",
    "totalSize",
    "smallestSize",
    "largestSize",
    "meanSize",
    "pathsProcessed",
    "byYear",
    "byFormat"
})
@Deprecated
public class FileProfileType {

    @XmlElement(required = true)
    protected XMLGregorianCalendar profilingStartDate;
    @XmlElement(required = true)
    protected XMLGregorianCalendar profilingEndDate;
    @XmlElement(required = true)
    protected XMLGregorianCalendar profilingSaveDate;
    @XmlElement(required = true)
    protected BigInteger totalReadableFiles;
    @XmlElement(required = true)
    protected BigInteger totalUnreadableFiles;
    @XmlElement(required = true)
    protected BigInteger totalUnreadableFolders;
    @XmlElement(required = true)
    protected BigDecimal totalSize;
    @XmlElement(required = true)
    protected BigDecimal smallestSize;
    @XmlElement(required = true)
    protected BigDecimal largestSize;
    @XmlElement(required = true)
    protected BigDecimal meanSize;
    @XmlElement(required = true)
    protected PathsProcessedType pathsProcessed;
    @XmlElement(required = true)
    protected ByYearType byYear;
    @XmlElement(required = true)
    protected ByFormatType byFormat;

    /**
     * Gets the value of the profilingStartDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getProfilingStartDate() {
        return profilingStartDate;
    }

    /**
     * Sets the value of the profilingStartDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setProfilingStartDate(XMLGregorianCalendar value) {
        this.profilingStartDate = value;
    }

    /**
     * Gets the value of the profilingEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getProfilingEndDate() {
        return profilingEndDate;
    }

    /**
     * Sets the value of the profilingEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setProfilingEndDate(XMLGregorianCalendar value) {
        this.profilingEndDate = value;
    }

    /**
     * Gets the value of the profilingSaveDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getProfilingSaveDate() {
        return profilingSaveDate;
    }

    /**
     * Sets the value of the profilingSaveDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setProfilingSaveDate(XMLGregorianCalendar value) {
        this.profilingSaveDate = value;
    }

    /**
     * Gets the value of the totalReadableFiles property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalReadableFiles() {
        return totalReadableFiles;
    }

    /**
     * Sets the value of the totalReadableFiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalReadableFiles(BigInteger value) {
        this.totalReadableFiles = value;
    }

    /**
     * Gets the value of the totalUnreadableFiles property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalUnreadableFiles() {
        return totalUnreadableFiles;
    }

    /**
     * Sets the value of the totalUnreadableFiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalUnreadableFiles(BigInteger value) {
        this.totalUnreadableFiles = value;
    }

    /**
     * Gets the value of the totalUnreadableFolders property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalUnreadableFolders() {
        return totalUnreadableFolders;
    }

    /**
     * Sets the value of the totalUnreadableFolders property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalUnreadableFolders(BigInteger value) {
        this.totalUnreadableFolders = value;
    }

    /**
     * Gets the value of the totalSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalSize() {
        return totalSize;
    }

    /**
     * Sets the value of the totalSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalSize(BigDecimal value) {
        this.totalSize = value;
    }

    /**
     * Gets the value of the smallestSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSmallestSize() {
        return smallestSize;
    }

    /**
     * Sets the value of the smallestSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSmallestSize(BigDecimal value) {
        this.smallestSize = value;
    }

    /**
     * Gets the value of the largestSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLargestSize() {
        return largestSize;
    }

    /**
     * Sets the value of the largestSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLargestSize(BigDecimal value) {
        this.largestSize = value;
    }

    /**
     * Gets the value of the meanSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMeanSize() {
        return meanSize;
    }

    /**
     * Sets the value of the meanSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMeanSize(BigDecimal value) {
        this.meanSize = value;
    }

    /**
     * Gets the value of the pathsProcessed property.
     * 
     * @return
     *     possible object is
     *     {@link PathsProcessedType }
     *     
     */
    public PathsProcessedType getPathsProcessed() {
        return pathsProcessed;
    }

    /**
     * Sets the value of the pathsProcessed property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathsProcessedType }
     *     
     */
    public void setPathsProcessed(PathsProcessedType value) {
        this.pathsProcessed = value;
    }

    /**
     * Gets the value of the byYear property.
     * 
     * @return
     *     possible object is
     *     {@link ByYearType }
     *     
     */
    public ByYearType getByYear() {
        return byYear;
    }

    /**
     * Sets the value of the byYear property.
     * 
     * @param value
     *     allowed object is
     *     {@link ByYearType }
     *     
     */
    public void setByYear(ByYearType value) {
        this.byYear = value;
    }

    /**
     * Gets the value of the byFormat property.
     * 
     * @return
     *     possible object is
     *     {@link ByFormatType }
     *     
     */
    public ByFormatType getByFormat() {
        return byFormat;
    }

    /**
     * Sets the value of the byFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link ByFormatType }
     *     
     */
    public void setByFormat(ByFormatType value) {
        this.byFormat = value;
    }

}
