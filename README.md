# Synopsis #

A suite of utility functions and applications for engineering OWL ontologies.


# Some functions #

## Computing maximally consistent sets

The function `MaximalConsistentSets#maximalConsistentSubsets` takes a set of axioms in parameter and returns the set of maximally consistent subsets.

It is an implementation of the algorithm presented in Robert Malouf's "Maximal Consistent Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.`

## TBox Normalization and rule generation

The function `Normalization#normalizeCondor` returns normalized version of the input ontology, following the procedure of Simancik et al. "Consequence-Based Reasoning beyond Horn Ontologies" (IJCAI 2011). The function `Normalization#normalizeNaive` is a more naive normalization, which normalizes every subclass axiom individually, using exclusively the function `NormalizationTools#normalizeSubClassAxiom`. If the TBox of the original ontology does not have only subclass types of axioms, some preprocessing is necessary using `NormalizationsTools#asSubClassOfAxioms`. Not every TBox can be converted.

Both normalization functions return an ontology who TBox contains only subclass axioms in normal form: A TBox axiom in normal form can be of one of four types:

* Type1: Subclass(atom or conjunction of atoms, atom or disjunction of atoms)

* Type2: Subclass(atom, exists property atom)

* Type3: Subclass(atom, forall property atom)  

* Type4: Subclass(exists property atom, atom)


The function `RuleGeneration#normalizedSubClassAxiomToRule` transforms a subclass axiom in normal form into a rule. 
It takes an axiom of Type1-4 and returns a string representation. E.g.,

* Type1 axiom SubClass(Conjunction(A B), C) becomes (1,a7,a12,|,a13)

* Type1 axiom SubClass(Conjunction(A B), Disjunction(C,D)) becomes (1,a7,a12,|,a13,a4)

* Type3 axiom Subclass(A, forall hasProperty B) becomes (3,a7,r3,a12)




# Test ontologies #

The directory `resources/` contains test OWL ontology.


# App example: show ontology #

The app AppShowOntology allows one to display a "human readable" form of an OWL ontology.

`AppShowOntology resources/bodysystem.owl` prints:

```
Ontology loaded.
Declaration(Class(<http://who.int/bodysystem.owl#AutonomicNervousSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#HaematopoieticSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#SympatheticNervousSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#SkinSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#MetabolicSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#CentralNervousSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#MentalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#VestibularSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#MusculoskeletalSystem>))
Declaration(Class(owl:Thing))
Declaration(Class(<http://who.int/bodysystem.owl#MotoricSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#EndocrineSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#ImmuneSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#ParasympatheticNervousSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#PeripheralNervousSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#CirculatorySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#HaemolymphoidSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#RespiratorySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#NutritionalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#UrinarySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#BodySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#DigestiveSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#AuditorySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#VisualSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#GenitourinarySystem>))
Declaration(Class(<http://who.int/bodysystem.owl#FemaleGenitalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#MaleGenitalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#SceletalSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#MuscularSystem>))
Declaration(Class(<http://who.int/bodysystem.owl#NervousSystem>))
SubClassOf(<http://who.int/bodysystem.owl#AuditorySystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#SkinSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#FemaleGenitalSystem> <http://who.int/bodysystem.owl#GenitourinarySystem>)
SubClassOf(<http://who.int/bodysystem.owl#GenitourinarySystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#CentralNervousSystem> <http://who.int/bodysystem.owl#NervousSystem>)
SubClassOf(<http://who.int/bodysystem.owl#HaematopoieticSystem> <http://who.int/bodysystem.owl#HaemolymphoidSystem>)
SubClassOf(<http://who.int/bodysystem.owl#SceletalSystem> <http://who.int/bodysystem.owl#MusculoskeletalSystem>)
SubClassOf(<http://who.int/bodysystem.owl#ParasympatheticNervousSystem> <http://who.int/bodysystem.owl#AutonomicNervousSystem>)
SubClassOf(<http://who.int/bodysystem.owl#VestibularSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#MusculoskeletalSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#MaleGenitalSystem> <http://who.int/bodysystem.owl#GenitourinarySystem>)
SubClassOf(<http://who.int/bodysystem.owl#NervousSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#PeripheralNervousSystem> <http://who.int/bodysystem.owl#NervousSystem>)
SubClassOf(<http://who.int/bodysystem.owl#MotoricSystem> <http://who.int/bodysystem.owl#NervousSystem>)
SubClassOf(<http://who.int/bodysystem.owl#EndocrineSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#VisualSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#MetabolicSystem> <http://who.int/bodysystem.owl#BodySystem>)
SubClassOf(<http://who.int/bodysystem.owl#SympatheticNervousSystem> <http://who.int/bodysystem.owl#AutonomicNervousSystem>)
SubClassOf(<http://who.int/bodysystem.owl#ImmuneSystem> <http://who.int/bodysystem.owl#HaemolymphoidSystem>)
SubClassOf(<http://who.int/bodysystem.owl#UrinarySystem> <http://who.int/bodysystem.owl#GenitourinarySystem>)
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
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#EndocrineSystem> "Endocrine System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#MusculoskeletalSystem> "Musculoskeletal System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#CirculatorySystem> "Circulatory System (Cardiovascular System)"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#DigestiveSystem> "Digestive System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#FemaleGenitalSystem> "Female Genital System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#VisualSystem> "Visual System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#NutritionalSystem> "Nutritional System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#VestibularSystem> "Vestibular System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#CentralNervousSystem> "Central Nervous System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#MetabolicSystem> "Metabolic System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#SkinSystem> "Skin System (Integumentary System)"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#RespiratorySystem> "Respiratory System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#ImmuneSystem> "Immune System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#HaemolymphoidSystem> "Haemolymphoid System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#GenitourinarySystem> "Genitourinary System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#SympatheticNervousSystem> "Sympathetic Nervous System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#UrinarySystem> "Urinary System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#AutonomicNervousSystem> "Autonomic Nervous System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#NervousSystem> "Nervous System"^^xsd:string)
AnnotationAssertion(rdfs:label <http://who.int/bodysystem.owl#MentalSystem> "Mental System"^^xsd:string)
```


# App example: TBox normalizations #


We provide the tools to convert the TBox an ontology into a normalized form. An axiom is in normalized form when it has one of the following forms. This might change in the future.

* Type1: Subclass(atom or conjunction of atoms, atom or disjunction of atoms)

* Type2: Subclass(atom, exists property atom)

* Type3: Subclass(atom, forall property atom)  

* Type4: Subclass(exists property atom, atom)


If the TBox of the original ontology does not have only subclass types of axioms, some preprocessing is necessary using `NormalizationsTools.asSubClassOfAxioms`. Not every TBox can be converted.


`Normalization.java` contains two normalizing functions:

* `normalizeCondor` which follows the procedure of Simancik et al. "Consequence-Based Reasoning beyond Horn Ontologies" (IJCAI 2011).

* `normalizeNaive` which normalizes every subclass axiom individually, using exclusively the function `NormalizationTools.normalizeSubClassAxiom`.
 


## Rule generation


`RuleGeneration.java` contains `normalizedSubClassAxiomToRule` which takes an axiom of Type1-4 and returns a string representation. E.g.,

* Type1 axiom SubClass(Conjunction(A B), C) becomes (1,a7,a12,|,a13)

* Type1 axiom SubClass(Conjunction(A B), Disjunction(C,D)) becomes (1,a7,a12,|,a13,a4)

* Type3 axiom Subclass(A, forall hasProperty B) becomes (3,a7,r3,a12)


## Example

Running `AppNormalization` with `resources/catsandnumbers.owl` as argument gives these results:

```
Ontology http://www.semanticweb.org/ontologies/dl2017_example loaded.

Original TBox
EquivalentClasses(PrimeNumber ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness)) )
EquivalentClasses(BlackCat ObjectIntersectionOf(ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black)) ObjectAllValuesFrom(hasColour Black)) )
EquivalentClasses(WhiteCat ObjectIntersectionOf(ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White)) ObjectAllValuesFrom(hasColour White)) )
SubClassOf(White GrayScale)
SubClassOf(Primeness Quality)
SubClassOf(Integer Number)
SubClassOf(Colour Quality)
SubClassOf(Number AbstractObject)
SubClassOf(Animal PhysicalObject)
SubClassOf(Black GrayScale)
SubClassOf(GrayScale Colour)
SubClassOf(Pet Animal)
SubClassOf(Cat Pet)
ObjectPropertyRange(hasColour Colour)
ObjectPropertyRange(hasQuality Quality)
ObjectPropertyDomain(hasQuality owl:Thing)
ObjectPropertyDomain(hasColour PhysicalObject)
DisjointClasses(Black White)
DisjointClasses(AbstractObject PhysicalObject)
DisjointClasses(PhysicalObject Quality)

NAIVE NORMALIZATION

Naive Normalized TBox
-- SubClassOf(GrayScale Colour)
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasQuality Primeness)] ObjectAllValuesFrom(hasQuality Primeness))
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectAllValuesFrom(hasQuality Primeness)] Integer) PrimeNumber)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectAllValuesFrom(hasQuality Primeness)] FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))]))
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))] ObjectSomeValuesFrom(hasQuality FRESH#[ObjectComplementOf(Primeness)]))
-- SubClassOf(ObjectSomeValuesFrom(hasQuality FRESH#[ObjectComplementOf(Primeness)]) FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))])
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(Primeness)] Primeness) owl:Nothing)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(Primeness)] Primeness))
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(Primeness)] Primeness) owl:Nothing)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(Primeness)] Primeness))
-- SubClassOf(ObjectIntersectionOf(AbstractObject PhysicalObject) owl:Nothing)
-- SubClassOf(ObjectIntersectionOf(Black White) owl:Nothing)
-- SubClassOf(PrimeNumber Integer)
-- SubClassOf(PrimeNumber ObjectAllValuesFrom(hasQuality Primeness))
-- SubClassOf(Cat Pet)
-- SubClassOf(BlackCat Cat)
-- SubClassOf(BlackCat ObjectSomeValuesFrom(hasColour Black))
-- SubClassOf(BlackCat ObjectAllValuesFrom(hasColour Black))
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour Black)] ObjectSomeValuesFrom(hasColour Black))
-- SubClassOf(ObjectSomeValuesFrom(hasColour Black) FRESH#[ObjectSomeValuesFrom(hasColour Black)])
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasColour Black)] ObjectAllValuesFrom(hasColour Black))
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectAllValuesFrom(hasColour Black)] FRESH#[ObjectSomeValuesFrom(hasColour Black)] Cat) BlackCat)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectAllValuesFrom(hasColour Black)] FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))]))
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))] ObjectSomeValuesFrom(hasColour FRESH#[ObjectComplementOf(Black)]))
-- SubClassOf(ObjectSomeValuesFrom(hasColour FRESH#[ObjectComplementOf(Black)]) FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))])
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(Black)] Black) owl:Nothing)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(Black)] Black))
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(Black)] Black) owl:Nothing)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(Black)] Black))
-- SubClassOf(Black GrayScale)
-- SubClassOf(Number AbstractObject)
-- SubClassOf(ObjectIntersectionOf(AbstractObject PhysicalObject) owl:Nothing)
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour White)] ObjectSomeValuesFrom(hasColour White))
-- SubClassOf(ObjectSomeValuesFrom(hasColour White) FRESH#[ObjectSomeValuesFrom(hasColour White)])
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasColour White)] ObjectAllValuesFrom(hasColour White))
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectAllValuesFrom(hasColour White)] FRESH#[ObjectSomeValuesFrom(hasColour White)] Cat) WhiteCat)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectAllValuesFrom(hasColour White)] FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))]))
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))] ObjectSomeValuesFrom(hasColour FRESH#[ObjectComplementOf(White)]))
-- SubClassOf(ObjectSomeValuesFrom(hasColour FRESH#[ObjectComplementOf(White)]) FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))])
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(White)] White) owl:Nothing)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(White)] White))
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(White)] White) owl:Nothing)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(White)] White))
-- SubClassOf(ObjectIntersectionOf(Black White) owl:Nothing)
-- SubClassOf(Pet Animal)
-- SubClassOf(Colour Quality)
-- SubClassOf(WhiteCat Cat)
-- SubClassOf(WhiteCat ObjectSomeValuesFrom(hasColour White))
-- SubClassOf(WhiteCat ObjectAllValuesFrom(hasColour White))
-- SubClassOf(Animal PhysicalObject)
-- SubClassOf(ObjectSomeValuesFrom(hasQuality owl:Thing) owl:Thing)
-- SubClassOf(Integer Number)
-- SubClassOf(ObjectIntersectionOf(PhysicalObject Quality) owl:Nothing)
-- SubClassOf(ObjectSomeValuesFrom(hasColour owl:Thing) PhysicalObject)
-- SubClassOf(owl:Thing ObjectAllValuesFrom(hasQuality Quality))
-- SubClassOf(White GrayScale)
-- SubClassOf(ObjectIntersectionOf(PhysicalObject Quality) owl:Nothing)
-- SubClassOf(Primeness Quality)
-- SubClassOf(owl:Thing ObjectAllValuesFrom(hasColour Colour))

To rules
nc(a19).
nc(a13).
nc(a29).
nc(a20).
nc(a4).
nc(a11).
nc(a1).
nc(a22).
nc(r2).
nc(r1).
nc(a2).
nc(a27).
nc(a6).
nc(a7).
nc(a17).
nc(a8).
nc(a5).
nc(a24).
nc(a9).
nc(a15).
nc(a18).
nc(a26).
nc(a12).
nc(a25).
nc(r3).
nc(a21).
nc(a28).
nc(a3).
nc(a23).
nc(a16).
nc(a10).
nc(a14).
a(1, (a18), (a17)).
a(3, a3, r2, a24).
a(1, (a3, a19), (a23)).
a(1, (a29), (a3, a11)).
a(2, a11, r2, a5).
a(4, a5, r2, a11).
a(1, (a5, a24), (a28)).
a(1, (a29), (a5, a24)).
a(1, (a5, a24), (a28)).
a(1, (a29), (a5, a24)).
a(1, (a12, a22), (a28)).
a(1, (a14, a26), (a28)).
a(1, (a23), (a19)).
a(3, a23, r2, a24).
a(1, (a16), (a21)).
a(1, (a15), (a16)).
a(2, a15, r1, a14).
a(3, a15, r1, a14).
a(2, a7, r1, a14).
a(4, a14, r1, a7).
a(3, a1, r1, a14).
a(1, (a1, a7, a16), (a15)).
a(1, (a29), (a1, a9)).
a(2, a9, r1, a4).
a(4, a4, r1, a9).
a(1, (a4, a14), (a28)).
a(1, (a29), (a4, a14)).
a(1, (a4, a14), (a28)).
a(1, (a29), (a4, a14)).
a(1, (a14), (a18)).
a(1, (a20), (a12)).
a(1, (a12, a22), (a28)).
a(2, a8, r1, a26).
a(4, a26, r1, a8).
a(3, a2, r1, a26).
a(1, (a2, a8, a16), (a27)).
a(1, (a29), (a2, a10)).
a(2, a10, r1, a6).
a(4, a6, r1, a10).
a(1, (a6, a26), (a28)).
a(1, (a29), (a6, a26)).
a(1, (a6, a26), (a28)).
a(1, (a29), (a6, a26)).
a(1, (a14, a26), (a28)).
a(1, (a21), (a13)).
a(1, (a17), (a25)).
a(1, (a27), (a16)).
a(2, a27, r1, a26).
a(3, a27, r1, a26).
a(1, (a13), (a22)).
a(4, a29, r2, a29).
a(1, (a19), (a20)).
a(1, (a22, a25), (a28)).
a(4, a29, r1, a22).
a(3, a29, r2, a25).
a(1, (a26), (a18)).
a(1, (a22, a25), (a28)).
a(1, (a24), (a25)).
a(3, a29, r1, a17).

where
a19		Integer
a13		Animal
a29		owl:Thing
a20		Number
a4		FRESH#[ObjectComplementOf(Black)]
a11		FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))]
a1		FRESH#[ObjectAllValuesFrom(hasColour Black)]
a22		PhysicalObject
r2		hasQuality
r1		hasColour
a2		FRESH#[ObjectAllValuesFrom(hasColour White)]
a27		WhiteCat
a6		FRESH#[ObjectComplementOf(White)]
a7		FRESH#[ObjectSomeValuesFrom(hasColour Black)]
a17		Colour
a8		FRESH#[ObjectSomeValuesFrom(hasColour White)]
a5		FRESH#[ObjectComplementOf(Primeness)]
a24		Primeness
a9		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))]
a15		BlackCat
a18		GrayScale
a26		White
a12		AbstractObject
a25		Quality
r3		owl:topObjectProperty
a21		Pet
a28		owl:Nothing
a3		FRESH#[ObjectAllValuesFrom(hasQuality Primeness)]
a23		PrimeNumber
a16		Cat
a10		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))]
a14		Black

CONDOR NORMALIZATION

Condor Normalized TBox
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))) ObjectSomeValuesFrom(hasColour White))] WhiteCat)
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black) ObjectAllValuesFrom(hasColour Black))] FRESH#[ObjectAllValuesFrom(hasColour Black)])
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black) ObjectAllValuesFrom(hasColour Black))] Cat)
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White)))] FRESH#[ObjectSomeValuesFrom(hasColour White)] Cat) FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))) ObjectSomeValuesFrom(hasColour White))])
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness)))] FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))]))
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasColour Colour)] ObjectAllValuesFrom(hasColour Colour))
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasColour White)] ObjectAllValuesFrom(hasColour White))
-- SubClassOf(Cat Pet)
-- SubClassOf(Pet Animal)
-- SubClassOf(owl:Thing FRESH#[ObjectAllValuesFrom(hasColour Colour)])
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))) ObjectSomeValuesFrom(hasColour Black))] BlackCat)
-- SubClassOf(PhysicalObject FRESH#[ObjectComplementOf(Quality)])
-- SubClassOf(Primeness Quality)
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black) ObjectAllValuesFrom(hasColour Black))] FRESH#[ObjectSomeValuesFrom(hasColour Black)])
-- SubClassOf(BlackCat FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black) ObjectAllValuesFrom(hasColour Black))])
-- SubClassOf(WhiteCat FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White) ObjectAllValuesFrom(hasColour White))])
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black)))] FRESH#[ObjectSomeValuesFrom(hasColour Black)] Cat) FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))) ObjectSomeValuesFrom(hasColour Black))])
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White)))] FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))]))
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(AbstractObject)] AbstractObject) owl:Nothing)
-- SubClassOf(ObjectSomeValuesFrom(hasColour owl:Thing) FRESH#[ObjectSomeValuesFrom(hasColour owl:Thing)])
-- SubClassOf(ObjectSomeValuesFrom(hasColour Black) FRESH#[ObjectSomeValuesFrom(hasColour Black)])
-- SubClassOf(FRESH#[ObjectIntersectionOf(Integer ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))))] PrimeNumber)
-- SubClassOf(AbstractObject FRESH#[ObjectComplementOf(PhysicalObject)])
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(PhysicalObject)] PhysicalObject) owl:Nothing)
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(White)] White) owl:Nothing)
-- SubClassOf(Black FRESH#[ObjectComplementOf(White)])
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(Black)] Black) owl:Nothing)
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(Primeness)] Primeness) owl:Nothing)
-- SubClassOf(Integer Number)
-- SubClassOf(ObjectSomeValuesFrom(hasColour White) FRESH#[ObjectSomeValuesFrom(hasColour White)])
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour White)] ObjectSomeValuesFrom(hasColour White))
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour Black)] ObjectSomeValuesFrom(hasColour Black))
-- SubClassOf(owl:Thing FRESH#[ObjectAllValuesFrom(hasQuality Quality)])
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasQuality Quality)] ObjectAllValuesFrom(hasQuality Quality))
-- SubClassOf(Number AbstractObject)
-- SubClassOf(Animal PhysicalObject)
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(Quality)] Quality) owl:Nothing)
-- SubClassOf(White GrayScale)
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White) ObjectAllValuesFrom(hasColour White))] Cat)
-- SubClassOf(PrimeNumber FRESH#[ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness))])
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasQuality Primeness)] ObjectAllValuesFrom(hasQuality Primeness))
-- SubClassOf(FRESH#[ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness))] Integer)
-- SubClassOf(Colour Quality)
-- SubClassOf(ObjectIntersectionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness)))] Integer) FRESH#[ObjectIntersectionOf(Integer ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))))])
-- SubClassOf(White FRESH#[ObjectComplementOf(Black)])
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour owl:Thing)] PhysicalObject)
-- SubClassOf(owl:Thing ObjectUnionOf(FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black)))] FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))]))
-- SubClassOf(FRESH#[ObjectAllValuesFrom(hasColour Black)] ObjectAllValuesFrom(hasColour Black))
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))] ObjectSomeValuesFrom(hasQuality FRESH#[ObjectComplementOf(Primeness)]))
-- SubClassOf(Black GrayScale)
-- SubClassOf(Quality FRESH#[ObjectComplementOf(PhysicalObject)])
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))] ObjectSomeValuesFrom(hasColour FRESH#[ObjectComplementOf(Black)]))
-- SubClassOf(FRESH#[ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness))] FRESH#[ObjectAllValuesFrom(hasQuality Primeness)])
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White) ObjectAllValuesFrom(hasColour White))] FRESH#[ObjectAllValuesFrom(hasColour White)])
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))] ObjectSomeValuesFrom(hasColour FRESH#[ObjectComplementOf(White)]))
-- SubClassOf(GrayScale Colour)
-- SubClassOf(FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White) ObjectAllValuesFrom(hasColour White))] FRESH#[ObjectSomeValuesFrom(hasColour White)])
-- SubClassOf(ObjectSomeValuesFrom(hasQuality owl:Thing) FRESH#[ObjectSomeValuesFrom(hasQuality owl:Thing)])
-- SubClassOf(FRESH#[ObjectSomeValuesFrom(hasQuality owl:Thing)] owl:Thing)
-- SubClassOf(PhysicalObject FRESH#[ObjectComplementOf(AbstractObject)])

To rules
nc(a35).
nc(a29).
nc(a45).
nc(a10).
nc(a17).
nc(a36).
nc(a5).
nc(a16).
nc(a7).
nc(a26).
nc(a1).
nc(a38).
nc(r2).
nc(a27).
nc(r1).
nc(a13).
nc(a3).
nc(a8).
nc(a43).
nc(a11).
nc(a21).
nc(a33).
nc(a22).
nc(a20).
nc(a9).
nc(a19).
nc(a40).
nc(a23).
nc(a31).
nc(a34).
nc(a42).
nc(a6).
nc(a28).
nc(a41).
nc(a12).
nc(a15).
nc(a2).
nc(r3).
nc(a37).
nc(a25).
nc(a18).
nc(a44).
nc(a4).
nc(a39).
nc(a14).
nc(a32).
nc(a24).
nc(a30).
a(1, (a16), (a43)).
a(1, (a17), (a1)).
a(1, (a17), (a32)).
a(1, (a13, a22, a32), (a16)).
a(1, (a45), (a14, a26)).
a(3, a2, r1, a33).
a(3, a3, r1, a42).
a(1, (a32), (a37)).
a(1, (a37), (a29)).
a(1, (a45), (a2)).
a(1, (a15), (a31)).
a(1, (a38), (a10)).
a(1, (a40), (a41)).
a(1, (a17), (a21)).
a(1, (a31), (a17)).
a(1, (a43), (a18)).
a(1, (a12, a21, a32), (a15)).
a(1, (a45), (a13, a24)).
a(1, (a6, a28), (a44)).
a(4, a45, r1, a25).
a(4, a30, r1, a21).
a(1, (a20), (a39)).
a(1, (a28), (a8)).
a(1, (a8, a38), (a44)).
a(1, (a11, a42), (a44)).
a(1, (a30), (a11)).
a(1, (a7, a30), (a44)).
a(1, (a9, a40), (a44)).
a(1, (a35), (a36)).
a(4, a42, r1, a22).
a(2, a22, r1, a42).
a(2, a21, r1, a30).
a(1, (a45), (a5)).
a(3, a5, r2, a41).
a(1, (a36), (a28)).
a(1, (a29), (a38)).
a(1, (a10, a41), (a44)).
a(1, (a42), (a34)).
a(1, (a18), (a32)).
a(1, (a39), (a19)).
a(3, a4, r2, a40).
a(1, (a19), (a35)).
a(1, (a33), (a41)).
a(1, (a14, a35), (a20)).
a(1, (a42), (a7)).
a(1, (a25), (a38)).
a(1, (a45), (a12, a23)).
a(3, a1, r1, a30).
a(2, a26, r2, a9).
a(1, (a30), (a34)).
a(1, (a41), (a8)).
a(2, a23, r1, a7).
a(1, (a19), (a4)).
a(1, (a18), (a3)).
a(2, a24, r1, a11).
a(1, (a34), (a33)).
a(1, (a18), (a22)).
a(4, a45, r2, a27).
a(1, (a27), (a45)).
a(1, (a38), (a6)).

where
a35		Integer
a29		Animal
a45		owl:Thing
a10		FRESH#[ObjectComplementOf(Quality)]
a17		FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black) ObjectAllValuesFrom(hasColour Black))]
a36		Number
a5		FRESH#[ObjectAllValuesFrom(hasQuality Quality)]
a16		FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))) ObjectSomeValuesFrom(hasColour White))]
a7		FRESH#[ObjectComplementOf(Black)]
a26		FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))]
a1		FRESH#[ObjectAllValuesFrom(hasColour Black)]
a38		PhysicalObject
r2		hasQuality
a27		FRESH#[ObjectSomeValuesFrom(hasQuality owl:Thing)]
r1		hasColour
a13		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White)))]
a3		FRESH#[ObjectAllValuesFrom(hasColour White)]
a8		FRESH#[ObjectComplementOf(PhysicalObject)]
a43		WhiteCat
a11		FRESH#[ObjectComplementOf(White)]
a21		FRESH#[ObjectSomeValuesFrom(hasColour Black)]
a33		Colour
a22		FRESH#[ObjectSomeValuesFrom(hasColour White)]
a20		FRESH#[ObjectIntersectionOf(Integer ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))))]
a9		FRESH#[ObjectComplementOf(Primeness)]
a19		FRESH#[ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness))]
a40		Primeness
a23		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))]
a31		BlackCat
a34		GrayScale
a42		White
a6		FRESH#[ObjectComplementOf(AbstractObject)]
a28		AbstractObject
a41		Quality
a12		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black)))]
a15		FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))) ObjectSomeValuesFrom(hasColour Black))]
a2		FRESH#[ObjectAllValuesFrom(hasColour Colour)]
r3		owl:topObjectProperty
a37		Pet
a25		FRESH#[ObjectSomeValuesFrom(hasColour owl:Thing)]
a18		FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White) ObjectAllValuesFrom(hasColour White))]
a44		owl:Nothing
a4		FRESH#[ObjectAllValuesFrom(hasQuality Primeness)]
a39		PrimeNumber
a14		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness)))]
a32		Cat
a24		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))]
a30		Black

Finished.
```


## Setup

Import as a Maven project.