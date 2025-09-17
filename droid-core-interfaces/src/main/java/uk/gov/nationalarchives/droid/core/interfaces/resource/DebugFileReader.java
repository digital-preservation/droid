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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.cache.WindowCache;
import net.byteseek.io.reader.windows.Window;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DebugFileReader extends FileReader {

    private long bytesReadFromFile;

    private long bytesReadFromCache;

    public DebugFileReader(File file, WindowCache cache) throws FileNotFoundException {
        super(file, cache);
    }

    @Override
    protected Window createWindow(long windowStart) throws IOException {
        bytesReadFromFile += windowSize;
        return super.createWindow(windowStart);
    }

    @Override
    public Window getWindow(final long position) throws IOException {
        if (this.cache.getWindow(position) != null) {
            bytesReadFromCache += windowSize;
        }
        return super.getWindow(position);
    }

    public long getBytesReadFromFile() {
        return bytesReadFromFile;
    }

    public long getBytesReadFromCache() {
        return bytesReadFromCache;
    }

    public void resetBytesRead() {
        this.bytesReadFromFile = 0;
        this.bytesReadFromCache = 0;
    }
}
