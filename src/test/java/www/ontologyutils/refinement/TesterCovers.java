package www.ontologyutils.refinement;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import www.ontologyutils.toolbox.Utils;

/**
 * Unit test for simple App.
 */
public class TesterCovers
        extends TestCase {
    private static final String OWL_FILE_PATH = "resources/catsandnumbers.owl";
    static OWLOntology ontology;
    static Covers ro;

    public TesterCovers(String testName) {
        super(testName);

        ontology = Utils.newOntology(OWL_FILE_PATH);
        ro = new Covers(ontology);
    }

    public static Test suite() {
        return new TestSuite(TesterCovers.class);
    }

    private static void printUpCover(OWLClassExpression e) {
        System.out.println("\n\n* UpCover of " + e);
        ro.getUpCover(e).forEach(System.out::println);
    }

    private static void printDownCover(OWLClassExpression e) {
        System.out.println("\n\n* DownCover of " + e);
        ro.getDownCover(e).forEach(System.out::println);
    }

    public void testApp() {
        System.currentTimeMillis();
        System.out.println("\n\n*** UPCOVERS\n\n");
        Utils.getSubClasses(ontology).forEach(e -> printUpCover(e));
        System.out.println("\n\n*** DOWNCOVERS\n\n");
        Utils.getSubClasses(ontology).forEach(e -> printDownCover(e));

        assertTrue(true);
    }
}
