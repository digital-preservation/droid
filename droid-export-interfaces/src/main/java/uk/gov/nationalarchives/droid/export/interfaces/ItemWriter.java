/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.export.interfaces;

import java.io.Writer;
import java.util.List;
import java.util.Map;


/**
 * @author rflitcroft, Brian O'Reilly
 * @param <T> the type to write
 */
public interface ItemWriter<T> {

    /**
     * Writes the items.
     * @param items the items to write.
     */
    void write(List<? extends T> items);
    
    /**
     * Opens a writer for writing.
     * @param writer the writer to use
     */
    void open(Writer writer);
    
    /**
     * Closes the writer.
     */
    void close();
    
    /**
     * 
     * @param options Sets the options to use when writing out the export.
     */
    void setOptions(ExportOptions options);

    /**
     * @param outputOptions Sets the output options to use when writing out the export
     */
    void setOutputOptions(ExportOutputOptions outputOptions);

    /**
     * Customises the column headers in the export output.
     * @param headersToSet - Map of headers to customise and values to set.
     */
    void setHeaders(Map<String, String> headersToSet);

    /**
     * Sets whether all fields should be quoted, or just those that contain a comma separator.
     * @param quoteAll Whether all fields should be quoted.
     */
    void setQuoteAllFields(boolean quoteAll);

    /**
     * Sets which columns should be written, as a space separated list of column headers.
     * If the string is null or empty, all columns will be written out.
     * <p> Valid column names are:
     * ID, PARENT_ID, URI, FILE_PATH, NAME, METHOD, STATUS, SIZE, TYPE, EXT, LAST_MODIFIED,
     * EXTENSION_MISMATCH, HASH, FORMAT_COUNT, PUID, MIME_TYPE, FORMAT_NAME, FORMAT_VERSION.
     *
     * @param columnNames A space separated list of column headers, or null or empty if all columns should be written.
     */
    void setColumnsToWrite(String columnNames);

    /**
     * Sets the ExportTemplate which can override the column names, column ordering and contents of the columns.
     *
     * @param template An instance of ExportTemplate.
     */
    void setExportTemplate(ExportTemplate template);

}
