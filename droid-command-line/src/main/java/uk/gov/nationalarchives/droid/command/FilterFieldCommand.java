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
package uk.gov.nationalarchives.droid.command;

import java.io.PrintWriter;

import org.apache.commons.cli.HelpFormatter;

import uk.gov.nationalarchives.droid.command.action.DroidCommand;
import uk.gov.nationalarchives.droid.command.filter.DqlCriterionMapper;
import uk.gov.nationalarchives.droid.command.i18n.I18N;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;

/**
 * @author rflitcroft
 *
 */
public class FilterFieldCommand implements DroidCommand {

    /**
     * 
     */
    private static final String NAME_DESCRIPTION = "%s\t%s";
    private static final int LINE_INDENT = 4;
    private static final int LINE_WIDTH = 80;

    private static final String BASE_PROPERTY = "dql.help.";
    
    private PrintWriter printWriter;
    private HelpFormatter helpFormatter = new HelpFormatter();
    
    /**
     * @param printWriter a print writer for user output
     */
    public FilterFieldCommand(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        
        helpFormatter.printWrapped(printWriter, LINE_WIDTH, LINE_INDENT, "Filter fields:");
        
        for (String fieldName : DqlCriterionMapper.allDqlFields()) {
            final CriterionFieldEnum field = DqlCriterionMapper.forField(fieldName);
            String description = I18N.getResource(BASE_PROPERTY + field.name());
            String text = String.format(NAME_DESCRIPTION, fieldName, description);
            helpFormatter.printWrapped(printWriter, LINE_WIDTH, LINE_INDENT, text);
        }
        
        helpFormatter.printWrapped(printWriter, LINE_WIDTH, LINE_INDENT, "\nFilter operators:");
        
        for (String operatorName : DqlCriterionMapper.allDqlOperators()) {
            final CriterionOperator operator = DqlCriterionMapper.forOperator(operatorName);
            String description = I18N.getResource(BASE_PROPERTY + operator.name());
            String text = String.format(NAME_DESCRIPTION, operatorName, description);
            helpFormatter.printWrapped(printWriter, LINE_WIDTH, LINE_INDENT, text);
        }
        
        printWriter.flush();
        
    }

}
