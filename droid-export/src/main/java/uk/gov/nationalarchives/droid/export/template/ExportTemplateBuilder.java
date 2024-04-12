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
package uk.gov.nationalarchives.droid.export.template;

import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplate;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;
import uk.gov.nationalarchives.droid.profile.CsvWriterConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to build export template from a file.
 * The class parses a file in a simple way using delimiters and string manipulations.
 */
public class ExportTemplateBuilder {

    private static final String COLON = ":";
    private static final String UNABLE_TO_PARSE_LINE_MSG = "Unable to parse line: '%s', %s";
    private static final String INVALID_DATA_MODIFIER_SYNTAX_MESSAGE_FORMAT = "Invalid syntax in data modifier expression '%s', %s";
    private static final String DOUBLE_QUOTES = "\"";
    private static final String OPENING_BRACKET = "(";
    private static final String CLOSING_BRACKET = ")";
    private static final String DATA_COLUMN_PREFIX = "$";

    /**
     * The entry point into building an ExportTemplate object by reading a template file.
     * @param pathToTemplate absolute path to the template file
     * @return ExportTemplate object
     */
    public ExportTemplate buildExportTemplate(String pathToTemplate) {
        if (pathToTemplate == null) {
            return null;
        }

        List<String> templateLines;
        try {
            templateLines = Files.readAllLines(Paths.get(pathToTemplate));
            Map<Integer, ExportTemplateColumnDef> columnMap = buildColumnMap(templateLines);
            return new ExportTemplateImpl(columnMap);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read export template file at path: " + pathToTemplate);
        }
    }

    private Map<Integer, ExportTemplateColumnDef> buildColumnMap(List<String> templateLines) {
        if ((templateLines == null) || (templateLines.size() == 0)) {
            throw new ExportTemplateParseException("Export template is empty");
        }
        String versionLine = templateLines.get(0);
        String version = parseVersionLine(versionLine);

        //we have only one version at the moment, but future provision for versioning
        switch (version) {
            case "1.0":
                return parseExportTemplateV1(templateLines.subList(1, templateLines.size()));
            default:
                throw new ExportTemplateParseException("Unsupported version for the export template");
        }
    }

    private Map<Integer, ExportTemplateColumnDef> parseExportTemplateV1(List<String> templateLines) {
        List<String> columnLines = templateLines.stream().filter(line -> line.trim().length() > 0).collect(Collectors.toList());
        Map<Integer, ExportTemplateColumnDef> columnMap = new HashMap<>();
        for (int i = 0; i < columnLines.size(); i++) {
            String line = columnLines.get(i);
            if (!line.contains(COLON)) {
                throw new ExportTemplateParseException(String.format(UNABLE_TO_PARSE_LINE_MSG, line, "line does not contain ':'"));
            }
            String header = line.substring(0, line.indexOf(COLON)).trim();
            if (header.isEmpty()) {
                throw new ExportTemplateParseException(String.format(UNABLE_TO_PARSE_LINE_MSG, line, "column header is empty"));
            }

            String token2 = line.substring(line.indexOf(COLON) + 1).trim();

            if (token2.startsWith(DATA_COLUMN_PREFIX)) {
                columnMap.put(i, createProfileNodeDef(header, token2));
            } else if ((token2.isEmpty()) || (token2.startsWith(DOUBLE_QUOTES))) {
                columnMap.put(i, createConstantStringDef(header, token2));
            } else {
                columnMap.put(i, createDataModifierDef(header, token2));
            }
        }
        return columnMap;
    }

    private ExportTemplateColumnDef createDataModifierDef(String header, String param2) {

        assertDataModifierSyntaxValid(param2);

        String[] tokens = param2.split("\\(");
        String operationName = tokens[0].trim();

        List<String> operations = Arrays.stream(
                ExportTemplateColumnDef.DataModification.values()).map(v -> v.toString()).
                collect(Collectors.toList());

        if (!operations.contains(operationName)) {
            throw new ExportTemplateParseException("Undefined operation '" + operationName + "' encountered in export template");
        }

        String column = tokens[1].trim().substring(0, tokens[1].trim().length() - 1);
        ProfileResourceNodeColumnDef inner = createProfileNodeDef(header, column);

        ExportTemplateColumnDef.DataModification operation = ExportTemplateColumnDef.DataModification.valueOf(operationName);
        return new DataModifierColumnDef(inner, operation);
    }

    private void assertDataModifierSyntaxValid(String expression) {
        String expressionToTest = expression.trim();
        // valid statement like LCASE($URI)
        if (expressionToTest.chars().filter(ch -> ch == '(').count() != 1) {
            throw new ExportTemplateParseException(String.format(INVALID_DATA_MODIFIER_SYNTAX_MESSAGE_FORMAT, expression, "expecting exactly one occurrence of '('"));
        }
        if (expressionToTest.chars().filter(ch -> ch == ')').count() != 1) {
            throw new ExportTemplateParseException(String.format(INVALID_DATA_MODIFIER_SYNTAX_MESSAGE_FORMAT, expression, "expecting exactly one occurrence of ')'"));
        }
        if (expressionToTest.indexOf(OPENING_BRACKET) > expressionToTest.indexOf(CLOSING_BRACKET)) {
            throw new ExportTemplateParseException(String.format(INVALID_DATA_MODIFIER_SYNTAX_MESSAGE_FORMAT, expression, "expecting '(' before ')'"));
        }
        if (expressionToTest.indexOf(OPENING_BRACKET) == 0) {
            throw new ExportTemplateParseException(String.format(INVALID_DATA_MODIFIER_SYNTAX_MESSAGE_FORMAT, expression, "expecting an operation definition before '('"));
        }
        String dataColumnToken = expressionToTest.substring(expressionToTest.indexOf(OPENING_BRACKET) + 1,
                expressionToTest.indexOf(CLOSING_BRACKET)).trim();
        if (!dataColumnToken.startsWith(DATA_COLUMN_PREFIX)) {
            throw new ExportTemplateParseException(String.format(INVALID_DATA_MODIFIER_SYNTAX_MESSAGE_FORMAT, expression, "expecting '$' after '('"));
        }
    }

    private ExportTemplateColumnDef createConstantStringDef(String header, String param2) {
        if (param2.isEmpty()) {
            return new ConstantStringColumnDef("", header);
        } else {
            if (!param2.endsWith(DOUBLE_QUOTES)) {
                throw new ExportTemplateParseException("The line with a constant value ('" + param2 + "') in template definition does not have closing quotes");
            }
            return new ConstantStringColumnDef(param2.substring(1, param2.length() - 1), header);
        }
    }

    private ProfileResourceNodeColumnDef createProfileNodeDef(String header, String param2) {
        String messageFormat = "Invalid column name. '%s' does not exist in profile results";
        if (!param2.startsWith(DATA_COLUMN_PREFIX)) {
            throw new ExportTemplateParseException(String.format(messageFormat, param2));
        }
        String originalColumnName = param2.substring(1);
        if (!(Arrays.stream(CsvWriterConstants.HEADERS).collect(Collectors.toList()).contains(originalColumnName))) {
            throw new ExportTemplateParseException(String.format(messageFormat, originalColumnName));
        }
        return new ProfileResourceNodeColumnDef(originalColumnName, header);
    }

    private String parseVersionLine(String versionLine) {
        String versionPrefix = "version";
        String versionString = versionLine.trim();
        if (!versionString.startsWith(versionPrefix)) {
            throw new ExportTemplateParseException("First line in the template needs to specify version in the form \"version <version number>\"");
        }

        return versionString.substring(versionPrefix.length()).trim();
    }
}


