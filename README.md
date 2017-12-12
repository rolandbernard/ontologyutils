# Synopsis #

A suite of utility functions and applications for engineering OWL ontologies.


# Some functions #

## Computing maximally consistent sets

The function `MaximalConsistentSets#maximalConsistentSubsets` takes a set of axioms in parameter and returns the set of maximally consistent subsets.

It is an implementation of the algorithm presented in Robert Malouf's "Maximal Consistent Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.


## TBox Normalization and rule generation

The function `Normalization#normalizeCondor` returns normalized version of the input ontology, following the procedure of Simancik et al. "Consequence-Based Reasoning beyond Horn Ontologies" (IJCAI 2011). The function `Normalization#normalizeNaive" is a more naive normalization, which normalizes every subclass axiom individually, using exclusively the function `NormalizationTools#normalizeSubClassAxiom`. If the TBox of the original ontology does not have only subclass types of axioms, some preprocessing is necessary using `NormalizationsTools#asSubClassOfAxioms`. Not every TBox can be converted.

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

Running `AppNormalization` with `resources/catsandnumbers.col` as argument gives these results:

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
(1, a1,|, a14).
(3, a7, r2, a24).
(1, a7, a13,|, a23).
(1, a16,|, a7, a19).
(2, a19, r2, a22).
(4, a22, r2, a19).
(1, a22, a24,|, a12).
(1, a16,|, a22, a24).
(1, a22, a24,|, a12).
(1, a16,|, a22, a24).
(1, a5, a27,|, a12).
(1, a29, a18,|, a12).
(1, a23,|, a13).
(3, a23, r2, a24).
(1, a10,|, a8).
(1, a15,|, a10).
(2, a15, r1, a29).
(3, a15, r1, a29).
(2, a4, r1, a29).
(4, a29, r1, a4).
(3, a21, r1, a29).
(1, a21, a4, a10,|, a15).
(1, a16,|, a21, a9).
(2, a9, r1, a25).
(4, a25, r1, a9).
(1, a25, a29,|, a12).
(1, a16,|, a25, a29).
(1, a25, a29,|, a12).
(1, a16,|, a25, a29).
(1, a29,|, a1).
(1, a2,|, a5).
(1, a5, a27,|, a12).
(2, a6, r1, a18).
(4, a18, r1, a6).
(3, a3, r1, a18).
(1, a3, a6, a10,|, a20).
(1, a16,|, a3, a26).
(2, a26, r1, a17).
(4, a17, r1, a26).
(1, a17, a18,|, a12).
(1, a16,|, a17, a18).
(1, a17, a18,|, a12).
(1, a16,|, a17, a18).
(1, a29, a18,|, a12).
(1, a8,|, a28).
(1, a14,|, a11).
(1, a20,|, a10).
(2, a20, r1, a18).
(3, a20, r1, a18).
(1, a28,|, a27).
(4, a16, r2, a16).
(1, a13,|, a2).
(1, a27, a11,|, a12).
(4, a16, r1, a27).
(3, a16, r2, a11).
(1, a18,|, a1).
(1, a27, a11,|, a12).
(1, a24,|, a11).
(3, a16, r1, a14).

where
a13		Integer
a16		owl:Thing
a28		Animal
a2		Number
a19		FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))]
a25		FRESH#[ObjectComplementOf(Black)]
a21		FRESH#[ObjectAllValuesFrom(hasColour Black)]
a27		PhysicalObject
r2		hasQuality
r1		hasColour
a3		FRESH#[ObjectAllValuesFrom(hasColour White)]
a20		WhiteCat
a4		FRESH#[ObjectSomeValuesFrom(hasColour Black)]
a17		FRESH#[ObjectComplementOf(White)]
a14		Colour
a6		FRESH#[ObjectSomeValuesFrom(hasColour White)]
a22		FRESH#[ObjectComplementOf(Primeness)]
a24		Primeness
a9		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))]
a15		BlackCat
a1		GrayScale
a18		White
a5		AbstractObject
a11		Quality
r3		owl:topObjectProperty
a8		Pet
a12		owl:Nothing
a7		FRESH#[ObjectAllValuesFrom(hasQuality Primeness)]
a23		PrimeNumber
a10		Cat
a26		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))]
a29		Black

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
(1, a13,|, a28).
(1, a20,|, a44).
(1, a20,|, a25).
(1, a42, a36, a25,|, a13).
(1, a12,|, a19, a6).
(3, a17, r3, a31).
(3, a24, r3, a14).
(1, a25,|, a11).
(1, a11,|, a39).
(1, a12,|, a17).
(1, a35,|, a16).
(1, a8,|, a10).
(1, a37,|, a34).
(1, a20,|, a23).
(1, a16,|, a20).
(1, a28,|, a45).
(1, a32, a23, a25,|, a35).
(1, a12,|, a42, a18).
(1, a41, a1,|, a3).
(4, a12, r3, a33).
(4, a2, r3, a23).
(1, a15,|, a4).
(1, a1,|, a29).
(1, a29, a8,|, a3).
(1, a27, a14,|, a3).
(1, a2,|, a27).
(1, a21, a2,|, a3).
(1, a7, a37,|, a3).
(1, a22,|, a26).
(4, a14, r3, a36).
(2, a36, r3, a14).
(2, a23, r3, a2).
(1, a12,|, a38).
(3, a38, r1, a34).
(1, a26,|, a1).
(1, a39,|, a8).
(1, a10, a34,|, a3).
(1, a14,|, a40).
(1, a45,|, a25).
(1, a4,|, a30).
(3, a9, r1, a37).
(1, a30,|, a22).
(1, a31,|, a34).
(1, a19, a22,|, a15).
(1, a14,|, a21).
(1, a33,|, a8).
(1, a12,|, a32, a43).
(3, a44, r3, a2).
(2, a6, r1, a7).
(1, a2,|, a40).
(1, a34,|, a29).
(2, a43, r3, a21).
(1, a30,|, a9).
(1, a45,|, a24).
(2, a18, r3, a27).
(1, a40,|, a31).
(1, a45,|, a36).
(4, a12, r1, a5).
(1, a5,|, a12).
(1, a8,|, a41).

where
a22		Integer
a12		owl:Thing
a39		Animal
a10		FRESH#[ObjectComplementOf(Quality)]
a20		FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour Black) ObjectAllValuesFrom(hasColour Black))]
a26		Number
a13		FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))) ObjectSomeValuesFrom(hasColour White))]
a38		FRESH#[ObjectAllValuesFrom(hasQuality Quality)]
a6		FRESH#[ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))]
a21		FRESH#[ObjectComplementOf(Black)]
a44		FRESH#[ObjectAllValuesFrom(hasColour Black)]
a8		PhysicalObject
r1		hasQuality
a5		FRESH#[ObjectSomeValuesFrom(hasQuality owl:Thing)]
r3		hasColour
a42		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(White)))]
a24		FRESH#[ObjectAllValuesFrom(hasColour White)]
a28		WhiteCat
a29		FRESH#[ObjectComplementOf(PhysicalObject)]
a23		FRESH#[ObjectSomeValuesFrom(hasColour Black)]
a27		FRESH#[ObjectComplementOf(White)]
a31		Colour
a36		FRESH#[ObjectSomeValuesFrom(hasColour White)]
a15		FRESH#[ObjectIntersectionOf(Integer ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness))))]
a7		FRESH#[ObjectComplementOf(Primeness)]
a30		FRESH#[ObjectIntersectionOf(Integer ObjectAllValuesFrom(hasQuality Primeness))]
a37		Primeness
a16		BlackCat
a43		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))]
a40		GrayScale
a14		White
a41		FRESH#[ObjectComplementOf(AbstractObject)]
a1		AbstractObject
a34		Quality
a32		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black)))]
a35		FRESH#[ObjectIntersectionOf(Cat ObjectComplementOf(ObjectSomeValuesFrom(hasColour ObjectComplementOf(Black))) ObjectSomeValuesFrom(hasColour Black))]
a17		FRESH#[ObjectAllValuesFrom(hasColour Colour)]
r2		owl:topObjectProperty
a11		Pet
a33		FRESH#[ObjectSomeValuesFrom(hasColour owl:Thing)]
a45		FRESH#[ObjectIntersectionOf(Cat ObjectSomeValuesFrom(hasColour White) ObjectAllValuesFrom(hasColour White))]
a3		owl:Nothing
a9		FRESH#[ObjectAllValuesFrom(hasQuality Primeness)]
a4		PrimeNumber
a19		FRESH#[ObjectComplementOf(ObjectSomeValuesFrom(hasQuality ObjectComplementOf(Primeness)))]
a25		Cat
a18		FRESH#[ObjectSomeValuesFrom(hasColour ObjectComplementOf(White))]
a2		Black

Finished.
```


## Setup

Import as a Maven project.