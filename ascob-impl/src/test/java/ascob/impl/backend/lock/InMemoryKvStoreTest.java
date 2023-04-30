package ascob.impl.backend.lock;

import ascob.impl.file.DatabaseFileStore;
import ascob.impl.file.InternalFile;
import ascob.impl.lock.InMemoryKvStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
public class InMemoryKvStoreTest {


    @Test
    public void testKvStore(@Autowired InMemoryKvStore kvStore) {

        kvStore.setKeyValue("ciao/1", "1");
        kvStore.setKeyValue("ciao/2", "2");

        assertEquals("1",kvStore.getKeyValue("ciao/1"));

        Collection<String> keys = kvStore.getKeysByPrefix("ciao/");

        assertEquals(2, keys.size());
        assertTrue(keys.contains("ciao/1"));
        assertTrue(keys.contains("ciao/2"));

        kvStore.removeKey("ciao/1");

        assertEquals(0, kvStore.getKeysByPrefix("ciao/1").size());
    }

    @Configuration
    @ComponentScan(basePackageClasses = {InMemoryKvStore.class})
    static class SpringConfigurationClass {

    }
}
