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
    public static final int WRAP_WIDTH = 79;

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
        
        //CHECKSTYLE:OFF
        
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
        
        //CHECKSTYLE:ON
    }
}
