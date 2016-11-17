package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeftJoinProfTest {

    private static final String CREATE_SCRIPT = "src/test/resources/test/redundant_join/redundant_join_fk_create.sql";
    private static final String DROP_SCRIPT = "src/test/resources/test/redundant_join/redundant_join_fk_drop.sql";
    private static final String OWL_FILE = "src/test/resources/test/redundant_join/redundant_join_fk_test.owl";
    private static final String ODBA_FILE = "src/test/resources/test/redundant_join/redundant_join_fk_test.obda";
    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";


    private OBDADataFactory fac;
    private Connection conn;

    private OBDAModel obdaModel;
    private OWLOntology ontology;


    @Before
    public void setUp() throws Exception {

        String url = "jdbc:h2:mem:professor";
        String username = "sa";
        String password = "sa";

        fac = OBDADataFactoryImpl.getInstance();
        conn = DriverManager.getConnection(url, username, password);
        Statement st = conn.createStatement();

        FileReader reader = new FileReader(CREATE_SCRIPT);

        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        conn.commit();

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(OWL_FILE)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(ODBA_FILE);
    }

    @After
    public void tearDown() throws Exception {
        dropTables();
        conn.close();
    }

    private void dropTables() throws SQLException, IOException {

        Statement st = conn.createStatement();

        FileReader reader = new FileReader(DROP_SCRIPT);
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        st.close();
        conn.commit();
    }

    @Test
    public void testSimpleFirstName() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v\n" +
                "  }\n" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara", "Mary"
        );
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Ignore("Not supported yet")
    @Test
    public void testFullName1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v ;\n" +
                "          :lastName ?lastName .\n" +
                "  }\n" +
                "}";

        List<String> expectedValues = ImmutableList.of(
        "Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara", "Mary"
        );
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Ignore("TODO: add the fix point to enable it")
    @Test
    public void testFullName2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v .\n" +
                "   }\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?lastName .\n" +
                "   }\n" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara", "Mary"
        );
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Test
    public void testFirstNameNickname() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v ;\n" +
                "          :nickname ?nickname .\n" +
                "  }\n" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "Roger", "Frank", "John", "Michael"
        );
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertTrue(containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Ignore("TODO: lift the conditions up and  then try to support it")
    @Test
    public void testSimpleNickname() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?v\n" +
                "  }\n" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "Rog", "Frankie", "Johnny", "King of Pop"
        );
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Test
    public void testCourseTeacherName() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v\n" +
                "  }\n" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "Smith", "Poppins", "Depp"
        );
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    private static boolean containsMoreThanOneOccurrence(String query, String pattern) {
        int firstOccurrenceIndex = query.indexOf(pattern);
        if (firstOccurrenceIndex >= 0) {
            return query.substring(firstOccurrenceIndex + 1).contains(pattern);
        }
        return false;
    }

    private String checkReturnedValuesAndReturnSql(String query, List<String> expectedValues) throws Exception {

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        String sql;

        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            sql = st.getUnfolding(query);
            QuestOWLResultSet rs = st.executeTuple(query);
            while (rs.nextRow()) {
                OWLLiteral ind1 = rs.getOWLLiteral("v");
                // log.debug(ind1.toString());
                if (ind1 != null) {
                    returnedValues.add(ind1.getLiteral());
                    System.out.println(ind1.getLiteral());
                    i++;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

        return sql;
    }
}