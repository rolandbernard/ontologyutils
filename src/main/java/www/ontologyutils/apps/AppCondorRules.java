package www.ontologyutils.apps;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.normalization.*;
import www.ontologyutils.rules.RuleGeneration;
import www.ontologyutils.toolbox.*;

/**
 *
 */
public class AppCondorRules {
    private Ontology ontology;

    private AppCondorRules(String ontologyFilePath) {
        ontology = Ontology.loadOntologyWithOriginAnnotations(ontologyFilePath);
    }

    private Ontology runCondor() {
        FreshAtoms.resetFreshAtomsEquivalenceAxioms(); // optional; for verification purpose

        Ontology copy = this.ontology.clone();

        List<OWLAxiom> tBoxAxioms = Utils.toList(copy.tboxAxioms());
        tBoxAxioms.forEach((ax) -> {
            copy.removeAxioms(ax);
            copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
        });

        // var condor = Normalization.normalizeCondor(copy);
        var condor = superNormalize(Normalization.normalizeCondor(copy));

        // check every axiom of the original ontology is entailed in condor
        assert (this.ontology.axioms().allMatch(ax -> condor.isEntailed(ax)));
        // check every axiom of condor is entailed in the copy of the original ontology
        // with extended signature
        copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
        assert (condor.axioms().allMatch(ax -> copy.isEntailed(ax)));
        copy.close();
        return condor;
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
                OWLAxiom axiom = df.getOWLSubClassOfAxiom(newConj, right,
                        Utils.toList(Ontology.axiomOriginAnnotations(a)));
                res.add(axiom);
                return res;
            }

            OWLClassExpression newAtom = FreshAtoms.createFreshAtomCopy(newConj);
            leftConj.remove(one);
            leftConj.remove(two);
            leftConj.add(newAtom);

            OWLAxiom axiom = df.getOWLSubClassOfAxiom(newConj, newAtom,
                    Utils.toList(Ontology.axiomOriginAnnotations(a)));
            res.add(axiom);
        }
    }

    /**
     * @param args
     *            One argument must be given, corresponding to an OWL ontology file
     *            path. E.g., run with the parameter resources/bodysystem.owl
     */
    public static void main(String[] args) {
        AppCondorRules mApp = new AppCondorRules(args[0]);

        Ontology condor = mApp.runCondor();

        RuleGeneration rgc = new RuleGeneration(condor);
        condor.tboxAxioms().forEach(ax -> {
            System.out.println(rgc.normalizedSubClassAxiomToRule(ax));
        });

        // we write the mappings in to a file
        try {
            FileWriter write = new FileWriter("rules-mappings.txt", false);
            write.append("ENTITIES\n");
            rgc.getMapEntities().entrySet().stream().forEach(e -> {
                try {
                    write.append(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString()) + "\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            write.append("AXIOMS GROUPS\n");
            rgc.getMapAxioms().entrySet().stream().forEach(e -> {
                try {
                    write.append(e.getValue() + "\t\t");
                    e.getKey().forEach(ann -> {
                        try {
                            write.append(ann.getValue().toString());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    });
                    write.append("\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            write.flush();
            write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
