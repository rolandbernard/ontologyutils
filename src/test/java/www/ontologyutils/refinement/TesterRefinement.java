package www.ontologyutils.refinement;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import www.ontologyutils.toolbox.Utils;

public class TesterRefinement  extends TestCase {

	private static final String OWL_FILE_PATH = "resources/catsandnumbers.owl";
	static OWLOntology ontology;
	static Covers covers;
	static RefinementOperator generalisation;
	static RefinementOperator specialisation;
	
    public TesterRefinement( String testName )
    {
        super( testName );
        
        ontology = Utils.newOntology(OWL_FILE_PATH);
        covers = new Covers(ontology);
        generalisation = new RefinementOperator(ontology, 
        		covers.getUpCoverOperator(), 
        		covers.getDownCoverOperator());
        specialisation = new RefinementOperator(ontology, 
        		covers.getDownCoverOperator(), 
        		covers.getUpCoverOperator());
    }

    public static Test suite()
    {
        return new TestSuite( TesterRefinement.class );
    }
	
    private static void printUpCover(OWLClassExpression e) {
		System.out.println("\n\n* UpCover of " + e);
		covers.getUpCover(e).forEach(System.out::println);
    }

    private static void printDownCover(OWLClassExpression e) {
    	System.out.println("\n\n* DownCover of " + e);
    	covers.getDownCover(e).forEach(System.out::println);
    }
    
    private static void printGeneralisations(OWLClassExpression e) {
		System.out.println("\n\n* Generalisations of " + e);
		generalisation.refine(e).forEach(System.out::println);
    }

    private static void printSpecialisations(OWLClassExpression e) {
    	System.out.println("\n\n* Specialisations of " + e);
    	specialisation.refine(e).forEach(System.out::println);
    }

    public void testApp()
    {
    		System.currentTimeMillis();
    		System.out.println("\n\n*** UPCOVERS\n\n");
    		Utils.getSubOfTBox(ontology).forEach(e -> printUpCover(e));
    		System.out.println("\n\n*** DOWNCOVERS\n\n");
    		Utils.getSubOfTBox(ontology).forEach(e -> printDownCover(e));
    		System.out.println("\n\n*** GENERALISATIONS\n\n");
    		Utils.getSubOfTBox(ontology).forEach(e -> printGeneralisations(e));
    		System.out.println("\n\n*** SPECIALISATIONS\n\n");
    		Utils.getSubOfTBox(ontology).forEach(e -> printSpecialisations(e));
    		
        assertTrue( true );
    }
    
}
