Ontology Utils
==============

A suite of utility functions and applications for engineering OWL ontologies.

This is a heavily modified (nearly all the original code has been removed or replaced) fork of the code written by Nicolas Troquard. The original code can be found [here](https://bitbucket.org/troquard/ontologyutils/). The code has been extended to handle weakening of ontologies using $\mathcal{SROIQ}$, including weakening of some role inclusion axioms.


## Getting Started

### How to Test

To run all the tests for this software, execute the Maven command `mvn test`. This will automatically run all the defined test using JUnit. Note that by default, all tests will be run also as part of the `package` target before the jar is generated.

### How to Build

The project can be build using the Maven command `mvn package`. This will create a jar in the target directory `target/ontologyutils-0.0.1.jar` and a second one in `target/shaded-ontologyutils-0.0.1.jar` with all the required dependencies included. If you want to skip running the tests, you can use `mvn package -DskipTests`.

### How to Run

After you have build the project, the generated jar files are not runnable, you will have to specify which app you want to run explicitly. You can use the Java command `java -cp target/shaded-ontologyutils-0.0.1.jar www.ontologyutils.apps.APPNAME` to run a specific app by replacing `APPNAME` with the app you want to run. e.g.,
```
java -cp target/shaded-ontologyutils-0.0.1.jar www.ontologyutils.apps.AppAutomatedRepairWeakening src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
```

### Test Ontologies

The directory `src/test/resources/www/ontologyutils/` contains test OWL ontologies.


## Some Functions

### Computing Maximally Consistent Sets

The method `MaximalConsistentSubsets#maximalConsistentSubsets` takes a set of axioms in parameter and returns the set of maximally consistent subsets.
It is an implementation of the algorithm presented in Robert Malouf's "Maximal Consistent Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.`

Additionally, the methods `MinimalSubsets#getMinimalSubset` and `MinimalSubsets#getMinimalSubsets` can be used to compute the minimal subsets satisfying some monotone predicate. This can be used to find a minimal correction subset, the complement of which is a maximally consistent subset.
`MinimalSubsets#getMinimalSubset` is based on the algorithms presented in Marques-Silva, Joao, Mikoláš Janota, and Anton Belov. "Minimal sets over monotone predicates in boolean formulae." Computer Aided Verification: 25th International Conference, CAV 2013, Saint Petersburg, Russia, July 13-19, 2013. Proceedings 25. Springer Berlin Heidelberg, 2013.
`MinimalSubsets#getMinimalSubsets` is a modified version of the MergeXplain algorithm proposed in Shchekotykhin, Kostyantyn, Dietmar Jannach, and Thomas Schmitz. "MergeXplain: Fast computation of multiple conflicts for diagnosis." Twenty-Fourth International Joint Conference on Artificial Intelligence. 2015.

### Refinement

The class `RefinementOperator` contains an implementation of the generic refinement operator presented in Nicolas Troquard, Roberto Confalonieri, Pietro Galliani, Rafael Peñaloza, Daniele Porello, Oliver Kutz: "Repairing Ontologies via Axiom Weakening", in Thirty-Second AAAI Conference on Artificial Intelligence (AAAI 2018). It can be instantiated as generalisation operator (with a reference ontology, `way` as UpCover, `back` as DownCover) or as a specialisation operator (with a reference ontology, `way` as DownCover, `back` as UpCover). The method `RefinementOperator#refine` return the concepts that immediately refine a concept via the refinement operator.

Further, the implementation has been extended to the refinement operator described in Confalonieri, R., Galliani, P., Kutz, O., Porello, D., Righetti, G., & Toquard, N. (2020). Towards even more irresistible axiom weakening. This enables refinement of roles and $\mathcal{SROIQ}$ concepts.

### Axiom Weakening

The class `AxiomWeakener` implements the axiom weakening operations presented in Troquard et al. "Repairing Ontologies via Axiom Weakening" (AAAI 2018). `AxiomWeakener:weakerAxioms` is used for getting the weaker axioms.

The axiom weakener has also been extended to operate on following the procedure in Confalonieri, R., Galliani, P., Kutz, O., Porello, D., Righetti, G., & Toquard, N. (2020). Towards even more irresistible axiom weakening. This enables refinement of roles and $\mathcal{SROIQ}$ concepts. Also, further weakening has been implemented for disjoint role assertions and role inclusion axioms.


## Some Apps

### App Example: Show Ontology

The app `AppShowOntology` allows one to display a "human readable" form of an OWL ontology.

`AppShowOntology src/test/resources/www/ontologyutils/bodysystem.owl` prints:

```
Ontology loaded.
Declaration(Class(<http://who.int/bodysystem.owl#AutonomicNervousSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#HaematopoieticSystem>))
[Truncated for README: 20 more Declaration lines]
Declaration(Class(<http://who.int/bodysystem.owl#AuditorySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#VisualSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#GenitourinarySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#FemaleGenitalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#MaleGenitalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#SceletalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#MuscularSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#NervousSystem>))
SubClassOf(<http://who.int/bodysystem.owl#AuditorySystem> <http://who.int/bodysystem.owl#BodySystem>)
[Truncated for README: 20 more SubClassOf lines]
SubClassOf(<http://who.int/bodysystem.owl#MentalSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#NutritionalSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#HaemolymphoidSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#CirculatorySystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#AutonomicNervousSystem> <http://who.int/bodysystem.owl#NervousSystem>)
SubClassOf(<http://who.int/bodysystem.owl#DigestiveSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#MuscularSystem> <http://who.int/bodysystem.owl#MusculoskeletalSystem>)
SubClassOf(<http://who.int/bodysystem.owl#RespiratorySystem> <http://who.int/bodysystem.owl#BodySystem>)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#MotoricSystem> "Motoric System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#BodySystem> "Body System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#ParasympatheticNervousSystem> "Parasympathetic Nervous System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#MaleGenitalSystem> "Male Genital System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#SceletalSystem> "Sceletal System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#PeripheralNervousSystem> "Peripheral Nervous System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#MuscularSystem> "Muscular System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#AuditorySystem> "Auditory System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#HaematopoieticSystem> "Haematopoietic System"^^xsd:string)
[Truncated for README: 20 more AnnotationAssertion lines]
```

### App Example: AppAutomatedRepairWeakening

This is an app showcasing `OntologyRepairWeakening` which is an implementation of `OntologyRepair` following closely (although not strictly) the axiom weakening approach described in Nicolas Troquard, Roberto Confalonieri, Pietro Galliani, Rafael Peñaloza, Daniele Porello, Oliver Kutz: "Repairing Ontologies via Axiom Weakening", AAAI 2018. See also `AppAutomatedRepairRandomMCS` and `AppInteractiveRepair`.

Ran with the parameter `src/test/resources/www/ontologyutils/inconsistent-leftpolicies-small.owl`, it gives:

```
Loaded... Ontology(OntologyID(OntologyIRI(<agenda:eu>) VersionIRI(<null>))) [Axioms: 10 Logical Axioms: 6] First 20 axioms: {SubClassOf(<agenda:eu#RaiseWelfare> <agenda:eu#LeftPolicy>) SubClassOf(<agenda:eu#RaiseWages> <agenda:eu#LeftPolicy>) Declaration(Class(<agenda:eu#RaiseWages>)) Declaration(NamedIndividual(<agenda:eu#Sweden>)) Declaration(Class(<agenda:eu#RaiseWelfare>)) Declaration(Class(<agenda:eu#LeftPolicy>)) ClassAssertion(<agenda:eu#RaiseWelfare> <agenda:eu#Sweden>) ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>) ClassAssertion(<agenda:eu#RaiseWages> <agenda:eu#Sweden>) DisjointUnion(<agenda:eu#LeftPolicy> <agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare> ) }
Converted ontology: 9 logical axioms:
ClassAssertion(<agenda:eu#RaiseWages> <agenda:eu#Sweden>)
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <agenda:eu#RaiseWelfare> ObjectComplementOf(<agenda:eu#RaiseWages>))
SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWages agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWages> <agenda:eu#LeftPolicy>)
ClassAssertion(<agenda:eu#RaiseWelfare> <agenda:eu#Sweden>)
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <agenda:eu#RaiseWages> ObjectComplementOf(<agenda:eu#RaiseWelfare>))
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#LeftPolicy>)
SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWelfare agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWelfare> <agenda:eu#LeftPolicy>)
ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>)
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <agenda:eu#LeftPolicy> ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>))
Repairing... 
Repaired ontology.
SubClassOf(<agenda:eu#RaiseWelfare> ObjectComplementOf(<agenda:eu#RaiseWages>))
SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWages agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWages> <agenda:eu#LeftPolicy>)
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#LeftPolicy>)
SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWelfare agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWelfare> <agenda:eu#LeftPolicy>)
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <agenda:eu#LeftPolicy> ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>))
SubClassOf(<agenda:eu#RaiseWages> ObjectComplementOf(<agenda:eu#RaiseWelfare>))
ClassAssertion(<agenda:eu#RaiseWages> <agenda:eu#Sweden>)
ClassAssertion(ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#Sweden>)
ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>)
Declaration(Class(<agenda:eu#LeftPolicy>))
Declaration(Class(<agenda:eu#RaiseWelfare>))
Declaration(Class(<agenda:eu#RaiseWages>))
Declaration(NamedIndividual(<agenda:eu#Sweden>))
Done.
```

### App Example: AppMakeInconsistent

This app uses axiom strengthening (`AxiomStrengthener`) to obtain an inconsistent ontology from a consistent one. The main intended application is to build inconsistent ontologies from real ones, used for testing repairing methods.

A first argument must be given, corresponding to an OWL ontology file path. A second argument can be given, to indicate the minimal number of strengthening iterations must be done. A third argument can be given, to indicate the minimal number of iterations needed that must be done after reaching inconsistency.

The resulting ontology is saved in a file `<filename>-made-inconsistent.owl`, when the filename of the original ontology is `<filename>.owl`.


## Acknowledgments

Have contributed to the original project: Nicolas Troquard (UNIBZ), Roberto Confalonieri (UNIBZ), Pietro Galliani (UNIBZ).

