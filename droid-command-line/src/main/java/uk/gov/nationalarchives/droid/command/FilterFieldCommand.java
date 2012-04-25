/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
