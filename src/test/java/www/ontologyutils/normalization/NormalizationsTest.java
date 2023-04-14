package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Unit tests for Tools.
 */
public class NormalizationsTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "../catsandnumbers.owl", "../bodysystem.owl", "../bfo.owl", "../apo.owl", "../aeo.owl", "../duo.owl",
    })
    public void testNormalizeCondor(final String resourceName) throws OWLOntologyCreationException {
        FreshAtoms.resetFreshAtomsEquivalenceAxioms();
        final var path = TBoxSubclassOfNormalizationTest.class.getResource(resourceName).getFile();
        try (final var ontology = Ontology.loadOntology(path)) {
            Ontology copy = Ontology.emptyOntology();
            copy.addAxioms(ontology.axioms());
            List<OWLAxiom> tBoxAxioms = copy.tboxAxioms().toList();
            tBoxAxioms.forEach((ax) -> {
                copy.replaceAxiom(ax, NormalizationTools.asSubClassOfAxioms(ax));
            });
            Ontology condor = Normalization.normalizeCondor(copy);
            assertTrue(ontology.axioms().allMatch(ax -> copy.isEntailed(ax)));
            copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
            assertTrue(condor.axioms().allMatch(ax -> condor.isEntailed(ax)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../catsandnumbers.owl", "../bodysystem.owl", "../bfo.owl", "../apo.owl", "../aeo.owl", "../duo.owl",
    })
    public void testNormalizeNaive(final String resourceName) throws OWLOntologyCreationException {
        FreshAtoms.resetFreshAtomsEquivalenceAxioms();
        final var path = TBoxSubclassOfNormalizationTest.class.getResource(resourceName).getFile();
        try (final var ontology = Ontology.loadOntology(path)) {
            Ontology copy = Ontology.emptyOntology();
            copy.addAxioms(ontology.axioms());
            List<OWLAxiom> tBoxAxioms = copy.tboxAxioms().toList();
            tBoxAxioms.forEach((ax) -> {
                copy.replaceAxiom(ax, NormalizationTools.asSubClassOfAxioms(ax));
            });
            Ontology condor = Normalization.normalizeNaive(copy);
            assertTrue(ontology.axioms().allMatch(ax -> copy.isEntailed(ax)));
            copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
            assertTrue(condor.axioms().allMatch(ax -> condor.isEntailed(ax)));
        }
    }
}
