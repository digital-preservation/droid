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

/**
 * Wrapper class to wrap various options used for export.
 * e.g.
 * - whether the export is per file or per format
 * - whether the data should be enclosed in quotes
 * - whether BOM is to be used
 * - Which columns to export
 * - Is there an export template to be used
 *
 */
public final class ExportDetails {

    private final ExportOptions exportOptions;
    private final String outputEncoding;
    private final boolean bomFlag;
    private final boolean quoteAllFields;
    private final String columnsToWrite;
    private final String exportTemplatePath;

    /**
     * Private constructor. The consumer can get the ExportDetails instance using ExportDetailsBuilder.
     * @param exportOptions  whether it is a "per row" export or "per format" export
     * @param outputEncoding  encoding to be used
     * @param bomFlag whether to use BOM
     * @param quoteAllFields whether the export fields should be enclosed in double quotes
     * @param columnsToWrite List of columns to write
     * @param exportTemplatePath absolute path to an export template, if one is being used.
     */
    private ExportDetails(ExportOptions exportOptions, String outputEncoding, boolean bomFlag, boolean quoteAllFields, String columnsToWrite, String exportTemplatePath) {
        this.exportOptions = exportOptions;
        this.outputEncoding = outputEncoding;
        this.bomFlag = bomFlag;
        this.quoteAllFields = quoteAllFields;
        this.columnsToWrite = columnsToWrite;
        this.exportTemplatePath = exportTemplatePath;
    }

    /**
     *
     * @return The export options.
     */
    public ExportOptions getExportOptions() {
        return exportOptions;
    }

    /**
     * @return The encoding for output.
     */
    public String getOutputEncoding() {
        return outputEncoding;
    }

    /**
     *
     * @return status of bom.
     */
    public boolean bomFlag() {
        return bomFlag;
    }

    /**
     * @return whether all fields are quoted, or just those that contain field separators (commas).
     */
    public boolean quoteAllFields() {
        return quoteAllFields;
    }

    /**
     * @return A list of the columns to write, or null if all columns.
     */
    public String getColumnsToWrite() {
        return columnsToWrite;
    }

    /**
     * @return A path for export template, null if no template in use.
     */
    public String getExportTemplatePath() {
        return exportTemplatePath;
    }

    /**
     * Builder class to build the ExportDetails as a fluent API.
     */
    public static class ExportDetailsBuilder {
        private ExportOptions exportOptions = ExportOptions.ONE_ROW_PER_FILE;
        private String outputEncoding = "UTF-8";
        private boolean bomFlag;
        private boolean quoteAllFields = true;
        private String columnsToWrite;
        private String exportTemplatePath;

        public ExportDetailsBuilder withExportOptions(ExportOptions options) {
            this.exportOptions = options;
            return  this;
        }

        public ExportDetailsBuilder withOutpuEncoding(String encoding) {
            this.outputEncoding = encoding;
            return this;
        }

        public ExportDetailsBuilder withBomFlag(boolean bom) {
            this.bomFlag = bom;
            return this;
        }
        public ExportDetailsBuilder withQuotingAllFields(boolean quoteFields) {
            this.quoteAllFields = quoteFields;
            return this;
        }

        public ExportDetailsBuilder withColumnsToWrite(String columns) {
            this.columnsToWrite = columns;
            return this;
        }

        public ExportDetailsBuilder withExportTemplatePath(String templatePath) {
            this.exportTemplatePath = templatePath;
            return this;
        }

        public ExportDetails build() {
            return new ExportDetails(exportOptions, outputEncoding, bomFlag, quoteAllFields, columnsToWrite, exportTemplatePath);
        }
    }
}
