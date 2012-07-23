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
package uk.gov.nationalarchives.droid.gui.treemodel;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author rflitcroft
 * 
 */
//CHECKSTYLE:OFF - too much coupling.
public enum OutlineColumn {
//CHECKSTYLE:ON
    /** Extension column. */
    EXTENSION {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableString> getColumnClass() {
            return DirectoryComparableString.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Extension";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {
            return new DirectoryComparableString(node.getMetaData().getExtension(), node);
        }
        
        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new FileExtensionRenderer(backColor);
        }
    },
    
    /** Size column. */
    SIZE {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableLong> getColumnClass() {
            return DirectoryComparableLong.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Size";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {
            return new DirectoryComparableLong(node.getMetaData().getSize(), node);
        }

        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new FileSizeRenderer(backColor);
        }
    },    

    /** Date column. */
    DATE {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableDate> getColumnClass() {
            return DirectoryComparableDate.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Last modified";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {
            return new DirectoryComparableDate(node.getMetaData().getLastModifiedDate(), node);
        }
        
        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new DefaultCellRenderer(backColor, SwingConstants.CENTER);
        }
    },

    
    /** The number of identifications made for a node. */
    IDENTIFICATION_COUNT {
        @Override
        public Class<?> getColumnClass() {
            return DirectoryComparableLong.class;
        }
        
        @Override
        public String getName() {
            return "Ids";
        }
        
        @Override
        public Object getValue(ProfileResourceNode node) {
            final Integer identificationCount = node.getIdentificationCount();
            return new DirectoryComparableLong(identificationCount, node);
        }
        
        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new FormatCountRenderer(backColor);
        }
    },

    /** Format column. */
    FORMAT {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableString> getColumnClass() {
            return DirectoryComparableString.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Format";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {
            String formatValue = null;

            List<Format> formatIdentifications = node.getFormatIdentifications();
            if (formatIdentifications.size() == 1) {
                formatValue = formatIdentifications.get(0).getName();
            } else if (formatIdentifications.size() > 1) {
                Set<String> formatNames = new HashSet<String>();
                for (Format f : formatIdentifications) {
                    String name = f.getName().trim();
                    if (!name.isEmpty()) {
                        formatNames.add(name);
                    }
                }
                formatValue = getMultiValuedString(formatNames);
            }
            return new DirectoryComparableString(formatValue, node);
        }
        
        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new DefaultCellRenderer(backColor, SwingConstants.LEFT);
        }
    },
    
    /** The version. */
    VERSION {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableString> getColumnClass() {
            return DirectoryComparableString.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Version";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {

            String version = null;

            List<Format> formatIdentifications = node.getFormatIdentifications();
            if (formatIdentifications.size() == 1) {
                version = formatIdentifications.get(0).getVersion();
            } else if (formatIdentifications.size() > 1) {
                Set<String> formatNames = new HashSet<String>();
                for (Format f : formatIdentifications) {
                    final String ver = f.getVersion().trim();
                    if (!ver.isEmpty()) {
                        formatNames.add(ver);
                    }
                }
                version = getMultiValuedString(formatNames);
            }
            return new DirectoryComparableString(version, node);
        }
        
        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new DefaultCellRenderer(backColor, SwingConstants.CENTER);
        }
    },    

   
    /** The mime type. */
    MIME_TYPE {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableString> getColumnClass() {
            return DirectoryComparableString.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Mime type";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {

            String mimeType = null;

            List<Format> formatIdentifications = node.getFormatIdentifications();
            if (formatIdentifications.size() == 1) {
                mimeType = formatIdentifications.get(0).getMimeType();
            } else if (formatIdentifications.size() > 1) {
                Set<String> formatNames = new HashSet<String>();
                for (Format f : formatIdentifications) {
                    final String mime = f.getMimeType().trim();
                    if (!mime.isEmpty()) {
                        formatNames.add(mime);
                    }
                }
                mimeType = getMultiValuedString(formatNames);
            }            
            return new DirectoryComparableString(mimeType, node);
        }
        
        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new DefaultCellRenderer(backColor, SwingConstants.LEFT);
        }
    },
    

    /** The PUID. */
    PUID {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableString> getColumnClass() {
            return DirectoryComparableString.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "PUID";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {
            Format format = null;
            String puid = "";
            List<Format> formatIdentifications = node.getFormatIdentifications();
            if (formatIdentifications.size() == 1) {
                format = formatIdentifications.get(0);
                puid = format.getPuid();
            } else if (formatIdentifications.size() > 1) {
                Set<String> formatNames = new HashSet<String>();
                for (Format f : formatIdentifications) {
                    final String p = f.getPuid();
                    if (!p.isEmpty()) {
                        formatNames.add(p);
                    }
                }
                puid = getMultiValuedString(formatNames);
            }            
            return new DirectoryComparableString(puid, node);
        }
        
        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new HyperlinkRenderer(backColor, SwingConstants.CENTER);
        }
    },

    /** The identification method. */
    IDENTIFICATION_METHOD {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableString> getColumnClass() {
            return DirectoryComparableString.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Method";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {
            IdentificationMethod method = node.getMetaData().getIdentificationMethod();
            return new DirectoryComparableString(method == null ? "" : method.getMethod(), node);
        }

        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new DefaultCellRenderer(backColor);
        }
    },
    
    /** The identification method. */
    HASH {
        /** {@inheritDoc} */
        @Override
        public Class<DirectoryComparableString> getColumnClass() {
            return DirectoryComparableString.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "Hash";
        }

        /** {@inheritDoc} */
        @Override
        public Object getValue(ProfileResourceNode node) {
            String hash = node.getMetaData().getHash();
            return new DirectoryComparableString(hash == null ? "" : hash, node);
        }

        @Override
        public TableCellRenderer getRenderer(Color backColor) {
            return new DefaultCellRenderer(backColor);
        }
    };    
    
    private static String getMultiValuedString(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String separator = "";
        String format = values.size() > 1 ? "%s\"%s\"" : "%s%s";
        for (String value : values) {
            builder.append(String.format(format, separator, value));
            separator = ", ";
        }
        return builder.toString();
    }
    


    /**
     * @return the Class that the column will represent.
     */
    public abstract Class<?> getColumnClass();

    /**
     * @return the column heading.
     */
    abstract String getName();

    /**
     * @param node
     *            the node whose value we want.
     * @return the value for the column.
     */
    abstract Object getValue(ProfileResourceNode node);
    
    /**
     * @param backColor - the background colour to render the table cell in by default.
     * @return the appropriate cell renderer, or null if there isn't one.
     */
    public abstract TableCellRenderer getRenderer(Color backColor);

}
