

package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.FileNotFoundException;
import java.io.InputStream;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.InputStreamReader;

public class WindowReaderFactory {

	//TODO: This could be extended to return a range of possible WindowReaders based on an estimated
	// "best fit" from the information known about the IdentificationRequest at the outset, e.g. the
	// file type, size etc.
	public WindowReader getWindowReader(RequestIdentifier identifier, InputStream in) throws FileNotFoundException {
			return new  InputStreamReader(in);
	}
	
	public WindowReader getWindowReader(RequestIdentifier identifier) throws FileNotFoundException {
		//Identifier uses URI syntax with spaces in the file path encoded to %20 - this will return a FileNotFoundException
		// if passed through as is.
		String filePath = identifier.getUri().toString().substring(6).replaceAll("%20", " ");
		return new FileReader(filePath);
	}
	
	public WindowReader getWindowReader(RequestIdentifier identifier, InputStream in, int capacity) throws FileNotFoundException {
		return new  InputStreamReader(in, 4096, capacity);
}
}
