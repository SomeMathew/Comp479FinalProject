package edu.comp479.crawler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.comp479.crawler.DocDiskManager;
import edu.comp479.crawler.Document;

class DocDiskManagerTest {
    private Path directory;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        directory = Paths.get("./testDump/");
        Files.createDirectories(directory);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void systemTestDumpThenRetrieve() throws IOException {
        DocDiskManager ddm = new DocDiskManager(directory);
        String title = "This is a title";
        String body = "This is a body";
        String url = "www.google.com";

        Document testDoc = new Document(title, body, url);

        String fileName = ddm.writeToDisk(testDoc);
        assertTrue(fileName != null);
        assertFalse(fileName.isEmpty());

        Document testDocRetrieve = ddm.readFromDisk(testDoc.getDocumentId());

        assertAll(() -> assertTrue(testDocRetrieve.getBody().equals(testDoc.getBody())),
                () -> assertTrue(testDocRetrieve.getDocumentId() == testDoc.getDocumentId()),
                () -> assertTrue(testDocRetrieve.getTitle().equals(testDoc.getTitle())),
                () -> assertTrue(testDocRetrieve.getUrl().equals(testDoc.getUrl())));
    }

}
