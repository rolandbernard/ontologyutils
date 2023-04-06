package www.ontologyutils.toolbox;

import java.io.File;
import java.util.*;
import java.util.stream.*;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.*;

/**
 * This class represents a wrapper used for ontologies in this package. The main
 * utility of having this wrapper is the ability to reuse reasoner and ontology
 * as much as possible. The class also provides some utility functions for
 * creating copies without certain axioms or empty ontologies.
 */
public class Ontology {
    private static final OWLReasonerFactory DEFAULT_FACTORY = new ReasonerFactory();

    private final OWLOntology ontology;
    private final OWLReasonerFactory reasonerFactory;
    private final Set<OWLLogicalAxiom> refutableAxioms;
    private OWLReasoner reasoner;

    /**
     * Create a new ontology wrapper around the given {@code OWLOntology} and
     * refutable axioms. Should the need arise to create a reasoner, use
     * {@code reasonerFactory} to create it.
     *
     * @param ontology
     * @param refutableAxioms
     * @param reasonerFactory
     */
    private Ontology(OWLOntology ontology, Set<OWLLogicalAxiom> refutableAxioms, OWLReasonerFactory reasonerFactory) {
        this.ontology = ontology;
        this.refutableAxioms = refutableAxioms;
        this.reasonerFactory = reasonerFactory;
    }

    public static Ontology wrapOntology(OWLOntology ontology, Set<OWLLogicalAxiom> refutableAxioms,
            OWLReasonerFactory reasonerFactory) {
        return new Ontology(ontology, refutableAxioms, reasonerFactory);
    }

    public static Ontology wrapOntology(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
        return new Ontology(ontology, ontology.logicalAxioms().collect(Collectors.toCollection(HashSet::new)),
                reasonerFactory);
    }

    public static Ontology emptyOntology(OWLReasonerFactory reasonerFactory) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology();
        } catch (OWLOntologyCreationException e) {
            Utils.panic(e);
        }
        return wrapOntology(ontology, reasonerFactory);
    }

    public static Ontology loadOntology(String filePath, OWLReasonerFactory reasonerFactory) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File ontologyFile = new File(filePath);
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        } catch (OWLOntologyCreationException e) {
            Utils.panic(e);
        }
        return wrapOntology(ontology, reasonerFactory);
    }

    public static Ontology loadOntology(String filePath) {
        return loadOntology(filePath, DEFAULT_FACTORY);
    }

    public Stream<OWLLogicalAxiom> logicalAxioms() {
        return ontology.logicalAxioms();
    }

    public Stream<OWLLogicalAxiom> refutableAxioms() {
        return refutableAxioms.stream();
    }

    public void removeAxiom(OWLAxiom axiom) {
        refutableAxioms.remove(axiom);
        ontology.removeAxiom(axiom);
    }

    public void addStaticAxiom(OWLAxiom axiom) {
        ontology.addAxiom(axiom);
    }

    public void addLogicalAxiom(OWLLogicalAxiom axiom) {
        refutableAxioms.add(axiom);
        ontology.addAxiom(axiom);
    }

    public Ontology withoutAxiom(OWLAxiom axiom) {
        Ontology ontology = clone();
        ontology.removeAxiom(axiom);
        return ontology;
    }

    public Ontology withStaticAxiom(OWLAxiom axiom) {
        Ontology ontology = clone();
        ontology.addStaticAxiom(axiom);
        return ontology;
    }

    public Ontology withLogicalAxiom(OWLLogicalAxiom axiom) {
        Ontology ontology = clone();
        ontology.addLogicalAxiom(axiom);
        return ontology;
    }

    public OWLOntology getOwlOntology() {
        return ontology;
    }

    public OWLDataFactory getDataFactory() {
        return ontology.getOWLOntologyManager().getOWLDataFactory();
    }

    public OWLReasoner getReasoner() {
        if (reasoner == null) {
            reasoner = reasonerFactory.createReasoner(ontology);
        }
        reasoner.flush();
        return reasoner;
    }

    @Override
    public Ontology clone() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology newOntology = null;
        try {
            newOntology = manager.copyOntology(ontology, OntologyCopy.SHALLOW);
        } catch (OWLOntologyCreationException e) {
            Utils.panic(e);
        }
        return new Ontology(newOntology, new HashSet<>(refutableAxioms), reasonerFactory);
    }

    public void dispose() {
        ontology.getOWLOntologyManager().removeOntology(ontology);
        if (reasoner != null) {
            reasoner.dispose();
            reasoner = null;
        }
    }
}
