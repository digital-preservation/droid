/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.gov.nationalarchives.droid.container;

import java.io.IOException;
import java.io.InputStream;
import uk.gov.nationalarchives.droid.core.IdentificationRequestByteReaderAdapter;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

/**
 *
 * @author rbrennan
 */
public abstract class AbstractIdentifierEngine implements IdentifierEngine {
    
    private IdentificationRequestFactory requestFactory;
    
    /**
     * Returns a ByteReader for the input stream supplied.
     * @param in an input stream
     * @return a Byte reader
     * @throws IOException if the input stream could not be read
     */
    protected ByteReader newByteReader(InputStream in) throws IOException {
        IdentificationRequest request = getRequestFactory().newRequest(null, null);
        request.open(in);
        return new IdentificationRequestByteReaderAdapter(request);
    }
    
     /**
     * @param requestFactory the requestFactory to set
     */
    public void setRequestFactory(IdentificationRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }
    
    /**
     * @return the requestFactory
     */
    protected IdentificationRequestFactory getRequestFactory() {
        return requestFactory;
    }
}
