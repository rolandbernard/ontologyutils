package www.ontologyutils.apps;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import www.ontologyutils.normalization.*;
import www.ontologyutils.rules.RuleGeneration;
import www.ontologyutils.toolbox.*;

/**
 * Super-normalize the ontology.
 */
public class AppSuperNormalize {
    private OWLOntology ontology;
    private String ontologyName;

    private AppSuperNormalize(String ontologyFilePath) {
        File ontologyFile = new File(ontologyFilePath);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create(ontologyFile);
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
            this.ontologyName = ontology.getOntologyID().getOntologyIRI().get().toString();
            System.out.println("Ontology " + ontologyName + " loaded.");
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Ontology runCondor() {
        FreshAtoms.resetFreshAtomsEquivalenceAxioms(); // optional; for verification purpose

        Ontology copy = Ontology.withAxiomsFrom(this.ontology);

        List<OWLAxiom> tBoxAxioms = copy.tboxAxioms().toList();
        tBoxAxioms.forEach((ax) -> {
            copy.removeAxioms(ax);
            copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
        });

        System.out.println("\nCondor Normalized TBox");
        // Ontology condor = Normalization.normalizeCondor(copy);
        Ontology condor = superNormalize(Normalization.normalizeCondor(copy));

        condor.tboxAxioms().forEach(ax -> System.out.println(Utils.pretty("-- " + ax.toString())));

        // check every axiom of the original ontology is entailed in condor
        assert (this.ontology.axioms().allMatch(ax -> condor.isEntailed(ax)));
        // check every axiom of condor is entailed in the copy of the original ontology
        // with extended signature
        copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
        assert (condor.axioms().allMatch(ax -> copy.isEntailed(ax)));
        copy.close();
        return condor;
    }

    private Ontology runNaive() {
        FreshAtoms.resetFreshAtomsEquivalenceAxioms(); // optional; for verification purpose

        Ontology copy = Ontology.withAxiomsFrom(this.ontology);

        Stream<OWLAxiom> tBoxAxioms = copy.tboxAxioms();
        tBoxAxioms.forEach((ax) -> {
            copy.removeAxioms(ax);
            copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
        });

        System.out.println("\nNaive Normalized TBox");
        // Ontology naive = Normalization.normalizeNaive(copy);
        Ontology naive = superNormalize(Normalization.normalizeNaive(copy));

        naive.tboxAxioms().forEach(ax -> System.out.println(Utils.pretty("-- " + ax.toString())));

        // check every axiom of the original ontology is entailed in naive
        assert (this.ontology.axioms().allMatch(ax -> naive.isEntailed(ax)));
        // check every axiom of naive is entailed in the copy of the original ontology
        // with extended signature
        copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
        assert (naive.axioms().allMatch(ax -> copy.isEntailed(ax)));
        copy.close();
        return naive;
    }

    /**
     * @param on
     *            an ontology in normal form
     * @return an equivalent ontology where type-1 rules have at most 2 conjuncts on
     *         the left.
     */
    private static Ontology superNormalize(Ontology on) {
        Ontology res = on.cloneOnlyStatic();
        on.tboxAxioms().forEach(a -> {
            res.addAxioms(superNormalize(a));
        });
        res.addAxioms(on.rboxAxioms());
        res.addAxioms(on.aboxAxioms());
        return res;
    }

    private static Set<OWLAxiom> superNormalize(OWLAxiom a) {
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        Set<OWLAxiom> res = new HashSet<>();
        OWLClassExpression left = ((OWLSubClassOfAxiom) a).getSubClass();
        OWLClassExpression right = ((OWLSubClassOfAxiom) a).getSuperClass();
        Set<OWLClassExpression> leftConj = left.asConjunctSet();
        if (!NormalForm.typeOneSubClassAxiom(left, right) || leftConj.size() <= 2) {
            // nothing to do
            res.add(a);
            return res;
        }
        while (true) {
            Iterator<OWLClassExpression> iter = leftConj.iterator();
            OWLClassExpression one = iter.next();
            OWLClassExpression two = iter.next();

            OWLClassExpression newConj = df.getOWLObjectIntersectionOf(one, two);
            assert (newConj.asConjunctSet().size() == 2);
            if (leftConj.size() == 2) {
                assert (!iter.hasNext());
                OWLAxiom axiom = df.getOWLSubClassOfAxiom(newConj, right, Ontology.axiomOriginAnnotations(a).toList());
                res.add(axiom);
                return res;
            }

            OWLClassExpression newAtom = FreshAtoms.createFreshAtomCopy(newConj);
            leftConj.remove(one);
            leftConj.remove(two);
            leftConj.add(newAtom);

            OWLAxiom axiom = df.getOWLSubClassOfAxiom(newConj, newAtom, Ontology.axiomOriginAnnotations(a).toList());
            res.add(axiom);
        }
    }

    /**
     * @param args
     *            One argument must be given, corresponding to an OWL ontology file
     *            path. E.g., run with the parameter resources/bodysystem.owl
     */
    public static void main(String[] args) {
        AppSuperNormalize mApp = new AppSuperNormalize(args[0]);

        System.out.println("\nOriginal TBox");
        mApp.ontology.tboxAxioms(Imports.INCLUDED)
                .map(OWLAxiom::toString).map(Utils::pretty)
                .forEach(System.out::println);

        ///////////////////////////////////////////////////////////////////////////////////

        System.out.println("\nNAIVE NORMALIZATION");
        Ontology naive = mApp.runNaive();

        System.out.println("\nTo rules");
        RuleGeneration rgn = new RuleGeneration(naive);
        rgn.getMapEntities().entrySet().stream().forEach(e -> System.out.println(rgn.entityToRule(e.getKey())));
        naive.tboxAxioms().forEach(ax -> System.out.println(rgn.normalizedSubClassAxiomToRule(ax)));

        System.out.println("\nwhere");
        rgn.getMapEntities().entrySet().stream()
                .forEach(e -> System.out.println(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString())));

        ///////////////////////////////////////////////////////////////////////////////////

        System.out.println("\nCONDOR NORMALIZATION");
        Ontology condor = mApp.runCondor();

        System.out.println("\nTo rules");
        RuleGeneration rgc = new RuleGeneration(condor);
        rgc.getMapEntities().entrySet().stream().forEach(e -> System.out.println(rgc.entityToRule(e.getKey())));
        condor.tboxAxioms().forEach(ax -> System.out.println(rgc.normalizedSubClassAxiomToRule(ax)));

        System.out.println("\nwhere");
        rgc.getMapEntities().entrySet().stream()
                .forEach(e -> System.out.println(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString())));

        ///////////////////////////////////////////////////////////////////////////////////

        System.out.println("\nFinished.");
    }
}
