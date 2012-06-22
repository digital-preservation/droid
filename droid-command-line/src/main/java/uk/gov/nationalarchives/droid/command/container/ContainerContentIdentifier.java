package uk.gov.nationalarchives.droid.command.container;

import java.io.InputStream;
import java.io.IOException;

/**
 *
 * @author rbrennan
 */
public interface ContainerContentIdentifier {
    public void process(InputStream in) throws IOException ;
}