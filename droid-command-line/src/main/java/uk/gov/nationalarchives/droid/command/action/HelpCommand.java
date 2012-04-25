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

import static uk.gov.nationalarchives.droid.command.i18n.I18N.getResource;

import uk.gov.nationalarchives.droid.command.i18n.I18N;

/**
 * @author rflitcroft
 *
 */
public class HelpCommand implements DroidCommand {

    /** Options message. */
    public static final String USAGE = "droid [options]";
    /** Wrap width. */
    public static final int WRAP_WIDTH = 80;

    private PrintWriter writer;
    
    /**
     * @param writer a print writer
     */
    public HelpCommand(PrintWriter writer) {
        this.writer = writer;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(writer, WRAP_WIDTH, USAGE, 
                getResource(I18N.OPTIONS_HEADER), CommandLineParam.options(), 2, 2, null);
    }
}
