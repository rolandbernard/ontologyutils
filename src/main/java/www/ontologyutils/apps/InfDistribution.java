package www.ontologyutils.apps;

import java.util.*;

import org.semanticweb.owlapi.model.OWLClassExpression;

import www.ontologyutils.toolbox.*;

/**
 * Test how much the deletion of single axioms affects the inferred class
 * hierarchy.
 */
public class InfDistribution extends App {
    private String inputFile;

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
        return options;
    }

    @Override
    public void run() {
        Ontology ontology = Ontology.loadOntology(inputFile);
        var concepts = Utils.toSet(ontology.conceptsInSignature().map(c -> (OWLClassExpression) c));
        var initial = Utils.toSet(ontology.inferredSubClassAxiomsOver(concepts));
        ontology.logicalAxioms().forEach(axiom -> {
            try (var copy = ontology.clone()) {
                copy.removeAxioms(axiom);
                var without = Utils.toSet(copy.inferredSubClassAxiomsOver(concepts));
                System.out
                        .println((initial.size() - without.size()) + ";" + (without.size() / (double) initial.size()));
            }
        });
        ontology.close();
    }

    /**
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        (new InfDistribution()).launch(args);
    }
}
