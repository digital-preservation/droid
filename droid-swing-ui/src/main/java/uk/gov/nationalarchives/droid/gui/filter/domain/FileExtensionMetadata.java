/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;

//import uk.gov.nationalarchives.droid.profile.Values;

/**
 * File extension metadata.
 * @author adash
 *
 */
public class FileExtensionMetadata extends GenericMetadata {
    
    //private static final String CLASS = ".class";
    //private static final String JAVA = ".java";
    //private static final String PDF = ".pdf";
    //private static final String DOC = ".doc";
    //private static final String DRD = ".drd";
    //private static final String BAT = ".bat";
    //private static final String EXE = ".exe";

    private static final String DISPLAY_NAME = "File extension";
    
    /** */
    public FileExtensionMetadata() {
        super(CriterionFieldEnum.FILE_EXTENSION);
       
        int i = 0;
        //addPossibleValue(new Values(i++, EXE, EXE));
        //addPossibleValue(new Values(i++, BAT, BAT));
        //addPossibleValue(new Values(i++, DRD, DRD));
        //addPossibleValue(new Values(i++, DOC, DOC));
        //addPossibleValue(new Values(i++, PDF, PDF));
        //addPossibleValue(new Values(i++, JAVA, JAVA));
        //addPossibleValue(new Values(i++, CLASS, CLASS));
        
        addOperation(CriterionOperator.EQ);
        addOperation(CriterionOperator.STARTS_WITH);
        addOperation(CriterionOperator.NE);
        addOperation(CriterionOperator.ENDS_WITH);
        addOperation(CriterionOperator.CONTAINS);
        addOperation(CriterionOperator.NOT_STARTS_WITH);
        addOperation(CriterionOperator.NOT_ENDS_WITH);
        addOperation(CriterionOperator.NOT_CONTAINS);
        
    }

    @Override
    public boolean isFreeText() {
        return true;
    }

    @Override
    public String toString() {
        return getMetadataName().toString();
    }

}
