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
import java.util.Comparator;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;

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
/*        formatter.setOptionComparator(
            new Comparator() {
                public int compare(Object o1, Object o2) {
                    Option opt1 = (Option)o1; 
                    Option opt2 = (Option)o2; 
                    return opt2.getKey().compareToIgnoreCase(opt1.getKey()); 
                }
        }); */
        formatter.printHelp(writer, WRAP_WIDTH, USAGE, 
                getResource(I18N.OPTIONS_HEADER), CommandLineParam.singleOptions(), 2, 3, null);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.getOptions(CommandLineParam.RUN_NO_PROFILE), 2, 2);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.noProfileRunSubOptions(), 5, 3);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.getOptions(CommandLineParam.RUN_PROFILE), 2, 2);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.profileRunSubOptions(), 5, 7);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.getOptions(CommandLineParam.EXPORT_ONE_ROW_PER_FILE), 2, 9);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.exportSubOptions(), 5, 7);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.getOptions(CommandLineParam.EXPORT_ONE_ROW_PER_FORMAT), 2, 7);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.exportSubOptions(), 5, 7);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.getOptions(CommandLineParam.REPORT), 2, 14);
        formatter.printOptions(writer, WRAP_WIDTH,
                CommandLineParam.reportSubOptions(), 5, 6);
    }
}
