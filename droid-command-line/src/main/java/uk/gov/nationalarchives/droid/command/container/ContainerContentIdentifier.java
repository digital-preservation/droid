package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.IOException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;

/**
 *
 * @author rbrennan
 */
public interface ContainerContentIdentifier {
    
    public void init(ContainerSignatureDefinitions defs, String containerType);
    
    public void process(File file, File tmpDir) throws IOException ;
}