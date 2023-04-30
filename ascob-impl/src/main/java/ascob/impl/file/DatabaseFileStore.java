package ascob.impl.file;

import ascob.file.FileStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class DatabaseFileStore implements FileStore {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void store(String path, InputStream inputStream) throws IOException {
        InternalFile internalFile = new InternalFile();
        internalFile.setPath(path);
        internalFile.setContent(inputStream.readAllBytes());
        entityManager.persist(internalFile);
    }

    @Override
    public void retrieveInto(String path, OutputStream outputStream) throws IOException {
        InternalFile internalFile = entityManager.find(InternalFile.class,path);
        if (internalFile==null) {
            throw new FileNotFoundException();
        }
       outputStream.write(internalFile.getContent());
    }

    @Override
    public boolean exists(String path) {
        InternalFile internalFile = entityManager.find(InternalFile.class,path);
        return internalFile !=null;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void delete(String path) {
        InternalFile internalFile = entityManager.find(InternalFile.class,path);
        if (internalFile!=null) {
            entityManager.remove(internalFile);
        }
    }
}
