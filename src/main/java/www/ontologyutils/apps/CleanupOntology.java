package www.ontologyutils.apps;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.normalization.SroiqNormalization;
import www.ontologyutils.toolbox.*;

/**
 * Remove all annotations, annotation axioms, and imports of the ontology.
 */
public class CleanupOntology extends App {
    private String inputFile;
    private String outputFile = null;
    private boolean normalize = false;

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
        options.add(OptionType.FLAG.create('n', "normalize", b -> normalize = true,
                "normalize the ontology beforehand"));
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
        for (var report : newOntology.checkOwlProfiles()) {
            if (!report.isInProfile() && report.getProfile().getName().endsWith("DL")) {
                for (var violation : report.getViolations()) {
                    newOntology.removeAxioms(violation.getAxiom());
                }
            }
        }
        newOntology.cleanUnnecessaryDeclarationAxioms();
        newOntology.generateDeclarationAxioms();
        System.err.println("Cleaned. (" + newOntology.logicalAxioms().count() + " axioms)");
        if (normalize) {
            var normalize = new SroiqNormalization(true, false);
            normalize.apply(newOntology);
            System.err.println("Normalized. (" + newOntology.logicalAxioms().count() + " axioms)");
        }
        if (outputFile == null) {
            outputFile = inputFile;
        }
        newOntology.saveOntology(outputFile);
        System.err.println("Saved. (" + newOntology.logicalAxioms().count() + " axioms)");
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
