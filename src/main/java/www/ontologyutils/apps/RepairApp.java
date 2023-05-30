package www.ontologyutils.apps;

import java.util.*;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import openllet.owlapi.OpenlletReasonerFactory;
import uk.ac.manchester.cs.factplusplus.owlapi.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import www.ontologyutils.normalization.NnfNormalization;
import www.ontologyutils.normalization.SroiqNormalization;
import www.ontologyutils.repair.*;
import www.ontologyutils.toolbox.*;

/**
 * Repair the given ontology using a random maximal consistent subset.
 */
public abstract class RepairApp extends App {
    private String inputFile;
    private String outputFile = null;
    private boolean normalizeSroiq = false;
    private boolean normalizeNnf = false;
    private boolean repair = true;
    private OWLReasonerFactory reasonerFactory = new FaCTPlusPlusReasonerFactory();
    private int verbose = 0;

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
        options.add(OptionType.FLAG.create('n', "normalize", b -> normalizeSroiq = true,
                "normalize the ontology before repair"));
        options.add(OptionType.FLAG.create("normalize-nnf", b -> normalizeSroiq = true,
                "normalize the ontology to NNF before repair"));
        options.add(OptionType.FLAG.create('R', "no-repair", b -> repair = false, "no not perform repair"));
        options.add(OptionType.FLAG.create('v', "verbose", b -> verbose = Integer.max(1, verbose),
                "print more information"));
        options.add(OptionType.FLAG.create('V', "extra-verbose", b -> verbose = 2, "print even more information"));
        options.add(OptionType.options(
                Map.of("hermit", new ReasonerFactory(),
                        "jfact", new JFactFactory(),
                        "openllet", OpenlletReasonerFactory.getInstance(),
                        "fact++", new FaCTPlusPlusReasonerFactory()))
                .create("reasoner", r -> reasonerFactory = r, "the reasoner to use"));
        return options;
    }

    /**
     * @return A repair object that has the desired effect.
     */
    protected abstract OntologyRepair getRepair();

    @Override
    protected void run() {
        var startTime = System.nanoTime();
        var ontology = Ontology.loadOntology(inputFile, reasonerFactory);
        System.err.println("Loaded...");
        if (normalizeNnf) {
            var normalization = new NnfNormalization();
            System.err.println("Normalizing to NNF...");
            normalization.apply(ontology);
        }
        if (normalizeSroiq) {
            var normalization = new SroiqNormalization(true, true);
            System.err.println("Normalizing to SROIQ...");
            normalization.apply(ontology);
        }
        if (repair) {
            var repair = getRepair();
            if (verbose >= 1) {
                repair.setInfoCallback(this::logMessage);
            }
            System.err.println("Repairing...");
            repair.apply(ontology);
            System.err.println("Repaired.");
        }
        if (verbose >= 2) {
            System.err.println("=== BEGIN RESULT ===");
            ontology.refutableAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                    .sorted().forEach(System.out::println);
            ontology.staticAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                    .sorted().forEach(System.out::println);
            assert ontology.isConsistent();
            System.err.println("==== END RESULT ====");
        }
        if (outputFile != null) {
            ontology.saveOntology(outputFile);
            System.err.println("Saved result.");
        }
        ontology.close();
        var endTime = System.nanoTime();
        System.err.println(
                "Done. (" + (endTime - startTime) / 1_000_000 + " ms; " + Ontology.reasonerCalls + " reasoner calls)");
    }
}
