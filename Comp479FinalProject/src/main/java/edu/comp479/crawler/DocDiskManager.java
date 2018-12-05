package edu.comp479.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class DocDiskManager {
    private static Logger LOGGER = Logger.getLogger(DocDiskManager.class.getName());

    public static final String FILE_PREFIX = "doc";
    public static final String FILE_SUFFIX = ".dmp";

    private final Kryo kryo;
    private final Path directory;
    private final Path lightDirectory;

    /**
     * Creates a new {@link DocDiskManager} to dump and retrieve documents from a
     * disk cache.
     * 
     * @throws IOException
     */
    public DocDiskManager(Path directory) throws IOException {
        kryo = new Kryo();
        kryo.register(Document.class);
        kryo.register(DocumentLight.class);
        Files.createDirectories(directory);
        this.directory = directory;
        this.lightDirectory = directory.resolve("light");
        Files.createDirectories(lightDirectory);
    }

    /**
     * Write a document to disk.
     * 
     * @param doc Document to write
     * @return Name of the file on disk or {@code null} on error.
     * @throws IOException
     */
    public String writeToDisk(Document doc) {
        String fileName = getFileName(doc.getDocumentId());
        Path pathToFile = directory.resolve(fileName);
        try (Output output = new Output(Files.newOutputStream(pathToFile))) {
            kryo.writeObject(output, doc);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to dump document to disk", e);
            return null;
        }

        return fileName;
    }

    /**
     * Fetch a document from disk.
     * 
     * @param docID Document Id of the document to retrieve.
     * @return The {@link Document} object or {@code null} on io error
     * @throws IOException
     */
    public Document readFromDisk(long docId) {
        String fileName = getFileName(docId);
        Path pathToFile = directory.resolve(fileName);

        Document retrievedDoc = null;
        try (Input input = new Input(Files.newInputStream(pathToFile))) {
            retrievedDoc = kryo.readObject(input, Document.class);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read document from disk", e);
            return null;
        }

        return retrievedDoc;
    }

    /**
     * Write a light document descriptor to disk.
     * 
     * @param doc {@link DocumentLight} to write
     * @return Name of the file on disk or {@code null} on error.
     * @throws IOException
     */
    public String writeToDisk(DocumentLight doc) {
        String fileName = getFileName(doc.getDocumentId());
        Path pathToFile = lightDirectory.resolve(fileName);
        try (Output output = new Output(Files.newOutputStream(pathToFile))) {
            kryo.writeObject(output, doc);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to dump document to disk", e);
            return null;
        }
    
        return fileName;
    }

    /**
     * Fetch a document from disk.
     * 
     * @param docID Document Id of the document to retrieve.
     * @return The {@link DocumentLight} object or {@code null} on io error
     * @throws IOException
     */
    public DocumentLight readLightFromDisk(long docId) {
        String fileName = getFileName(docId);
        Path pathToFile = lightDirectory.resolve(fileName);

        DocumentLight retrievedDoc = null;
        try (Input input = new Input(Files.newInputStream(pathToFile))) {
            retrievedDoc = kryo.readObject(input, DocumentLight.class);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read document from disk", e);
            return null;
        }

        return retrievedDoc;
    }

    /**
     * Get the filename for this docId.
     * 
     * @param docId The docId for the document to retrieve.
     * @return The file name for this docId.
     */
    private String getFileName(long docId) {
        return FILE_PREFIX + docId + FILE_SUFFIX;
    }

}
