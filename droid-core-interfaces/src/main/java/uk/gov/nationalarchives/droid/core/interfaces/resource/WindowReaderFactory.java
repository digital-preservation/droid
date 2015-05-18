

package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.FileNotFoundException;
import java.io.InputStream;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.InputStreamReader;

public class WindowReaderFactory {

	//TODO: This could be expended to return a range of possible WindowReaders based on an estimated
	// "best fit" from the information known about the IdentificationRequest at the outset, e.g. the
	// file type, size etc.
	public WindowReader getWindowReader(RequestIdentifier identifier, InputStream in) throws FileNotFoundException {
		//TODO: For now just call the other constructor but we would probably want to return an InputStreamReader
		// tailored to our estimated best fit - e.g. in terms of cache Size etc.
		return getWindowReader(identifier);
	}
	
	public WindowReader getWindowReader(RequestIdentifier identifier) throws FileNotFoundException {
		String filePath = identifier.getUri().toString().substring(6).replaceAll("%20", " ");
		return new FileReader(filePath);
	}
}
