package www.ontologyutils.apps;

import java.util.stream.Collectors;

import www.ontologyutils.refinement.*;
import www.ontologyutils.toolbox.*;

public class AppBenchmark {
    private static void benchRun(Ontology ontology) {
        try (var covers = new Covers(ontology, ontology.simpleRoles().collect(Collectors.toSet()))) {
            var refinement = new RefinementOperator(covers.upCover().cached(), covers.downCover().cached());
            ontology.subConcepts().forEach(concept -> {
                refinement.refine(concept).count();
                refinement.refineReverse(concept).count();
            });
            ontology.rolesInSignature().forEach(role -> {
                refinement.refine(role).count();
                refinement.refineReverse(role).count();
            });
        }
        try (var axiomWeakener = new AxiomWeakener(ontology)) {
            ontology.logicalAxioms().forEach(strongAxiom -> {
                axiomWeakener.weakerAxioms(strongAxiom).forEach(weakAxiom -> {
                    ontology.isEntailed(weakAxiom);
                });
            });
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
            System.err.println("Usage: java " + AppAutomatedRepairRandomMCS.class.getCanonicalName() + " FILENAME");
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
