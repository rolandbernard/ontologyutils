package www.ontologyutils.apps;

import org.semanticweb.owlapi.model.OWLClassExpression;

import www.ontologyutils.refinement.*;
import www.ontologyutils.toolbox.*;

/**
 * Small benchmark for computing repeated refinements on the same ontology.
 * Mainly useful for profiling and comparing performance of reasoners.
 */
public class AppBenchmark {
    private static void benchRun(Ontology ontology) {
        Utils.randomSeed(42);
        for (int i = 0; i < 10; i++) {
            if (ontology.isConsistent()) {
                var covers = new Covers(ontology, Utils.toSet(ontology.subConcepts()),
                        Utils.toSet(ontology.simpleRoles()));
                var refinement = new RefinementOperator(covers.upCover().cached(), covers.downCover().cached());
                for (int j = 0; j < 10; j++) {
                    OWLClassExpression toRefine = Ontology.getDefaultDataFactory().getOWLNothing();
                    for (int k = 0; k < 10; k++) {
                        toRefine = Utils.randomChoice(refinement.refine(toRefine));
                    }
                }
            } else {
                var mcs = Utils.randomChoice(ontology.someMaximalConsistentSubsets(Ontology::isConsistent));
                var refOnto = ontology.cloneWithRefutable(mcs);
                var covers = new Covers(refOnto, Utils.toSet(ontology.subConcepts()),
                        Utils.toSet(ontology.simpleRoles()));
                var refinement = new RefinementOperator(covers.upCover().cached(), covers.downCover().cached());
                for (int j = 0; j < 10; j++) {
                    var badAxioms = ontology.someMinimalUnsatisfiableSubsets(Ontology::isConsistent)
                            .flatMap(s -> s.stream());
                    OWLClassExpression toRefine = Utils
                            .randomChoice(Utils.randomChoice(badAxioms).nestedClassExpressions());
                    for (int k = 0; k < 10; k++) {
                        toRefine = Utils.randomChoice(refinement.refine(toRefine));
                    }
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
     *            Must contain one argument representing the file path of an
     *            ontology.
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
