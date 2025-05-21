/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.internal.api;

import com.sun.net.httpserver.HttpServer;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import jakarta.xml.bind.JAXBException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import uk.gov.nationalarchives.droid.core.SignatureParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * Class to create an instance of DroidAPI for testing purpose.
 * It makes use of hardcoded signature paths for current version
 */
public class DroidAPITestUtils {

    static Path signaturePath = Paths.get("../droid-results/custom_home/signature_files/DROID_SignatureFile_V120.xml");
    static Path containerPath = Paths.get("../droid-results/custom_home/container_sigs/container-signature-20240715.xml");

    public static DroidAPI createApi(URI endpointOverride) throws SignatureParseException {
        return createApi(endpointOverride, signaturePath, containerPath, null);
    }

    public static DroidAPI createApi(URI endpointOverride, List<HashAlgorithm> hashAlgorithms) throws SignatureParseException {
        return createApi(endpointOverride, signaturePath, containerPath, hashAlgorithms);
    }

    public static DroidAPI createApi(URI endpointOverride, Path signaturePath, Path containerPath, List<HashAlgorithm> hashAlgorithms) throws SignatureParseException {
        DroidAPI.DroidAPIBuilder droidAPIBuilder = DroidAPI.builder()
                .binarySignature(signaturePath)
                .containerSignature(containerPath)
                .hashAlgorithms(hashAlgorithms)
                .httpClient(HttpClient.newHttpClient());
        S3ClientBuilder builder = S3Client.builder().region(Region.EU_WEST_2);
        if(endpointOverride != null) {
            S3Client s3Client = builder.endpointOverride(endpointOverride).build();
            return droidAPIBuilder.s3Client(s3Client).build();
        }
        return droidAPIBuilder.s3Client(builder.build()).build();
    }

    static HttpServer createHttpServer() throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.createContext("/", exchange -> {
            byte[] bytes;
            if (exchange.getRequestHeaders().containsKey("Range")) {
                String range = exchange.getRequestHeaders().get("Range").getFirst();
                long size = Files.size(Paths.get(URI.create("file://" + exchange.getRequestURI().toString())));
                bytes = getBytesForRange(exchange.getRequestURI().getPath(), range);
                exchange.getResponseHeaders().add("Content-Range", range.replace("=", " ") + "/" + size);
            } else {
                bytes = Files.readAllBytes(new File(exchange.getRequestURI().getPath()).toPath());
            }
            exchange.getResponseHeaders().add("Last-Modified", "1970-01-01T00:00:00.000Z");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.close();
        });
        httpServer.bind(new InetSocketAddress(0), 0);
        httpServer.start();
        return httpServer;
    }

    private static String createContents(Path path, String fileName) {
        try {
            long size = Files.size(path);
            return "<Contents>" +
                    "<Key>" + fileName + "</Key>" +
                    "<LastModified>1970-01-01T00:00:00.000Z</LastModified>" +
                    "<Size>" + size + "</Size>" +
                    "</Contents>";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static HttpServer createS3Server() throws IOException {
        HttpServer s3Server = HttpServer.create();
        s3Server.createContext("/", exchange -> {
            Map<String, String> queryParams = URLEncodedUtils
                    .parse(exchange.getRequestURI(), Charset.defaultCharset())
                    .stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
            if (exchange.getRequestMethod().equals("GET") && queryParams.containsKey("list-type") && queryParams.get("list-type").equals("2")) {
                String fileName = queryParams.get("prefix");
                Path filePath = getFilePathFromUriPath("/" + fileName);
                StringBuilder responseBuilder = new StringBuilder("<ListBucketResult>");
                if (Files.isDirectory(filePath)) {
                    try (Stream<Path> stream = Files.walk(filePath)) {
                        stream.filter(Files::isRegularFile)
                                .forEach(path -> responseBuilder.append(createContents(path, path.toString())));
                    }
                } else {
                    responseBuilder.append(createContents(filePath, fileName));
                }

                String response = responseBuilder.append("</ListBucketResult>").toString();

                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(response.getBytes());
                responseBody.close();
            } else if (exchange.getRequestMethod().equals("HEAD")) {
                String fullPath = exchange.getRequestURI().getPath().substring(1);
                Path filePath = getFilePathFromUriPath(fullPath.substring(fullPath.indexOf("/")));
                long size = Files.size(filePath);
                exchange.getResponseHeaders().add("Content-Length", Long.toString(size));
                exchange.getResponseHeaders().add("Last-Modified", "Mon, 03 Mar 2025 17:29:48 GMT");
                exchange.sendResponseHeaders(200, -1);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write("".getBytes());
                responseBody.close();
            } else if (exchange.getRequestMethod().equals("GET")) {
                String fullPath = exchange.getRequestURI().getPath().substring(1);
                Path filePath = getFilePathFromUriPath(fullPath.substring(fullPath.indexOf("/")));
                byte[] bytes;
                if (exchange.getRequestHeaders().containsKey("Range")) {
                    String range = exchange.getRequestHeaders().get("Range").getFirst();
                    bytes = getBytesForRange(filePath.toString(), range);
                } else {
                    bytes = Files.readAllBytes(filePath);
                }
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(bytes);
                responseBody.close();
            }
        });
        s3Server.bind(new InetSocketAddress(0), 0);
        s3Server.start();
        return s3Server;
    }

    private static Path getFilePathFromUriPath(String uriPath) {
        if(FileSystems.getDefault().getSeparator().equals("\\")) {
            return Path.of(uriPath.substring(1));
        } else {
            return Path.of(uriPath);
        }
    }

    public static byte[] getBytesForRange(String filePath, String range) {
        String[] rangeArr = range.split("=")[1].split("-");
        int rangeStart = Integer.parseInt(rangeArr[0]);
        int rangeEnd = Integer.parseInt(rangeArr[1]);
        int length = rangeEnd - rangeStart + 1;
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            raf.seek(rangeStart);
            byte[] buffer = new byte[length];
            int bytesRead = raf.read(buffer);
            return bytesRead == length ? buffer : Arrays.copyOf(buffer, bytesRead);
        } catch (IOException | NegativeArraySizeException e) {
            return new byte[0];
        }
    }

    public record ContainerType(String name, String id, String puid) {}
    public record ContainerFile(ContainerType containerType, String sequence, String puid, Optional<String> path) {}

    public static String generateId() {
        return Long.toString(Math.round(Math.random() * 1000));
    }

    private static Path generateFile(String extension) {
        try {
            return Files.createTempDirectory("test").resolve("test.%s".formatted(extension));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path generateZipFile(String data, String fileName) {
        Path testFile = generateFile("zip");

        try (FileOutputStream fileOutputStream = new FileOutputStream(testFile.toFile());
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {

            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);

            zipOutputStream.write(data.getBytes());
            zipOutputStream.closeEntry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return testFile;
    }

    public static Path generateOle2File(String data, String entryName) {
        Path testFile = generateFile("ole2");
        try (POIFSFileSystem fs = new POIFSFileSystem();
             FileOutputStream fos = new FileOutputStream(testFile.toFile())) {

            fs.createDocument(new ByteArrayInputStream(data.getBytes()), entryName);

            fs.writeFilesystem(fos);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return testFile;
    }

    public static Path generateGzFile(String data) {
        Path outputFilePath = generateFile("gz");
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath.toFile());
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream)) {
            gzipOutputStream.write(data.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputFilePath;
    }

    public static DroidAPI createApiForContainer(URI endpointOverride, ContainerFile signatureFile) {
        try {
            Path containerFilePath = generateContainerSignatureFile(signatureFile);
            Path signatureFilePath = generateSignatureFile(signatureFile.puid, signatureFile.containerType);
            return createApi(endpointOverride, signatureFilePath, containerFilePath, List.of());
        } catch (ParserConfigurationException | IOException | TransformerException | JAXBException |
                 SignatureParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path generateSignatureFile(String puid, ContainerType containerType) throws ParserConfigurationException, IOException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("FFSignatureFile");
        doc.appendChild(root);

        Element fileFormatCollection = doc.createElement("FileFormatCollection");
        root.appendChild(fileFormatCollection);


        Element internalSignatureCollection = createContainerInternalSignature(doc, containerType);
        root.appendChild(internalSignatureCollection);

        Element fileFormat = doc.createElement("FileFormat");
        fileFormat.setAttribute("ID", generateId());
        fileFormat.setAttribute("PUID", puid);

        Element containerFormat = doc.createElement("FileFormat");
        containerFormat.setAttribute("ID", generateId());
        containerFormat.setAttribute("PUID", containerType.puid);

        Element internalSignatureId = doc.createElement("InternalSignatureID");
        internalSignatureId.setTextContent(containerType.id);
        containerFormat.appendChild(internalSignatureId);

        fileFormatCollection.appendChild(fileFormat);
        fileFormatCollection.appendChild(containerFormat);

        return getXmlFile(doc, "signatures");
    }

    private static Element createContainerInternalSignature(Document doc, ContainerType containerType) {
        String sequenceText = switch (containerType.puid) {
            case "x-fmt/266" -> "1F8B08";
            case "fmt/189" -> "5B436F6E74656E745F54797065735D2E786D6C20A2";
            case "x-fmt/263" -> "504B0304";
            case "fmt/111" -> "D0CF11E0A1B11AE1";
            default -> throw new RuntimeException("Unknown container type: " + containerType.name);
        };

        Element internalSignatureCollection = doc.createElement("InternalSignatureCollection");

        Element internalSignature = doc.createElement("InternalSignature");
        internalSignature.setAttribute("ID", containerType.id);

        Element byteSequence = doc.createElement("ByteSequence");
        byteSequence.setAttribute("Reference", "BOFoffset");

        Element subsequence = doc.createElement("SubSequence");
        subsequence.setAttribute("MinFragLength", "0");
        subsequence.setAttribute("Position", "1");
        subsequence.setAttribute("SubSeqMaxOffset", "0");
        subsequence.setAttribute("SubSeqMinOffset", "0");

        Element sequence = doc.createElement("Sequence");
        sequence.setTextContent(sequenceText);

        subsequence.appendChild(sequence);
        byteSequence.appendChild(subsequence);
        internalSignature.appendChild(byteSequence);
        internalSignatureCollection.appendChild(internalSignature);

        return internalSignatureCollection;
    }

    private static Path generateContainerSignatureFile(ContainerFile signatureFile) throws JAXBException, IOException, TransformerException, ParserConfigurationException {
        String signatureId = generateId();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Root element
        Element root = doc.createElement("ContainerSignatureMapping");
        root.setAttribute("schemaVersion", "1.0");
        root.setAttribute("signatureVersion", "38");
        doc.appendChild(root);

        // ContainerSignatures
        Element containerSignatures = doc.createElement("ContainerSignatures");
        root.appendChild(containerSignatures);

        // ContainerSignature
        Element containerSignature = doc.createElement("ContainerSignature");
        containerSignature.setAttribute("Id", signatureId);
        containerSignature.setAttribute("ContainerType", signatureFile.containerType.name);
        containerSignatures.appendChild(containerSignature);

        // Files
        Element files = doc.createElement("Files");
        containerSignature.appendChild(files);

        // File
        Element file = doc.createElement("File");
        files.appendChild(file);

        if (signatureFile.path.isPresent()) {
            Element path = doc.createElement("Path");
            path.setTextContent(signatureFile.path.get());
            file.appendChild(path);
        }

        // BinarySignatures
        Element binarySignatures = doc.createElement("BinarySignatures");
        file.appendChild(binarySignatures);

        // InternalSignatureCollection
        Element internalSignatureCollection = doc.createElement("InternalSignatureCollection");
        binarySignatures.appendChild(internalSignatureCollection);

        // InternalSignature
        Element internalSignature = doc.createElement("InternalSignature");
        internalSignature.setAttribute("ID", signatureId);
        internalSignatureCollection.appendChild(internalSignature);

        // ByteSequence
        Element byteSequence = doc.createElement("ByteSequence");
        byteSequence.setAttribute("Reference", "BOFoffset");
        internalSignature.appendChild(byteSequence);

        // SubSequence
        Element subSequence = doc.createElement("SubSequence");
        subSequence.setAttribute("Position", "1");
        subSequence.setAttribute("SubSeqMinOffset", "0");
        subSequence.setAttribute("SubSeqMaxOffset", "1024");
        byteSequence.appendChild(subSequence);

        // Sequence
        Element sequence = doc.createElement("Sequence");
        sequence.setTextContent("'%s'".formatted(signatureFile.sequence));
        subSequence.appendChild(sequence);

        // FileFormatMappings
        Element fileFormatMappings = doc.createElement("FileFormatMappings");
        root.appendChild(fileFormatMappings);

        // FileFormatMapping
        Element fileFormatMapping = doc.createElement("FileFormatMapping");
        fileFormatMapping.setAttribute("signatureId", signatureId);
        fileFormatMapping.setAttribute("Puid", signatureFile.puid);
        fileFormatMappings.appendChild(fileFormatMapping);

        // TriggerPuids
        Element triggerPuids = doc.createElement("TriggerPuids");
        root.appendChild(triggerPuids);

        // TriggerPuid
        Element triggerPuid = doc.createElement("TriggerPuid");
        triggerPuid.setAttribute("ContainerType", signatureFile.containerType.name);
        triggerPuid.setAttribute("Puid", signatureFile.containerType.puid);
        triggerPuids.appendChild(triggerPuid);

        return getXmlFile(doc, "containers");
    }

    private static Path getXmlFile(Document doc, String fileType) throws IOException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        Path containersDirectory = Files.createTempDirectory(fileType);
        Path containersFile = containersDirectory.resolve(fileType + ".xml");
        DOMSource source = new DOMSource(doc);
        FileWriter writer = new FileWriter(containersFile.toString());
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return containersFile;
    }
}
