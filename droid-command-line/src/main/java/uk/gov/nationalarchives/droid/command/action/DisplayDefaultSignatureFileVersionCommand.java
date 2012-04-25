/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.io.PrintWriter;
import java.util.Map;

import uk.gov.nationalarchives.droid.command.i18n.I18N;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * @author rflitcroft
 *
 */
public class DisplayDefaultSignatureFileVersionCommand implements DroidCommand {
    
    private PrintWriter printWriter;
    private SignatureManager signatureManager;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws CommandExecutionException {
        try {
            Map<SignatureType, SignatureFileInfo> sigFileInfos = signatureManager.getDefaultSignatures();
            for (SignatureFileInfo info : sigFileInfos.values()) {
                printWriter.println(I18N.getResource(I18N.DEFAULT_SIGNATURE_VERSION,
                        info.getType(), info.getVersion(), info.getFile().getName()));
            }
        } catch (SignatureFileException e) {
            throw new CommandExecutionException(e);
        }
    }
    
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

}
