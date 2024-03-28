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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplate;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ExportTemplateBuilderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_read_an_export_template_file_and_construct_export_template_object() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "Identifier: $ID",
                "Language: \"Gibberish\"",
                "Path: UCASE($FILE_PATH)",
                "Size: $SIZE",
                "HASH: $HASH");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);
        ExportTemplate template = builder.buildExportTemplate(tempFile.getAbsolutePath());
        assertNotNull(template);
        ExportTemplateColumnDef cscd = template.getColumnOrderMap().get(1);
        assertTrue(cscd instanceof ConstantStringColumnDef);
        assertEquals("Gibberish",template.getColumnOrderMap().get(1).getDataValue());

        ExportTemplateColumnDef dmcd = template.getColumnOrderMap().get(2);
        assertTrue(dmcd instanceof DataModifierColumnDef);
        assertEquals("FILE_PATH", dmcd.getOriginalColumnName());
        assertEquals("SMALL", dmcd.getOperatedValue("small"));
    }

    @Test
    public void should_throw_exception_when_the_column_description_lines_do_not_have_a_colon() throws IOException {
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList("version 1.0", "myCol $My_COL");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Unable to parse line: 'myCol $My_COL'", ex.getMessage());
    }

    @Test
    public void should_throw_exception_when_the_header_information_is_missing() throws IOException {
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList("version 1.0", ": $My_COL");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Unable to parse line: ': $My_COL'", ex.getMessage());
    }

    @Test
    public void should_treat_missing_value_as_empty_string_constant_in_export_template() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "Identifier: $ID",
                "Language: ",
                "Path: UCASE($FILE_PATH)");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);
        ExportTemplate template = builder.buildExportTemplate(tempFile.getAbsolutePath());
        assertNotNull(template);
        assertTrue(template.getColumnOrderMap().get(1) instanceof ConstantStringColumnDef);
        assertTrue(template.getColumnOrderMap().get(2) instanceof DataModifierColumnDef);
        assertEquals("FILE_PATH", template.getColumnOrderMap().get(2).getOriginalColumnName());
    }

    @Test
    public void should_throw_exception_when_version_string_is_bad() throws IOException {
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        Files.write(tempFile.toPath(), "versio 1.3".getBytes(StandardCharsets.UTF_8));
        List<String> data = Collections.singletonList("myCol:$My_COL");
        Files.write(tempFile.toPath(), data, StandardOpenOption.APPEND);
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("First line in the template needs to specify version in the form \"version <version number>\"", ex.getMessage());
    }

    @Test
    public void should_trim_blanks_from_lines_and_tokens_to_produce_a_valid_template() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "",
                "Language: \"Marathi\"    ",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);
        ExportTemplate template = builder.buildExportTemplate(tempFile.getAbsolutePath());
        assertEquals(1, template.getColumnOrderMap().size());
        assertEquals("Marathi", template.getColumnOrderMap().get(0).getDataValue());
    }

    @Test
    public void should_allow_constant_strings_with_double_quotes_in_the_data_value() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "",
                "Language: \"Star trek: \"Klingon\"\"",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);
        ExportTemplate template = builder.buildExportTemplate(tempFile.getAbsolutePath());
        assertEquals(1, template.getColumnOrderMap().size());
        assertEquals("Star trek: \"Klingon\"", template.getColumnOrderMap().get(0).getDataValue());
    }

    @Test
    public void should_throw_an_exception_if_the_constant_string_value_does_not_have_closing_double_quotes() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "",
                "Language: \"English",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("The line with a constant value ('\"English') in template definition does not have closing quotes", ex.getMessage());
    }

    @Test
    public void should_throw_an_exception_if_the_column_name_given_for_profile_resource_column_is_not_a_well_known_header() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "",
                "Identifier: $UUID",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Invalid column name. 'UUID' does not exist in profile results", ex.getMessage());
    }

    @Test
    public void should_throw_an_exception_when_the_operation_is_unknown() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "",
                "Identifier: Lower($ID)",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Undefined operation 'Lower' encountered in export template", ex.getMessage());
    }

    @Test
    public void should_trim_leading_and_trailing_spaces_from_a_header_label() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "     Identifier     : \"Something\"",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplate template =  builder.buildExportTemplate(tempFile.getAbsolutePath());
        assertEquals("Identifier", template.getColumnOrderMap().get(0).getHeaderLabel());
    }

    @Test
    public void should_support_colon_in_the_constant_string_value() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "     MyWebsite     : \"http://www.knowingwhere.com\"",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplate template =  builder.buildExportTemplate(tempFile.getAbsolutePath());
        assertEquals("MyWebsite", template.getColumnOrderMap().get(0).getHeaderLabel());
        assertEquals("http://www.knowingwhere.com", template.getColumnOrderMap().get(0).getDataValue());
    }

    @Test
    public void should_throw_an_exception_when_an_operation_cannot_be_located() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "     Copyright     : Crown Copyright (C)",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Invalid syntax in data modifier expression 'Crown Copyright (C)', expecting '$' after '('", ex.getMessage());
    }

    @Test
    public void should_throw_an_exception_when_a_constant_is_not_enclosed_in_double_quotes() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "     Copyright     : Crown Copyright",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Invalid syntax in data modifier expression 'Crown Copyright', expecting exactly one occurence of '('", ex.getMessage());
    }

    @Test
    public void should_throw_an_exception_when_a_column_name_for_modofication_is_invalid() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "     Copyright     : LCASE($CROWN)",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Invalid column name. 'CROWN' does not exist in profile results", ex.getMessage());
    }

    @Test
    public void should_throw_an_exception_when_a_column_name_is_not_prefixed_with_dollar_sign() throws IOException {
        ExportTemplateBuilder builder = new ExportTemplateBuilder();
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        List<String> data = Arrays.asList(
                "version 1.0",
                "     Copyright     : LCASE(PUID)",
                "");
        Files.write(tempFile.toPath(), data, StandardOpenOption.WRITE);

        ExportTemplateParseException ex = assertThrows(ExportTemplateParseException.class, () -> builder.buildExportTemplate(tempFile.getAbsolutePath()));
        assertEquals("Invalid syntax in data modifier expression 'LCASE(PUID)', expecting '$' after '('", ex.getMessage());
    }

}