package ascob.impl.backend.file;

import ascob.file.FileStore;
import ascob.impl.file.DatabaseFileStore;
import ascob.impl.file.InternalFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;

import static org.junit.Assert.*;

@SpringBootTest
@ActiveProfiles("testjpa")
public class DatabaseFileStoreTest {

    @Autowired
    FileStore fileStore;

    @Test
    public void testFileStore() throws IOException {

        String contentToStore="ciaomiaobau";
        fileStore.store("file1", new ByteArrayInputStream(contentToStore.getBytes()));


        ByteArrayOutputStream contentRetrieved = new ByteArrayOutputStream();
        fileStore.retrieveInto("file1",contentRetrieved);
        assertArrayEquals(contentToStore.getBytes(),contentRetrieved.toByteArray());

        assertTrue(fileStore.exists("file1"));

        fileStore.delete("file1");
        assertFalse(fileStore.exists("file1"));

        assertThrows(FileNotFoundException.class, ()-> fileStore.retrieveInto("notfound", new ByteArrayOutputStream()));

    }

    @Configuration
    @EntityScan(basePackageClasses = {InternalFile.class})
    @ComponentScan(basePackageClasses = {DatabaseFileStore.class})
    @EnableAutoConfiguration
    static class SpringConfigurationClass {

    }
}
