package www.ontologyutils.apps;

import java.util.*;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.toolbox.Ontology;
import www.ontologyutils.toolbox.Utils;

/**
 * Show the ontology.
 */
public class ShowOntology extends App {
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
        ontology.refutableAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                .sorted().forEach(System.out::println);
        ontology.staticAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                .sorted().forEach(System.out::println);
    }

    /**
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        (new ShowOntology()).launch(args);
    }
}
