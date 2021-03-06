package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


public class DatatypeInferenceTest {

    private static final String JDBC_URL = "jdbc:h2:mem:mapping-datatype-inference";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/datatype-inference/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";
    private static TestConnectionManager TEST_MANAGER;

    @BeforeClass
    public static void setUp() throws Exception {
        TEST_MANAGER = new TestConnectionManager(JDBC_URL, DB_USER, DB_PASSWORD, CREATE_SCRIPT, DROP_SCRIPT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TEST_MANAGER.close();
    }

    @Test(expected = MappingOntologyMismatchException.class)
    public void testMappingOntologyConflict() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_invalid_datatype.obda");
    }

    @Test
    public void testRangeInferredDatatype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_range_datatype.obda");
        checkDatatype(spec.getSaturatedMapping(), Predicate.COL_TYPE.STRING);
    }

    @Test
    public void testNoRangeMappingDatatype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_no_range_prop_mapping_datatype.obda");
        checkDatatype(spec.getSaturatedMapping(), Predicate.COL_TYPE.INTEGER);
    }

    @Test
    public void testNoRangeColtype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_no_range_prop_coltype.obda");
        checkDatatype(spec.getSaturatedMapping(), Predicate.COL_TYPE.STRING);
    }

    @Test
    public void testUnknownMappingDatatype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_unknown_prop_mapping_datatype.obda");
        checkDatatype(spec.getSaturatedMapping(), Predicate.COL_TYPE.INTEGER);
    }

    @Test
    public void testUnknownStringColtype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_unknown_prop_coltype.obda");
        checkDatatype(spec.getSaturatedMapping(), Predicate.COL_TYPE.STRING);
    }

    @Test
    public void testUnknownIntegerColtype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_unknown_prop_coltype_int.obda");
        checkDatatype(spec.getSaturatedMapping(), Predicate.COL_TYPE.INTEGER);
    }

    private void checkDatatype(Mapping mapping, Predicate.COL_TYPE expectedColType) {
        Optional<Predicate> optionalDatatype = mapping.getPredicates().stream()
                .map(mapping::getDefinition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(query -> query.getRootConstructionNode().getSubstitution().getImmutableMap().values().stream())
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .map(Function::getFunctionSymbol)
                .filter(p -> p instanceof DatatypePredicate)
                .findFirst();

        assertTrue("A datatype was expected", optionalDatatype.isPresent());
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Predicate datatype = optionalDatatype.get();

        assertEquals(TYPE_FACTORY.getTypePredicate(expectedColType), datatype);
    }
}
