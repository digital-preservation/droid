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
