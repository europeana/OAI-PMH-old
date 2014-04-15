import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Simo on 14-3-12.
 */
public class OwlimTest {
    public static RepositoryConnection getConnection() {
        String sesameServer = "http://localhost:8080/openrdf-sesame";
        String repositoryID = "europeana";

        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        RepositoryConnection connecton = null;

        try {
            repo.initialize();
            connecton = repo.getConnection();

        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return connecton;
    }

    public static void main(String[] args) throws IOException, RepositoryException {
        RepositoryConnection repository = getConnection();
        FileInputStream rdfStream = new FileInputStream("test.rdf");
        try {
            repository.begin();
            repository.add(rdfStream, "", RDFFormat.RDFXML);
            repository.commit();

//            Update update;
        } catch (RDFParseException e) {
            e.printStackTrace();
        } finally {
            rdfStream.close();
            repository.close();
        }


    }
}
