Ontology Utils
==============

A suite of utility functions and applications for engineering OWL ontologies.

This is a heavily modified fork of the code written by Nicolas Troquard. The original code can be found [here](https://bitbucket.org/troquard/ontologyutils/). The code has been extended to handle weakening of ontologies using $\mathcal{SROIQ}$, including weakening of some role inclusion axioms.


## Getting Started

### How to Test

To run all the tests for this software, execute the Maven command `mvn test`. This will automatically run all the defined test using JUnit. Note that by default, all tests will be run also as part of the `package` target before the jar is generated.

### How to Build

The project can be build using the Maven command `mvn package`. This will create a jar in the target directory `target/ontologyutils-0.0.1.jar` with all the required code and dependencies. If you want to skip running the tests, you can use `mvn package -DskipTests`.

### How to Run

After you have build the project, the generated jar files are not runnable, you will have to specify which app you want to run explicitly. You can use the Java command `java -cp target/ontologyutils-0.0.1.jar www.ontologyutils.apps.APPNAME` to run a specific app by replacing `APPNAME` with the app you want to run. e.g.,
```
java -cp target/ontologyutils-0.0.1.jar www.ontologyutils.apps.AppAutomatedRepairWeakening src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
```

### Test ontologies

The directory `src/test/resources/www/ontologyutils/` contains test OWL ontologies.


## Some functions

### Computing maximally consistent sets

The method `MaximalConsistentSubsets#maximalConsistentSubsets` takes a set of axioms in parameter and returns the set of maximally consistent subsets.
It is an implementation of the algorithm presented in Robert Malouf's "Maximal Consistent Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.`

Additionally, the methods `MinimalSubsets#getMinimalSubset` and `MinimalSubsets#getMinimalSubsets` can be used to compute the minimal subsets satisfying some monotone predicate. This can be used to find a minimal correction subset, the complement of which is a maximally consistent subset.
`MinimalSubsets#getMinimalSubset` is based on the algorithms presented in Marques-Silva, Joao, Mikoláš Janota, and Anton Belov. "Minimal sets over monotone predicates in boolean formulae." Computer Aided Verification: 25th International Conference, CAV 2013, Saint Petersburg, Russia, July 13-19, 2013. Proceedings 25. Springer Berlin Heidelberg, 2013.
`MinimalSubsets#getMinimalSubsets` is a modified version of the MergeXplain algorithm proposed in Shchekotykhin, Kostyantyn, Dietmar Jannach, and Thomas Schmitz. "MergeXplain: Fast computation of multiple conflicts for diagnosis." Twenty-Fourth International Joint Conference on Artificial Intelligence. 2015.


### TBox Normalization

The method `Normalization#normalizeCondor` returns normalized version of the input ontology, following the procedure of Simancik et al. "Consequence-Based Reasoning beyond Horn Ontologies" (IJCAI 2011). The method `Normalization#normalizeNaive` is a more naive normalization, which normalizes every subclass axiom individually, using exclusively the method `NormalizationTools#normalizeSubClassAxiom`. If the TBox of the original ontology does not have only subclass types of axioms, some preprocessing is necessary using `NormalizationsTools#asSubClassOfAxioms`. Not every TBox can be converted.

Both normalization functions return an ontology who TBox contains only subclass axioms in normal form: A TBox axiom in normal form can be of one of four types:

* Type1: Subclass(atom or conjunction of atoms, atom or disjunction of atoms)
* Type2: Subclass(atom, exists property atom)
* Type3: Subclass(atom, forall property atom)  
* Type4: Subclass(exists property atom, atom)

Furthemore, the method `RuleGeneration#normalizedSubClassAxiomToRule` transforms a subclass axiom in normal form into a rule. See section "App example: TBox normalizations" for more details.


### Refinement

The class `RefinementOperator` contains an implementation of the generic refinement operator presented in Nicolas Troquard, Roberto Confalonieri, Pietro Galliani, Rafael Peñaloza, Daniele Porello, Oliver Kutz: "Repairing Ontologies via Axiom Weakening", in Thirty-Second AAAI Conference on Artificial Intelligence (AAAI 2018). It can be instantiated as generalisation operator (with a reference ontology, `way` as UpCover, `back` as DownCover) or as a specialisation operator (with a reference ontology, `way` as DownCover, `back` as UpCover). The method `RefinementOperator#refine` return the concepts that immediately refine a concept via the refinement operator.

Further, the implementation has been extended to the refinement operator described in Confalonieri, R., Galliani, P., Kutz, O., Porello, D., Righetti, G., & Toquard, N. (2020). Towards even more irresistible axiom weakening. This enables refinement of roles and $\mathcal{SROIQ}$ concepts.


### Axiom weakening

The class `AxiomWeakener` implements the axiom weakening operations presented in Troquard et al. "Repairing Ontologies via Axiom Weakening" (AAAI 2018). `AxiomWeakener:weakerAxioms` is used for getting the weaker axioms.

The axiom weakener has also been extended to operate on following the procedure in Confalonieri, R., Galliani, P., Kutz, O., Porello, D., Righetti, G., & Toquard, N. (2020). Towards even more irresistible axiom weakening. This enables refinement of roles and $\mathcal{SROIQ}$ concepts. Also, further weakening has been implemented for disjoint role assertions and role inclusion axioms.

### Rule generation

The method `RuleGeneration#normalizedSubClassAxiomToRule` transforms a subclass axiom in normal form into a rule. 
It takes an axiom of Type1-4 and returns a string representation. E.g.,

* Type1 axiom SubClass(Conjunction(A B), C) becomes a(1, G, a7, a12, (a13,))
* Type1 axiom SubClass(Conjunction(A B), Disjunction(C,D)) becomes a(1,G,  a7, a12, (a13, a4))
* Type1 axiom SubClass(A, Disjunction(C,D)) becomes a(1, G, a7, a7, (a13, a4))
* Type3 axiom Subclass(A, forall hasProperty B) becomes a(3, G, a7, r3, a12)

where G is an integer representing a group of axioms: two axioms belong to the same group if they result from the normalization of the same axiom in the OWL ontology.


## Some Apps

### App example: show ontology

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


### App example: TBox normalizations

We present `AppNormalize`. 

The execution of the app verbosely prints a series of successive normalizations of the ontology passed in argument.

We provide the tools to convert the TBox an ontology into a normalized form. An axiom is in normalized form when it has one of the following forms. This might change in the future.

* Type1: Subclass(atom or conjunction of atoms, atom or disjunction of atoms)
* Type2: Subclass(atom, exists property atom)
* Type3: Subclass(atom, forall property atom)  
* Type4: Subclass(exists property atom, atom)

If the TBox of the original ontology does not have only subclass types of axioms, some preprocessing is necessary using `NormalizationsTools.asSubClassOfAxioms`. Not every TBox can be converted.

`Normalization.java` contains two normalizing functions:

* `normalizeCondor` which follows the procedure of Simancik et al. "Consequence-Based Reasoning beyond Horn Ontologies" (IJCAI 2011).
* `normalizeNaive` which normalizes every subclass axiom individually, using exclusively the method `NormalizationTools.normalizeSubClassAxiom`.
 
(See also `AppSuperNormalize` which only allows Type1 axioms with at most two conjuncts at the left.)


### App example:  AppCondorRules

Running `AppCondorRules` with `src/test/resources/www/ontologyutils/catsandnumbers.owl` as argument gives this result:

```
a(2, 3, a25, r1, a7).
a(1, 11, a8, a40, (a46,)).
a(3, 9, a2, r1, a35).
a(1, 7, a45, a45, (a20,)).
a(1, 5, a7, a32, (a46,)).
a(1, 3, a33, a33, (a19,)).
a(1, 13, a40, a40, (a6,)).
a(4, 4, a47, r1, a27).
a(1, 7, a20, a20, (a34,)).
a(1, 13, a8, a40, (a46,)).
a(1, 7, a20, a20, (a24,)).
a(1, 10, a41, a41, (a21,)).
a(3, 10, a4, r2, a42).
a(4, 1, a47, r2, a29).
a(1, 7, a20, a20, (a3,)).
a(1, 10, a21, a21, (a37,)).
a(1, 7, a11, a44, (a46,)).
a(1, 14, a47, a47, (a5,)).
a(3, 14, a5, r2, a43).
a(1, 5, a32, a32, (a11,)).
a(1, 7, a47, a47, (a13, a26)).
a(1, 2, a32, a32, (a36,)).
a(1, 5, a11, a44, (a46,)).
a(2, 10, a28, r2, a9).
a(1, 5, a44, a44, (a7,)).
a(1, 3, a19, a19, (a23,)).
a(1, 3, a19, a19, (a1,)).
a(1, 11, a40, a40, (a10,)).
a(1, 10, a21, a21, (a4,)).
a(1, 3, a17, a17, (a33,)).
a(1, 7, a18, a18, (a45,)).
a(1, 3, a19, a19, (a34,)).
a(4, 3, a32, r1, a23).
a(1, 19, a38, a38, (a30,)).
a(1, 16, a31, a31, (a40,)).
a(1, 11, a43, a43, (a8,)).
a(1, 17, a39, a39, (a31,)).
a(1, 6, a37, a37, (a38,)).
a(1, 8, a35, a35, (a43,)).
a(1, 3, a7, a32, (a46,)).
a(1, 15, a34, a34, (a39,)).
a(1, 13, a6, a30, (a46,)).
a(3, 3, a1, r1, a32).
a(1, 18, a36, a36, (a35,)).
a(1, 3, a15, a34, (a17,)).
a(1, 3, a12, a23, (a15,)).
a(1, 10, a47, a47, (a14, a28)).
a(2, 7, a26, r1, a11).
a(1, 13, a30, a30, (a8,)).
a(1, 20, a44, a44, (a36,)).
a(1, 4, a27, a27, (a40,)).
a(2, 7, a24, r1, a44).
a(1, 11, a10, a43, (a46,)).
a(1, 12, a42, a42, (a43,)).
a(1, 7, a13, a24, (a16,)).
a(1, 7, a16, a34, (a18,)).
a(1, 1, a29, a29, (a47,)).
a(1, 3, a47, a47, (a12, a25)).
a(1, 10, a22, a22, (a41,)).
a(3, 7, a3, r1, a44).
a(2, 3, a23, r1, a32).
a(4, 7, a44, r1, a24).
a(1, 9, a47, a47, (a2,)).
a(1, 10, a9, a42, (a46,)).
a(1, 10, a14, a37, (a22,)).
```

and the file `rules-mappings.txt` contains:

```
ENTITIES
a37		Integer
a47		owl:Thing
a19		FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black) ObjectAllValuesFrom(hasColour Black))]
a38		Number
a5		FRESH#[ObjectAllValuesFrom(hasQuality Quality)]
a7		FRESH#[ObjectComplementOf(Black)]
a1		FRESH#[ObjectAllValuesFrom(hasColour Black)]
r2		hasQuality
a29		FRESH#[ObjectSomeValuesFrom(hasQuality owl:Thing)]
a13		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White)))]
a8		FRESH#[ObjectComplementOf(PhysicalObject)]
a45		WhiteCat
a35		Colour
a9		FRESH#[ObjectComplementOf(Primeness)]
a21		FRESH#[ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness))]
a25		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))]
a33		BlackCat
a36		GrayScale
a6		FRESH#[ObjectComplementOf(AbstractObject)]
a17		FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))) ObjectSomeValuesFrom(hasColour Black))]
a2		FRESH#[ObjectAllValuesFrom(hasColour Colour)]
r3		owl:topObjectProperty
a46		owl:Nothing
a41		PrimeNumber
a34		Cat
a32		Black
a31		Animal
a10		FRESH#[ObjectComplementOf(Quality)]
a18		FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))) ObjectSomeValuesFrom(hasColour White))]
a28		FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))]
a40		PhysicalObject
a15		FRESH#[ObjectIntersectionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black)))] FRESH#[ObjectSomeValuesFrom(hasColour Black)])]
r1		hasColour
a3		FRESH#[ObjectAllValuesFrom(hasColour White)]
a11		FRESH#[ObjectComplementOf(White)]
a23		FRESH#[ObjectSomeValuesFrom(hasColour Black)]
a24		FRESH#[ObjectSomeValuesFrom(hasColour White)]
a22		FRESH#[ObjectIntersectionOf(Integer ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))))]
a42		Primeness
a44		White
a30		AbstractObject
a43		Quality
a12		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black)))]
a39		Pet
a27		FRESH#[ObjectSomeValuesFrom(hasColour owl:Thing)]
a20		FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White) ObjectAllValuesFrom(hasColour White))]
a4		FRESH#[ObjectAllValuesFrom(hasQuality Primeness)]
a14		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness)))]
a16		FRESH#[ObjectIntersectionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White)))] FRESH#[ObjectSomeValuesFrom(hasColour White)])]
a26		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))]
AXIOMS GROUPS
7		EquivalentClasses(WhiteCat ObjectIntersectionOf(ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White)) ObjectAllValuesFrom(hasColour White)) )
20		SubClassOf(White GrayScale)
5		DisjointClasses(Black White)
18		SubClassOf(GrayScale Colour)
17		SubClassOf(Pet Animal)
10		EquivalentClasses(PrimeNumber ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness)) )
11		DisjointClasses(PhysicalObject Quality)
3		EquivalentClasses(BlackCat ObjectIntersectionOf(ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black)) ObjectAllValuesFrom(hasColour Black)) )
13		DisjointClasses(AbstractObject PhysicalObject)
1		ObjectPropertyDomain(hasQuality owl:Thing)
14		ObjectPropertyRange(hasQuality Quality)
2		SubClassOf(Black GrayScale)
15		SubClassOf(Cat Pet)
16		SubClassOf(Animal PhysicalObject)
19		SubClassOf(Number AbstractObject)
4		ObjectPropertyDomain(hasColour PhysicalObject)
9		ObjectPropertyRange(hasColour Colour)
12		SubClassOf(Primeness Quality)
6		SubClassOf(Integer Number)
8		SubClassOf(Colour Quality)
```


### App example: AppAutomatedRepairWeakening

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


### App example: AppTurnBasedMechanism

This app showcases the multiagent turn-based approach for ontology aggregation presented in Daniele Porello, Nicolas Troquard, Rafael Peñaloza, Roberto Confalonieri, Pietro Galliani, and Oliver Kutz. Two Approaches to Ontology Aggregation Based on Axiom Weakening. In 27th International Joint Conference on Artificial Intelligence and 23rd European Conference on Artificial Intelligence (IJCAI-ECAI 2018). International Joint Conferences on Artificial Intelligence Organization, 2018, pages 1942-1948.

Ran with the parameter `src/test/resources/www/ontologyutils/inconsistent-leftpolicies-small.owl`, it gives:

```
--- The ontology is not consistent.

--- Voters.
How many voters? > 2

--- Preferences.
- This is the agenda:
1 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <
agenda:eu#LeftPolicy> ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>))
2 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <
agenda:eu#RaiseWages> ObjectComplementOf(<agenda:eu#RaiseWelfare>))
3 : SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWages agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWages> <agen
da:eu#LeftPolicy>)
4 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <
agenda:eu#RaiseWelfare> ObjectComplementOf(<agenda:eu#RaiseWages>))
5 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) O
bjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#LeftPolicy>)
6 : SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWelfare agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWelfare> <
agenda:eu#LeftPolicy>)
7 : ClassAssertion(<agenda:eu#RaiseWages> <agenda:eu#Sweden>)
8 : ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>)
9 : ClassAssertion(<agenda:eu#RaiseWelfare> <agenda:eu#Sweden>)
- Preferences voter 1
- Current ranking: [0, 0, 0, 0, 0, 0, 0, 0, 0]
Next favorite axiom? > 1
- Current ranking: [1, 0, 0, 0, 0, 0, 0, 0, 0]
Next favorite axiom? > 2
- Current ranking: [1, 2, 0, 0, 0, 0, 0, 0, 0]
Next favorite axiom? > 3
- Current ranking: [1, 2, 3, 0, 0, 0, 0, 0, 0]
Next favorite axiom? > 8
- Current ranking: [1, 2, 3, 0, 0, 0, 0, 4, 0]
Next favorite axiom? > 9
- Current ranking: [1, 2, 3, 0, 0, 0, 0, 4, 5]
Next favorite axiom? > 7
- Current ranking: [1, 2, 3, 0, 0, 0, 6, 4, 5]
Next favorite axiom? > 4
- Current ranking: [1, 2, 3, 7, 0, 0, 6, 4, 5]
Next favorite axiom? > 5
- Current ranking: [1, 2, 3, 7, 8, 0, 6, 4, 5]
Next favorite axiom? > 6
- Preferences voter 1 : [1, 2, 3, 7, 8, 9, 6, 4, 5]
- Preferences voter 2
- Current ranking: [0, 0, 0, 0, 0, 0, 0, 0, 0]
Next favorite axiom? > 9
- Current ranking: [0, 0, 0, 0, 0, 0, 0, 0, 1]
Next favorite axiom? > 8
- Current ranking: [0, 0, 0, 0, 0, 0, 0, 2, 1]
Next favorite axiom? > 7
- Current ranking: [0, 0, 0, 0, 0, 0, 3, 2, 1]
Next favorite axiom? > 6
- Current ranking: [0, 0, 0, 0, 0, 4, 3, 2, 1]
Next favorite axiom? > 5
- Current ranking: [0, 0, 0, 0, 5, 4, 3, 2, 1]
Next favorite axiom? > 1
- Current ranking: [6, 0, 0, 0, 5, 4, 3, 2, 1]
Next favorite axiom? > 2
- Current ranking: [6, 7, 0, 0, 5, 4, 3, 2, 1]
Next favorite axiom? > 3
- Current ranking: [6, 7, 8, 0, 5, 4, 3, 2, 1]
Next favorite axiom? > 4
- Preferences voter 2 : [6, 7, 8, 9, 5, 4, 3, 2, 1]

--- Approvals.
- This is the agenda:
1 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <
agenda:eu#LeftPolicy> ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>))
2 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <
agenda:eu#RaiseWages> ObjectComplementOf(<agenda:eu#RaiseWelfare>))
3 : SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWages agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWages> <agen
da:eu#LeftPolicy>)
4 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <
agenda:eu#RaiseWelfare> ObjectComplementOf(<agenda:eu#RaiseWages>))
5 : SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) O
bjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#LeftPolicy>)
6 : SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWelfare agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWelfare> <
agenda:eu#LeftPolicy>)
7 : ClassAssertion(<agenda:eu#RaiseWages> <agenda:eu#Sweden>)
8 : ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>)
9 : ClassAssertion(<agenda:eu#RaiseWelfare> <agenda:eu#Sweden>)
- Approvals voter 1
Approve axiom 1? (0/1) > 1
Approve axiom 2? (0/1) > 1
Approve axiom 3? (0/1) > 1
Approve axiom 4? (0/1) > 0
Approve axiom 5? (0/1) > 1
Approve axiom 6? (0/1) > 0
Approve axiom 7? (0/1) > 0
Approve axiom 8? (0/1) > 1
Approve axiom 9? (0/1) > 1
- Approvals voter 1 : [1, 1, 1, 0, 1, 0, 0, 1, 1]
- Approvals voter 2
Approve axiom 1? (0/1) > 0
Approve axiom 2? (0/1) > 0
Approve axiom 3? (0/1) > 0
Approve axiom 4? (0/1) > 0
Approve axiom 5? (0/1) > 1
Approve axiom 6? (0/1) > 1
Approve axiom 7? (0/1) > 1
Approve axiom 8? (0/1) > 1
1Approve axiom 9? (0/1) > 
- Approvals voter 2 : [0, 0, 0, 0, 1, 1, 1, 1, 1]

--- Starting turn-based mechanism...

Current voter: 1
Next accepted favorite axiom: SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages ag
enda:eu#RaiseWelfare )>) <agenda:eu#LeftPolicy> ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>))
Adding axiom: SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWel
fare )>) <agenda:eu#LeftPolicy> ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>))
Current voter: 2
Next accepted favorite axiom: ClassAssertion(<agenda:eu#RaiseWelfare> <agenda:eu#Sweden>)
Adding axiom: ClassAssertion(<agenda:eu#RaiseWelfare> <agenda:eu#Sweden>)
Current voter: 1
Next accepted favorite axiom: SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages ag
enda:eu#RaiseWelfare )>) <agenda:eu#RaiseWages> ObjectComplementOf(<agenda:eu#RaiseWelfare>))
Adding axiom: SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWel
fare )>) <agenda:eu#RaiseWages> ObjectComplementOf(<agenda:eu#RaiseWelfare>))
Current voter: 2
Next accepted favorite axiom: ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>)
Adding axiom: ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>)
Current voter: 1
Next accepted favorite axiom: SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWages agenda:eu#LeftPolicy)>) <a
genda:eu#RaiseWages> <agenda:eu#LeftPolicy>)
Adding axiom: SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWages agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWa
ges> <agenda:eu#LeftPolicy>)
Current voter: 2
Next accepted favorite axiom: ClassAssertion(<agenda:eu#RaiseWages> <agenda:eu#Sweden>)
** Weakening. **
Adding axiom: ClassAssertion(ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#Sweden>)
Current voter: 1
Next accepted favorite axiom: SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages ag
enda:eu#RaiseWelfare )>) ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#LeftPolicy>)
Adding axiom: SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWel
fare )>) ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#LeftPolicy>)
Current voter: 2
Next accepted favorite axiom: SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWelfare agenda:eu#LeftPolicy)>) 
<agenda:eu#RaiseWelfare> <agenda:eu#LeftPolicy>)
Adding axiom: SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWelfare agenda:eu#LeftPolicy)>) <agenda:eu#Raise
Welfare> <agenda:eu#LeftPolicy>)
Current voter: 1
Voter 1 gives up!
Current voter: 2
Voter 2 gives up!
-- End of procedure: all voters have given up.

--- Turn-based mechanism finished in 0 seconds

--- RESULT ONTOLOGY

SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <agen
da:eu#LeftPolicy> ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>))
SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWelfare agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWelfare> <agen
da:eu#LeftPolicy>)
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) Objec
tUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#LeftPolicy>)
SubClassOf(Annotation(<origin> <SubClassOf(agenda:eu#RaiseWages agenda:eu#LeftPolicy)>) <agenda:eu#RaiseWages> <agenda:e
u#LeftPolicy>)
SubClassOf(Annotation(<origin> <DisjointUnion(agenda:eu#LeftPolicy agenda:eu#RaiseWages agenda:eu#RaiseWelfare )>) <agen
da:eu#RaiseWages> ObjectComplementOf(<agenda:eu#RaiseWelfare>))
ClassAssertion(<agenda:eu#RaiseWelfare> <agenda:eu#Sweden>)
ClassAssertion(<agenda:eu#LeftPolicy> <agenda:eu#Sweden>)
ClassAssertion(ObjectUnionOf(<agenda:eu#RaiseWages> <agenda:eu#RaiseWelfare>) <agenda:eu#Sweden>)
```


### App example: AppBlendingDialogue

This app showcases the technique of asymmetric dialogical concept hybridisations, presented in Guendalina Righetti, Daniele Porello, Nicolas Troquard, Oliver Kutz, Maria M. Hedblom, Pietro Galliani. Asymmetric Hybrids: Dialogues for Computational Concept Combination. 12th International Conference on Formal Ontology in Information Systems (FOIS 2021). IOS Press.


### App example: AppMakeInconsistent

This app uses axiom strengthening (`AxiomStrengthener`) to obtain an inconsistent ontology from a consistent one. The main intended application is to build inconsistent ontologies from real ones, used for testing repairing methods.

A first argument must be given, corresponding to an OWL ontology file path. A second argument can be given, to indicate the minimal number of strengthening iterations must be done. A third argument can be given, to indicate the minimal number of iterations needed that must be done after reaching inconsistency.

The resulting ontology is saved in a file `<filename>-made-inconsistent.owl`, when the filename of the original ontology is `<filename>.owl`.


## Acknowledgments

Have contributed to this project: Nicolas Troquard (UNIBZ), Roberto Confalonieri (UNIBZ), Pietro Galliani (UNIBZ).

