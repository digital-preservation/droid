/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by rhubner on 1/17/17.
 */
public class DroidUrlFormatTest {

    @Test
    public void testFromUrl() throws URISyntaxException {
        URI uri = new URI("file:/home/rhubner/ffmpeg_build/strange%20filenames/~~~%60%60~!@%23$%25%5E&*()_+_;:/");

        String expectedURI = "file:/home/rhubner/ffmpeg_build/strange%20filenames/%7E%7E%7E%60%60%7E%21%40%23%24%25%5E%26*%28%29_%2B_%3B%3A/";

        String result = DroidUrlFormat.format(uri);

        assertThat(result, equalTo(expectedURI));

    }

    private static final String TEST_PATTERNS[][] =  new String[][] {
            {"file:/C:/Users/rhubner/Downloads/droid/LICENSE", "file:/C:/Users/rhubner/Downloads/droid/LICENSE"},
            {"file:/root/", "file:/root/"},
            {"file:////na-filer1/homedir$/rhubner/Documents/web-archive-pc.pdf", "file:////na-filer1/homedir%24/rhubner/Documents/web-archive-pc.pdf"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/", "file:/home/rhubner/ffmpeg_build/strange%20filenames/"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/%22", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%22"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/Horkýže%20Slíže%20-%20Atómovy%20kryt", "file:/home/rhubner/ffmpeg_build/strange%20filenames/Hork%C3%BD%C5%BEe%20Sl%C3%AD%C5%BEe%20-%20At%C3%B3movy%20kryt"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/,,,,", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%2C%2C%2C%2C"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/~~~%60%60~!@%23$%25%5E&*()_+_;:", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%7E%7E%7E%60%60%7E%21%40%23%24%25%5E%26*%28%29_%2B_%3B%3A"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/åß∂ƒ©˙∆˚¬…æ", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%C3%A5%C3%9F%E2%88%82%C6%92%C2%A9%CB%99%E2%88%86%CB%9A%C2%AC%E2%80%A6%C3%A6"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/ÅÍÎÏ˝ÓÔ\uF8FFÒÚÆ☃", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%C3%85%C3%8D%C3%8E%C3%8F%CB%9D%C3%93%C3%94%EF%A3%BF%C3%92%C3%9A%C3%86%E2%98%83"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/%60⁄€‹›ﬁﬂ‡°·‚—±", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%60%E2%81%84%E2%82%AC%E2%80%B9%E2%80%BA%EF%AC%81%EF%AC%82%E2%80%A1%C2%B0%C2%B7%E2%80%9A%E2%80%94%C2%B1"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/ЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюя", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%D0%81%D0%82%D0%83%D0%84%D0%85%D0%86%D0%87%D0%88%D0%89%D0%8A%D0%8B%D0%8C%D0%8D%D0%8E%D0%8F%D0%90%D0%91%D0%92%D0%93%D0%94%D0%95%D0%96%D0%97%D0%98%D0%99%D0%9A%D0%9B%D0%9C%D0%9D%D0%9E%D0%9F%D0%A0%D0%A1%D0%A2%D0%A3%D0%A4%D0%A5%D0%A6%D0%A7%D0%A8%D0%A9%D0%AA%D0%AB%D0%AC%D0%AD%D0%AE%D0%AF%D0%B0%D0%B1%D0%B2%D0%B3%D0%B4%D0%B5%D0%B6%D0%B7%D0%B8%D0%B9%D0%BA%D0%BB%D0%BC%D0%BD%D0%BE%D0%BF%D1%80%D1%81%D1%82%D1%83%D1%84%D1%85%D1%86%D1%87%D1%88%D1%89%D1%8A%D1%8B%D1%8C%D1%8D%D1%8E%D1%8F"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/찦차를%20타고%20온%20펲시맨과%20쑛다리%20똠방각하", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%EC%B0%A6%EC%B0%A8%EB%A5%BC%20%ED%83%80%EA%B3%A0%20%EC%98%A8%20%ED%8E%B2%EC%8B%9C%EB%A7%A8%EA%B3%BC%20%EC%91%9B%EB%8B%A4%EB%A6%AC%20%EB%98%A0%EB%B0%A9%EA%B0%81%ED%95%98"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/ヽ༼ຈل͜ຈ༽ﾉ%20ヽ༼ຈل͜ຈ༽ﾉ%20", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%E3%83%BD%E0%BC%BC%E0%BA%88%D9%84%CD%9C%E0%BA%88%E0%BC%BD%EF%BE%89%20%E3%83%BD%E0%BC%BC%E0%BA%88%D9%84%CD%9C%E0%BA%88%E0%BC%BD%EF%BE%89%20"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/%3C%3E%3C%3E%3F*+%25", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%3C%3E%3C%3E%3F*%2B%25"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/\uD835\uDD4B\uD835\uDD59\uD835\uDD56%20\uD835\uDD62\uD835\uDD66\uD835\uDD5A\uD835\uDD54\uD835\uDD5C%20\uD835\uDD53\uD835\uDD63\uD835\uDD60\uD835\uDD68\uD835\uDD5F%20\uD835\uDD57\uD835\uDD60\uD835\uDD69%20\uD835\uDD5B\uD835\uDD66\uD835\uDD5E\uD835\uDD61\uD835\uDD64%20\uD835\uDD60\uD835\uDD67\uD835\uDD56\uD835\uDD63%20\uD835\uDD65\uD835\uDD59\uD835\uDD56%20\uD835\uDD5D\uD835\uDD52\uD835\uDD6B\uD835\uDD6A%20\uD835\uDD55\uD835\uDD60\uD835\uDD58",
             "file:/home/rhubner/ffmpeg_build/strange%20filenames/%F0%9D%95%8B%F0%9D%95%99%F0%9D%95%96%20%F0%9D%95%A2%F0%9D%95%A6%F0%9D%95%9A%F0%9D%95%94%F0%9D%95%9C%20%F0%9D%95%93%F0%9D%95%A3%F0%9D%95%A0%F0%9D%95%A8%F0%9D%95%9F%20%F0%9D%95%97%F0%9D%95%A0%F0%9D%95%A9%20%F0%9D%95%9B%F0%9D%95%A6%F0%9D%95%9E%F0%9D%95%A1%F0%9D%95%A4%20%F0%9D%95%A0%F0%9D%95%A7%F0%9D%95%96%F0%9D%95%A3%20%F0%9D%95%A5%F0%9D%95%99%F0%9D%95%96%20%F0%9D%95%9D%F0%9D%95%92%F0%9D%95%AB%F0%9D%95%AA%20%F0%9D%95%95%F0%9D%95%A0%F0%9D%95%98"},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/....", "file:/home/rhubner/ffmpeg_build/strange%20filenames/...."},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/%5C.%5C.", "file:/home/rhubner/ffmpeg_build/strange%20filenames/%5C.%5C."},
            {"file:/home/rhubner/ffmpeg_build/strange%20filenames/......", "file:/home/rhubner/ffmpeg_build/strange%20filenames/......"}
    };

    @Test
    public void testMore() throws URISyntaxException {
        for(String[] pattern: TEST_PATTERNS) {
            URI uri = new URI(pattern[0]);
            String result = DroidUrlFormat.format(uri);
            assertThat(result, equalTo(pattern[1]));

        }
    }
}