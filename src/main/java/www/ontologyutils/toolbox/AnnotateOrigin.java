package www.ontologyutils.toolbox;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class AnnotateOrigin {

	public static final String ANN_ORIGIN = "origin";
	
	private static OWLDataFactory df = new OWLDataFactoryImpl();
	private static OWLAnnotationProperty p = df.getOWLAnnotationProperty(IRI.create(ANN_ORIGIN));
	
	public static Collection<OWLAnnotation> getAxiomAnnotations(OWLAxiom a) {
		Collection<OWLAnnotation> annotations = a.annotations().collect(Collectors.toSet());
		if (annotations.isEmpty()) {
			OWLAnnotation ann = df.getOWLAnnotation(p, IRI.create(Utils.pretty(a.toString())));
			annotations = Collections.singleton(ann);
		}
		
		return annotations;
	}
	
	public static OWLAxiom getAnnotatedAxiom(OWLAxiom a, OWLAxiom origin) {
		return a.getAnnotatedAxiom(getAxiomAnnotations(origin));
	}
	
	public static OWLOntology newOntology(String ontologyFilePath) {

		File ontologyFile = new File(ontologyFilePath);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create(ontologyFile);

		OWLOntology ontology = null;

		try {
			ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		OWLOntology annotated = Utils.newEmptyOntology();
		ontology.axioms().forEach(ax -> 
				annotated.addAxiom(getAnnotatedAxiom(ax, ax)));
		
		return annotated;
	}
	
}
