package www.ontologyutils.apps;

import java.util.*;

import www.ontologyutils.toolbox.*;

/**
 * Check the different OWL2 profiles against the given ontology. Outputting the
 * report results.
 */
public class ClassifyOntology extends App {
    private String inputFile;

    @Override
    protected List<Option<?>> appOptions() {
        var options = new ArrayList<Option<?>>();
        options.addAll(super.appOptions());
        options.add(OptionType.FILE.createDefault(file -> {
            if (inputFile != null) {
                throw new IllegalArgumentException("multiple input files specified");
            }
            inputFile = file.toString();
        }, "the file containing the original ontology"));
        return options;
    }

    @Override
    public void run() {
        var ontology = Ontology.loadOntology(inputFile).withHermit();
        System.out.println("Axioms: " + ontology.logicalAxioms().count() + "; Concept names: "
                + ontology.conceptsInSignature().count() + "; Role names: " + ontology.rolesInSignature().count()
                + "; Subconcepts: " + ontology.subConcepts().count());
        System.out.print("OWL 2 profiles: ");
        var reports = ontology.checkOwlProfiles();
        for (var report : reports) {
            if (!report.isInProfile() && report.getProfile().getName().endsWith("DL")) {
                System.out.print(report);
            }
        }
        for (var report : reports) {
            if (report.isInProfile()) {
                System.out.print(report.getProfile().getName() + "; ");
            }
        }
        System.out.println();
        System.out.print("DL languages: ");
        ontology.checkDlExpressivity().forEach(language -> {
            System.out.print(language.name() + "; ");
        });
        System.out.println();
        ontology.close();
    }

    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter resources/inconsistent-leftpolicies-small.owl
     *
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        (new ClassifyOntology()).launch(args);
    }
}
