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
