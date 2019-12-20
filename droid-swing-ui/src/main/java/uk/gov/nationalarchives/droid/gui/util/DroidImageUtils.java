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
package uk.gov.nationalarchives.droid.gui.util;

import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Some bespoke image utils.
 *
 */
public final class DroidImageUtils {

    /**
     * Default width and height for small icons.
     */
    public static final int SMALL_ICON_WIDTH_HEIGHT = 16;

    /**
     * Default width and height for large icons.
     */
    public static final int LARGE_ICON_WIDTH_HEIGHT = 24;

    private DroidImageUtils() { }

    /**
     * create image based on all existing resolutions (to manage high screen res screens)
     * @param path path of the image
     * @param baseName base name of the image
     * @param type type of icon SMALL/LARGE
     * @return the scaled image
     */
    public static ImageIcon createBaseMultiResolutionImage(String path, String baseName, IconType type) {
        java.util.List<String> imageLocations;
        switch (type){
            default:
            case LARGE:
                imageLocations = List.of(
                        path + "24x24-" + baseName,
                        path + "48x48-" + baseName,
                        path +  "96x96-" + baseName
                );
                break;
            case SMALL:
                imageLocations = List.of(
                        path + "16x16-" + baseName,
                        path + "32x32-" + baseName,
                        path + "64x64-" + baseName
                );
                break;
        }

        List<Image> imgList = new ArrayList<Image>();
        for(String location: imageLocations) {
            Image currentImg = null;
            try {
                currentImg = ImageIO.read(new File(DroidImageUtils.class.getResource(location).toURI()));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
            imgList.add(currentImg);
        }

        return new ImageIcon(new BaseMultiResolutionImage(imgList.toArray(new Image[0])));
    }

}
