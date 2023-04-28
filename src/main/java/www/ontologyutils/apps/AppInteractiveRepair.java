package www.ontologyutils.apps;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.refinement.AxiomWeakener;
import www.ontologyutils.toolbox.*;

public class AppInteractiveRepair {
    Ontology ontology;

    public AppInteractiveRepair(String ontologyFilePath) {
        ontology = Ontology.loadOntology(ontologyFilePath);
    }

    /**
     * @param args
     *            One argument must be given, corresponding to an OWL ontology file
     *            path. E.g., run with the parameter
     *            resources/inconsistent-leftpolicies.owl
     */
    public static void main(String[] args) {
        final int MCS_SAMPLE_SIZE = 1;
        AppInteractiveRepair mApp = new AppInteractiveRepair(args[0]);
        System.out.println("Loaded... " + mApp.ontology);
        Set<OWLAxiom> axioms = mApp.ontology.axioms().collect(Collectors.toSet());
        Set<OWLAxiom> nonLogicalAxioms = axioms.stream().filter(ax -> !ax.isLogicalAxiom()).collect(Collectors.toSet());
        // 0- We isolate the logical axioms, and make sure the TBox axioms are all
        // subclass axioms, converting them when necessary.
        Set<OWLAxiom> logicalAxioms = new HashSet<>();// axioms.stream().filter(ax ->
                                                      // ax.isLogicalAxiom()).collect(Collectors.toSet());
        logicalAxioms.addAll(mApp.ontology.aboxAxioms().collect(Collectors.toSet()));
        logicalAxioms.addAll(mApp.ontology.rboxAxioms().collect(Collectors.toSet()));
        mApp.ontology.tboxAxioms().forEach(ax -> {
            logicalAxioms.addAll(NormalizationTools.asSubClassOfAxioms(ax));
        });
        System.out.println("Converted ontology: " + logicalAxioms.size() + " logical axioms:");
        logicalAxioms.forEach(System.out::println);

        Set<OWLAxiom> axiomsToKeep = new HashSet<>();

        // 1- Choosing a reference ontology (randomly)
        System.out.println("Searching some MCSs and electing one as reference ontology...");
        Set<Set<OWLAxiom>> mcss = MaximalConsistentSubsets.maximalConsistentSubsets(logicalAxioms, MCS_SAMPLE_SIZE);
        Ontology referenceOntology = Ontology.withAxioms(Utils.randomChoice(mcss));
        Ontology currentOntology = Ontology.emptyOntology();

        // 2- AxiomWeakener
        AxiomWeakener aw = new AxiomWeakener(referenceOntology, mApp.ontology);

        // 3- Repairing interactively
        while (!currentOntology.isConsistent()) {
            System.out.println("Looking for a bad axiom...");
            ArrayList<OWLAxiom> badAxioms = new ArrayList<OWLAxiom>(findSomehowBadAxioms(logicalAxioms, axiomsToKeep));

            // SELECT BAD AXIOM
            System.out.println("Select an axiom to weaken.");
            for (int i = 0; i < badAxioms.size(); i++) {
                System.out.println((i + 1) + "\t" + Utils.prettyPrintAxiom(badAxioms.get(i)));
            }
            System.out.print("Enter axiom number > ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int axNum = -777;
            try {
                axNum = Integer.parseInt(br.readLine()) - 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            OWLAxiom badAxiom = badAxioms.get(axNum);

            // SELECT WEAKENING
            ArrayList<OWLAxiom> weakerAxiomsAux = new ArrayList<OWLAxiom>(
                    aw.weakerAxioms((OWLSubClassOfAxiom) badAxiom).toList());
            ArrayList<OWLAxiom> weakerAxioms = new ArrayList<OWLAxiom>();
            for (OWLAxiom ax : weakerAxiomsAux) {
                weakerAxioms.add(ax.getAxiomWithoutAnnotations());
            }
            weakerAxioms.remove(badAxiom.getAxiomWithoutAnnotations());
            weakerAxioms.add(0, badAxiom.getAxiomWithoutAnnotations());

            System.out.println("Select a weakening.");
            System.out.println("0 \t[KEEP AND DO NOT ASK AGAIN]");
            for (int i = 0; i < weakerAxioms.size(); i++) {
                System.out.println((i + 1) + "\t"
                        + ((i == 0) ? "[KEEP FOR NOW]" : "" + Utils.prettyPrintAxiom(weakerAxioms.get(i))));
            }
            System.out.print("Enter axiom number > ");
            BufferedReader brW = new BufferedReader(new InputStreamReader(System.in));
            int axNumW = -777;
            try {
                axNumW = Integer.parseInt(brW.readLine()) - 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            OWLAxiom weakerAxiom = null;
            if (axNumW == -1) {
                axiomsToKeep.add(badAxiom);
                try (var ontology = Ontology.withAxioms(axiomsToKeep)) {
                    if (!ontology.isConsistent()) {
                        System.out.println("The set of axioms to keep is inconsistent; we empty it.");
                        axiomsToKeep = new HashSet<>();
                    }
                }
                System.out
                        .println("Keeping " + axiomsToKeep.size() + " axiom" + (axiomsToKeep.size() >= 2 ? "s." : "."));
                continue;
            } else {
                weakerAxiom = weakerAxioms.get(axNumW);
            }

            // we remove the bad axiom and add its weakenings
            currentOntology.replaceAxiom(badAxiom, weakerAxiom);
            logicalAxioms.remove(badAxiom);
            logicalAxioms.add(Ontology.getOriginAnnotatedAxiom(weakerAxiom, badAxiom));
            // we log the operation
            System.out.println("- Weaken: \t " + badAxiom + "\n  Into:   \t "
                    + Ontology.getOriginAnnotatedAxiom(weakerAxiom, badAxiom) + "\n");
        }

        aw.close();
        System.out.println("Repaired ontology.");
        logicalAxioms.forEach(System.out::println);
        nonLogicalAxioms.forEach(System.out::println);
        System.out.println("We specifically tried to keep the following axioms:");
        axiomsToKeep.forEach(System.out::println);
        System.out.println("Done.");
    }

    private static Set<OWLAxiom> findSomehowBadAxioms(Set<OWLAxiom> axioms, Set<OWLAxiom> axiomsToKeep) {
        Set<Set<OWLAxiom>> mcss = MaximalConsistentSubsets.maximalConsistentSubsets(axioms,
                (int) ((axioms.size() - axiomsToKeep.size()) / 4) + 1, axiomsToKeep);
        HashMap<OWLAxiom, Integer> occurences = new HashMap<>();
        for (OWLAxiom ax : axioms) {
            occurences.put(ax, 0);
        }
        for (Set<OWLAxiom> mcs : mcss) {
            mcs.stream().forEach(ax -> {
                if (!occurences.containsKey(ax)) {
                    throw new RuntimeException("Did not expect " + ax);
                }
                occurences.put(ax, occurences.get(ax) + 1);
            });
        }
        int minOcc = Integer.MAX_VALUE;
        for (OWLAxiom ax : axioms) {
            if (!occurences.containsKey(ax)) {
                throw new RuntimeException("Did not expect " + ax);
            }
            minOcc = Integer.min(minOcc, occurences.get(ax));
        }
        Set<OWLAxiom> badAxioms = new HashSet<>();
        for (OWLAxiom ax : axioms) {
            if (occurences.get(ax) == minOcc) {
                badAxioms.add(ax);
            }
        }
        if (badAxioms.size() < 1) {
            throw new RuntimeException("Did not find a bad subclass or assertion axiom in " + axioms);
        }
        return badAxioms;
    }
}
