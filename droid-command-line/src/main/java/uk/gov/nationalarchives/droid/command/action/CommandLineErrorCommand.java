/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.io.PrintWriter;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import static uk.gov.nationalarchives.droid.command.i18n.I18N.getResource;

import uk.gov.nationalarchives.droid.command.i18n.I18N;

/**
 * @author rflitcroft
 *
 */
public class CommandLineErrorCommand implements DroidCommand {

    private static final String USAGE = "droid [options]";

    private static final int WRAP_WIDTH = 80;
    private PrintWriter writer;
    private Options options;
    
    /**
     * @param writer the print write to diaply output
     * @param options the commnd line options
     * 
     */
    public CommandLineErrorCommand(PrintWriter writer, Options options) {
        this.writer = writer;
        this.options = options;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printWrapped(writer, WRAP_WIDTH, getResource(I18N.BAD_OPTIONS));
        formatter.printHelp(writer, WRAP_WIDTH, 
                USAGE, getResource(I18N.OPTIONS_HEADER), options, 2, 2, null);
    }
}
