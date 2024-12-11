package uk.gov.nationalarchives.droid.container.gz;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import uk.gov.nationalarchives.droid.container.AbstractIdentifierEngine;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.archive.GZipArchiveHandler;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class GzIdentifierEngine extends AbstractIdentifierEngine {

    @Override
    public void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException {
        for (String entryName: matches.getAllFileEntries()) {
            try (GZIPInputStream stream = new GZIPInputStream(request.getSourceInputStream()); ByteReader reader = newByteReader(stream)) {
                List<ContainerSignatureMatch> matchList =
                        matches.getContainerSignatureMatches();
                for (ContainerSignatureMatch match : matchList) {
                    match.matchBinaryContent(entryName, reader);
                }
            }
        }
    }
}
