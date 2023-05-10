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
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl", "/apo.owl", "/aeo.owl", "/duo.owl", "/tiny.owl"
    })
    public void testNormalizeCondor(String resourceName) throws OWLOntologyCreationException {
        FreshAtoms.resetFreshAtomsEquivalenceAxioms();
        var path = NormalizationsTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            try (Ontology copy = ontology.clone()) {
                List<OWLAxiom> tBoxAxioms = copy.tboxAxioms().toList();
                tBoxAxioms.forEach((ax) -> {
                    copy.replaceAxiom(ax, NormalizationTools.asSubClassOfAxioms(ax));
                });
                try (var norm = Normalization.normalizeCondor(copy)) {
                    assertTrue(ontology.isEntailed(copy));
                    assertTrue(copy.isEntailed(ontology));
                    copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl", "/apo.owl", "/aeo.owl", "/duo.owl", "/tiny.owl"
    })
    public void testNormalizeNaive(String resourceName) throws OWLOntologyCreationException {
        FreshAtoms.resetFreshAtomsEquivalenceAxioms();
        var path = NormalizationsTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            try (var copy = ontology.clone()) {
                List<OWLAxiom> tBoxAxioms = copy.tboxAxioms().toList();
                tBoxAxioms.forEach((ax) -> {
                    copy.replaceAxiom(ax, NormalizationTools.asSubClassOfAxioms(ax));
                });
                try (var norm = Normalization.normalizeNaive(copy)) {
                    assertTrue(ontology.isEntailed(copy));
                    assertTrue(copy.isEntailed(ontology));
                    copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
                    assertTrue(norm.isEntailed(copy));
                    assertTrue(copy.isEntailed(norm));
                }
            }
        }
    }
}
