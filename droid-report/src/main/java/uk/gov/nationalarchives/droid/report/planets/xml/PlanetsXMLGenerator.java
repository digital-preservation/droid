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
package uk.gov.nationalarchives.droid.report.planets.xml;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import uk.gov.nationalarchives.droid.planet.xml.dao.GroupByPuidSizeAndCountRow;
import uk.gov.nationalarchives.droid.planet.xml.dao.GroupByYearSizeAndCountRow;
import uk.gov.nationalarchives.droid.planet.xml.dao.PlanetsXMLData;
import uk.gov.nationalarchives.droid.report.planets.domain.ByFormatType;
import uk.gov.nationalarchives.droid.report.planets.domain.ByYearType;
import uk.gov.nationalarchives.droid.report.planets.domain.FileProfileType;
import uk.gov.nationalarchives.droid.report.planets.domain.FormatItemType;
import uk.gov.nationalarchives.droid.report.planets.domain.ObjectFactory;
import uk.gov.nationalarchives.droid.report.planets.domain.PathsProcessedType;
import uk.gov.nationalarchives.droid.report.planets.domain.YearItemType;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author Alok Kumar Dash.
 * 
 * @deprecated PLANETS XML is now generated using XSLT over normal report xml files. 
 */
@Deprecated
public class PlanetsXMLGenerator {

    private static final int TWENTY = 20;
    private static final int FOURTY = 40;
    private static final int SIXTY = 60;
    private static final int EIGHTY = 80;
    private static final int HUNDRED = 100;
    private static final int THOUSAND = 1000;

    private static final int YEAROFFSET = 1900;
    private String filePath;

    private ProgressObserver observer;

    private PlanetsXMLData planetsData;

    //private final Log log = LogFactory.getLog(getClass());

    /**
     * Constructor.
     * 
     * @param observer
     *            Progress observer.
     * @param filePath
     *            File path to save.
     * @param planetsData
     *            PlanetsXMLData data.
     */
    public PlanetsXMLGenerator(ProgressObserver observer,
            String filePath, PlanetsXMLData planetsData) {
        this.observer = observer;
        this.filePath = filePath;
        this.planetsData = planetsData;
    }

    /**
     * Constructor.
     * 
     * @param filePath
     *            File path to save.
     * @param planetsData
     *            PlanetsXMLData data.
     */
    public PlanetsXMLGenerator(String filePath, PlanetsXMLData planetsData) {
        this.filePath = filePath;
        this.planetsData = planetsData;
    }

    /**
     * No argument constructor.
     */
    public PlanetsXMLGenerator() {
    }

    /**
     * Generates planet xml.
     */
    public void generate() {
        try {
            ObjectFactory objFactory = new ObjectFactory();
            FileProfileType fileProfileType = objFactory
                    .createFileProfileType();
            JAXBElement<FileProfileType> fileProfile = objFactory
                    .createFileProfile(fileProfileType);

            PathsProcessedType pathProcessedType = getPathProcessed(objFactory);

            ByFormatType byFormatType = getGroupByPuid(objFactory);

            ByYearType byYearType = getGroupByYear(objFactory);

            getFileProfileType(fileProfileType, pathProcessedType,
                    byFormatType, byYearType);

            JAXBContext jaxbContext = JAXBContext
                    .newInstance("uk.gov.nationalarchives.droid.report.planets.domain");
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            if (observer != null) {
                observer.onProgress(HUNDRED);
            }

            m.marshal(fileProfile, new File(filePath));

        } catch (JAXBException ex) {
            throw new RuntimeException("Error  while writing to an xml file.");
            //log.info("JAXB Exception - Error while writing to an xml.");
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while writing to an xml file.");
            //log.info("InterruptedException - Error while writing to an xml.");
        }

    }

    /**
     * @param objFactory
     * @return
     * @throws InterruptedException
     */
    private PathsProcessedType getPathProcessed(ObjectFactory objFactory)
        throws InterruptedException {
        PathsProcessedType pathProcessedType = objFactory
                .createPathsProcessedType();

        List<String> pathItemsList = pathProcessedType.getPathItem();

        for (String item : planetsData.getTopLevelItems()) {
            pathItemsList.add(item);
        }
        if (observer != null) {
            observer.onProgress(TWENTY);
        }
        return pathProcessedType;
    }

    /**
     * @param fileProfileType
     * @param pathProcessedType
     * @param byFormatType
     * @param byYearType
     * @throws InterruptedException
     */
    private void getFileProfileType(FileProfileType fileProfileType,
            PathsProcessedType pathProcessedType, ByFormatType byFormatType,
            ByYearType byYearType) throws InterruptedException {
        fileProfileType.setPathsProcessed(pathProcessedType);
        fileProfileType.setByFormat(byFormatType);
        fileProfileType.setByYear(byYearType);

        fileProfileType.setProfilingEndDate(getXMLGregorianCalendar(planetsData
                .getProfileStat().getProfileEndDate()));
        fileProfileType
                .setProfilingSaveDate(getXMLGregorianCalendar(planetsData
                        .getProfileStat().getProfileSaveDate()));
        fileProfileType
                .setProfilingStartDate(getXMLGregorianCalendar(planetsData
                        .getProfileStat().getProfileStartDate()));

        fileProfileType.setTotalSize(new BigDecimal(planetsData
                .getProfileStat().getProfileTotalSize()));
        fileProfileType.setSmallestSize(new BigDecimal(planetsData
                .getProfileStat().getProfileSmallestSize()));
        fileProfileType.setMeanSize(planetsData.getProfileStat()
                .getProfileMeanSize());
        fileProfileType.setLargestSize(new BigDecimal(planetsData
                .getProfileStat().getProfileLargestSize()));

        fileProfileType.setTotalUnreadableFiles(planetsData.getProfileStat()
                .getProfileTotalUnReadableFiles());
        fileProfileType.setTotalReadableFiles(planetsData.getProfileStat()
                .getProfileTotalReadableFiles());
        fileProfileType.setTotalUnreadableFolders(planetsData.getProfileStat()
                .getProfileTotalUnReadableFolders());

        if (observer != null) {
            observer.onProgress(EIGHTY);
        }

    }

    /**
     * @param objFactory
     * @return
     * @throws InterruptedException
     */
    private ByFormatType getGroupByPuid(ObjectFactory objFactory)
        throws InterruptedException {
        ByFormatType byFormatType = objFactory.createByFormatType();
        List<FormatItemType> formatItemList = byFormatType.getFormatItem();
        for (GroupByPuidSizeAndCountRow byPuid : planetsData.getGroupByPuid()) {
            FormatItemType formatItemType = objFactory.createFormatItemType();
            formatItemType.setFormatName(byPuid.getFormatName());
            formatItemType.setFormatVersion(byPuid.getFormatVersion());
            formatItemType.setMIME(byPuid.getMimeType());
            formatItemType.setNumFiles(byPuid.getCount());
            formatItemType.setPUID(byPuid.getPuid());
            formatItemType.setTotalFileSize(byPuid.getSize());
            if (!"NULL".equals(byPuid.getPuid())) {
                formatItemList.add(formatItemType);
            }
        }
        if (observer != null) {
            observer.onProgress(FOURTY);
        }
        return byFormatType;
    }

    /**
     * @param objFactory
     * @return
     * @throws DatatypeConfigurationException
     * @throws InterruptedException
     */
    private ByYearType getGroupByYear(ObjectFactory objFactory)
        throws InterruptedException {
        ByYearType byYearType = objFactory.createByYearType();
        List byeYearTypeList = byYearType.getYearItem();

        if (planetsData.getGroupByYear() == null) {
            byeYearTypeList = null;
            return null;
        }

        for (GroupByYearSizeAndCountRow byYear : planetsData.getGroupByYear()) {
            YearItemType yearItemType = objFactory.createYearItemType();

            yearItemType.setNumFiles(byYear.getCount());
            yearItemType.setTotalFileSize(byYear.getSize());

            XMLGregorianCalendar xmlCalendar;
            try {
                xmlCalendar = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar();

                xmlCalendar.setYear(byYear.getYear());
                yearItemType.setYear(xmlCalendar);
                byeYearTypeList.add(yearItemType);

            } catch (DatatypeConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (observer != null) {
            observer.onProgress(SIXTY);
        }

        return byYearType;
    }

    private XMLGregorianCalendar getXMLGregorianCalendar(Date date) {
        XMLGregorianCalendar xmlCalendar = null;
        try {
            xmlCalendar = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar();
            if (date != null) {
                xmlCalendar.setDay(date.getDate());
                xmlCalendar.setMonth(date.getMonth() + 1);
                xmlCalendar.setYear(date.getYear() + YEAROFFSET);
                xmlCalendar.setHour(date.getHours());
                xmlCalendar.setMinute(date.getMinutes());
                xmlCalendar.setSecond(date.getSeconds());
            }

        } catch (DatatypeConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return xmlCalendar;

    }

}
