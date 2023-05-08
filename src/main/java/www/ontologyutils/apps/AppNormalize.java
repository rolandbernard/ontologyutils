package www.ontologyutils.apps;

import java.io.File;
import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import www.ontologyutils.normalization.*;
import www.ontologyutils.rules.RuleGeneration;
import www.ontologyutils.toolbox.*;

public class AppNormalize {
    private OWLOntology ontology;
    private String ontologyName;

    public AppNormalize(String ontologyFilePath) {
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

        Ontology copy = Ontology.emptyOntology();
        copy.addAxioms(this.ontology.axioms());

        List<OWLAxiom> tBoxAxioms = copy.tboxAxioms().toList();
        tBoxAxioms.forEach((ax) -> {
            copy.removeAxioms(ax);
            copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
        });

        System.out.println("\nCondor Normalized TBox");
        Ontology condor = Normalization.normalizeCondor(copy);

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

        Ontology copy = Ontology.emptyOntology();
        copy.addAxioms(this.ontology.axioms());

        List<OWLAxiom> tBoxAxioms = copy.tboxAxioms().toList();
        tBoxAxioms.forEach((ax) -> {
            copy.removeAxioms(ax);
            copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
        });

        System.out.println("\nNaive Normalized TBox");
        Ontology naive = Normalization.normalizeNaive(copy);

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
     * @param args
     *            One argument must be given, corresponding to an OWL ontology file
     *            path. E.g., run with the parameter resources/bodysystem.owl
     */
    public static void main(String[] args) {
        AppNormalize mApp = new AppNormalize(args[0]);

        System.out.println("\nOriginal TBox");
        // Utils.printTBox(mApp.ontology);
        mApp.ontology.tboxAxioms(Imports.INCLUDED)
                .map(OWLAxiom::toString).map(Utils::pretty)
                .forEach(System.out::println);

        ///////////////////////////////////////////////////////////////////////////////////

        System.out.println("\nNAIVE NORMALIZATION");
        Ontology naive = mApp.runNaive();

        System.out.println("\nTo rules");
        RuleGeneration rgn = new RuleGeneration(naive);
        rgn.getMapEntities().entrySet().stream()
                .forEach(e -> System.out.println(rgn.entityToRule(e.getKey())));
        naive.tboxAxioms().forEach(ax -> System.out.println(rgn.normalizedSubClassAxiomToRule(ax)));

        System.out.println("\nwhere");
        rgn.getMapEntities().entrySet().stream()
                .forEach(e -> System.out.println(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString())));

        ///////////////////////////////////////////////////////////////////////////////////

        System.out.println("\nCONDOR NORMALIZATION");
        Ontology condor = mApp.runCondor();

        System.out.println("\nTo rules");
        RuleGeneration rgc = new RuleGeneration(condor);
        rgc.getMapEntities().entrySet().stream()
                .forEach(e -> System.out.println(rgc.entityToRule(e.getKey())));
        condor.tboxAxioms().forEach(ax -> System.out.println(rgc.normalizedSubClassAxiomToRule(ax)));

        System.out.println("\nwhere");
        rgc.getMapEntities().entrySet().stream()
                .forEach(e -> System.out.println(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString())));

        ///////////////////////////////////////////////////////////////////////////////////

        System.out.println("\nFinished.");
    }
}
