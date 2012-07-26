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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import uk.gov.nationalarchives.droid.command.i18n.I18N;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * @author rflitcroft
 *
 */
public class ConfigureDefaultSignatureFileVersionCommand implements DroidCommand {

    private static Map<SignatureType, DroidGlobalProperty> mapping = new HashMap<SignatureType, DroidGlobalProperty>();
    static {
        mapping.put(SignatureType.BINARY, DroidGlobalProperty.DEFAULT_BINARY_SIG_FILE_VERSION);
        mapping.put(SignatureType.CONTAINER, DroidGlobalProperty.DEFAULT_CONTAINER_SIG_FILE_VERSION);
        mapping.put(SignatureType.TEXT, DroidGlobalProperty.DEFAULT_TEXT_SIG_FILE_VERSION);
    }
    
    private PrintWriter printWriter;
    private SignatureManager signatureManager;
    private DroidGlobalConfig globalConfig;
    
    private int signatureFileVersion;
    private SignatureType type;
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void execute() throws CommandExecutionException {
        
        boolean validVersion = false;
        Map<SignatureType, SortedMap<String, SignatureFileInfo>> sigFileInfos = 
            signatureManager.getAvailableSignatureFiles();
        
        Map<String, SignatureFileInfo> sigFileInfoForType = sigFileInfos.get(type);
        
        for (Map.Entry<String, SignatureFileInfo> entry : sigFileInfoForType.entrySet()) {
            String key = entry.getKey();
            SignatureFileInfo info = entry.getValue();
            if (info.getVersion() == signatureFileVersion) {
                validVersion = true;
                updateDefaultVersion(key);
                break;
            }
        }

        if (!validVersion) {
            throw new CommandExecutionException(I18N.getResource(
                    I18N.CONFIGURE_SIGNATURE_FILE_VERSION_INVALID,
                    signatureFileVersion));
        }
    }

    /**
     * @throws CommandExecutionException
     */
    private void updateDefaultVersion(String key) throws CommandExecutionException {
        final PropertiesConfiguration properties = globalConfig.getProperties();
        properties.setProperty(mapping.get(type).getName(), key);
        try {
            properties.save();
            SignatureFileInfo sigFileInfo = signatureManager.getDefaultSignatures().get(type);
            printWriter.println(I18N.getResource(I18N.CONFIGURE_SIGNATURE_FILE_VERSION_SUCCESS,
                    sigFileInfo.getVersion(), sigFileInfo.getFile().getName()));
                    
        } catch (ConfigurationException e) {
            throw new CommandExecutionException(e);
        } catch (SignatureFileException e) {
            throw new CommandExecutionException(e);
        }
    };
    
    /**
     * @param printWriter the printWriter to set
     */
    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }
    
    /**
     * @param signatureManager the signatureManager to set
     */
    public void setSignatureManager(SignatureManager signatureManager) {
        this.signatureManager = signatureManager;
    }
    
    /**
     * @param globalConfig the globalConfig to set
     */
    public void setGlobalConfig(DroidGlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }
    
    /**
     * @param signatureFileVersion the signatureFileVersion to set
     */
    public void setSignatureFileVersion(int signatureFileVersion) {
        this.signatureFileVersion = signatureFileVersion;
    }
    
    /**
     * @param type the type to set
     */
    public void setType(SignatureType type) {
        this.type = type;
    }
}
