package www.ontologyutils.apps;

import java.util.*;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.refinement.*;
import www.ontologyutils.toolbox.*;

public class AppBenchmark {
    private static void benchRun(Ontology ontology) {
        Utils.randomSeed(42);
        var refined = new HashSet<OWLClassExpression>();
        try (var covers = new Covers(ontology, ontology.simpleRoles().collect(Collectors.toSet()))) {
            var refinement = new RefinementOperator(covers.upCover().cached(), covers.downCover().cached());
            refined.addAll(ontology.subConcepts().toList());
            for (int i = 0; i < 100; i++) {
                var newRefined = new HashSet<OWLClassExpression>();
                refined.stream().forEach(concept -> {
                    newRefined.add(Utils.randomChoice(refinement.refine(concept)));
                    newRefined.add(Utils.randomChoice(refinement.refineReverse(concept)));
                });
                refined = newRefined;
            }
            System.out.println("reasoner calls: " + covers.reasonerCalls);
        }
        var weaker = new HashSet<OWLAxiom>();
        try (var axiomWeakener = new AxiomWeakener(ontology)) {
            weaker.addAll(ontology.logicalAxioms().toList());
            for (int i = 0; i < 1000; i++) {
                var newWeaker = new HashSet<OWLAxiom>();
                weaker.stream().forEach(strongAxiom -> {
                    newWeaker.add(Utils.randomChoice(axiomWeakener.weakerAxioms(strongAxiom)));
                });
                weaker = newWeaker;
            }
        }
    }

    private static void benchOntology(Ontology ontology) {
        var startTime = System.nanoTime();
        benchRun(ontology);
        var endTime = System.nanoTime();
        System.out.println("time to run: " + (endTime - startTime) / 1_000_000 + " ms");
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
