package www.ontologyutils.apps;

import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClassExpression;

import www.ontologyutils.refinement.*;
import www.ontologyutils.toolbox.*;

public class AppBenchmark {
    private static void benchRun(Ontology ontology) {
        Utils.randomSeed(42);
        for (int i = 0; i < 10; i++) {
            try (var covers = new Covers(ontology, ontology.simpleRoles().collect(Collectors.toSet()))) {
                var refinement = new RefinementOperator(covers.upCover().cached(), covers.downCover().cached());
                OWLClassExpression toRefine = Ontology.getDefaultDataFactory().getOWLNothing();
                for (int j = 0; j < 1_000; j++) {
                    if (toRefine.isOWLThing()) {
                        toRefine = Ontology.getDefaultDataFactory().getOWLNothing();
                    }
                    toRefine = Utils.randomChoice(refinement.refine(toRefine));
                }
            }
        }
    }

    private static void benchOntology(Ontology ontology) {
        var startTime = System.nanoTime();
        Ontology.reasonerCalls = 0;
        benchRun(ontology);
        var endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1_000_000 + " ms; " + Ontology.reasonerCalls + " reasoner calls");
    }

    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter
     * src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java " + AppBenchmark.class.getCanonicalName() + " FILENAME");
            System.exit(1);
        }
        var ontology = Ontology.loadOntology(args[0]);
        System.err.println("Loaded...");
        System.err.println("** Benchmark: FaCT++");
        try (var withFactPP = ontology.cloneWithFactPP()) {
            benchOntology(withFactPP);
        } catch (Exception e) {
            System.err.println("ERROR... " + e);
        }
        System.err.println("** Benchmark: OPENLLET");
        try (var withOpenllet = ontology.cloneWithOpenllet()) {
            benchOntology(withOpenllet);
        } catch (Exception e) {
            System.err.println("ERROR... " + e);
        }
        System.err.println("** Benchmark: FACT");
        try (var withFact = ontology.cloneWithJFact()) {
            benchOntology(withFact);
        } catch (Exception e) {
            System.out.println("ERROR... " + e);
        }
        System.err.println("** Benchmark: HERMIT");
        try (var withHermit = ontology.cloneWithHermit()) {
            benchOntology(withHermit);
        } catch (Exception e) {
            System.err.println("ERROR... " + e);
        }
        ontology.close();
    }
}
