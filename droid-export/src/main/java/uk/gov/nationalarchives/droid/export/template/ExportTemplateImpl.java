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

import java.util.HashMap;
import java.util.Map;

public class ExportTemplateImpl implements ExportTemplate {
    private Map<Integer, ExportTemplateColumnDef> columnOrderMap = new HashMap<>();

    //CHECKSTYLE:OFF - No need to worry about magic numbers here for now, until UI is all wired up
    public ExportTemplateImpl() {
        columnOrderMap.put(0, new ProfileResourceNodeColumnDef("ID", "Identifier"));
        columnOrderMap.put(1, new ProfileResourceNodeColumnDef("FILE_PATH", "Path"));
        columnOrderMap.put(2, new ProfileResourceNodeColumnDef("SIZE", "Size"));
        columnOrderMap.put(3, new ProfileResourceNodeColumnDef("HASH", "HASH"));
        columnOrderMap.put(4, new ProfileResourceNodeColumnDef("PUID", "Puid"));
    }
    //CHECKSTYLE:ON

    public Map<Integer, ExportTemplateColumnDef> getColumnOrderMap() {
        return columnOrderMap;
    }
}
