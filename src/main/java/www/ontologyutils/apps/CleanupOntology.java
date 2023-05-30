package www.ontologyutils.apps;

import java.util.*;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.toolbox.Ontology;
import www.ontologyutils.toolbox.Utils;

/**
 * Remove all annotations, annotation axioms, and imports of the ontology.
 */
public class CleanupOntology extends App {
    private String inputFile;
    private String outputFile = null;

    @Override
    protected List<Option<?>> appOptions() {
        var options = new ArrayList<Option<?>>();
        options.addAll(super.appOptions());
        options.add(OptionType.FILE.createDefault(file -> {
            if (inputFile != null) {
                throw new IllegalArgumentException("multiple input files specified");
            }
            inputFile = file.toString();
        }, "the file containing the original ontology"));
        options.add(OptionType.FILE.create('o', "output", file -> {
            if (outputFile != null) {
                throw new IllegalArgumentException("multiple output files specified");
            }
            outputFile = file.toString();
        }, "the file to write the result to"));
        return options;
    }

    @Override
    public void run() {
        Ontology ontology = Ontology.loadOntology(inputFile);
        System.err.println("Loaded... (" + ontology.logicalAxioms().count() + " axioms)");
        Ontology newOntology = Ontology.emptyOntology();
        for (var axiom : Utils.toList(ontology.axioms())) {
            var newAxiom = axiom.<OWLAxiom>getAxiomWithoutAnnotations();
            if (!newAxiom.isAnnotationAxiom() && newAxiom.dataPropertiesInSignature().count() == 0
                    && newAxiom.datatypesInSignature().count() == 0) {
                newOntology.addAxioms(newAxiom);
            }
        }
        newOntology.generateDeclarationAxioms();
        if (outputFile != null) {
            newOntology.saveOntology(outputFile);
            System.err.println("Saved. (" + newOntology.logicalAxioms().count() + " axioms)");
        }
    }

    /**
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        (new CleanupOntology()).launch(args);
    }
}
