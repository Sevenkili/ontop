package it.unibz.inf.ontop.reformulation.tests;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NPDUndolferTest extends TestCase {

    private final String owlfile = "src/test/resources/npd-v2-ql_a.owl";
    private final String obdafile = "src/test/resources/npd-v2-ql_a.obda";


    /**
     * Query 6 from the NPD benchmark
     * (this query was an indicator for incorrect variable order in SPAQRL Extend)
     *
     * @throws Exception
     */

    @Test
    public void testNpdQ6() throws Exception {

        String query =
                "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                        "PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
                        "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT DISTINCT ?wellbore (?length * 0.30 AS ?lenghtM) (?length AS ?lenghtS) ?company (?year + 2 AS ?YearB) " +
                        "WHERE {" +
                        "  ?wc npdv:coreForWellbore" +
                        "        [ rdf:type                      npdv:Wellbore ;" +
                        "          npdv:name                     ?wellbore ;" +
                        "          npdv:wellboreCompletionYear   ?year ;" +
                        "          npdv:drillingOperatorCompany  [ npdv:name ?company ] " +
                        "        ] ." +
                        "  { ?wc npdv:coresTotalLength ?length } " +
                        "  " +
                        "  FILTER(?year >= \"2008\"^^xsd:integer && ?length > 50 " +
                        "  )" +
                        "} ORDER BY ?wellbore";

        String rewriting = getRewriting(query);
        assertFalse(rewriting.contains("GTE(company,"));
        assertTrue(rewriting.contains("GTE(year"));
        assertTrue(rewriting.contains("GT(lenghtS"));
    }


    /**
     * Davide's query
     * (indicator for incomplete treatment of data properties in the rewriting)
     *
     * @throws Exception
     */

    @Test
    public void testNpdQD() throws Exception {

        String query =
                "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                        "PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
                        "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT DISTINCT ?wc " +
                        "		   WHERE { " +
                        "		      ?wc npdv:coreForWellbore [ rdf:type npdv:Wellbore ]. " +
                        "		   }";

        String rewriting = getRewriting(query);
    }

    @Test
    public void testNpdPROJ() throws Exception {

        String query =
                "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                        "PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
                        "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT DISTINCT ?name ?awc " +
                        "		   WHERE { " +
                        "		      ?awc npdv:coreForWellbore [ rdf:type npdv:Wellbore; " +
                        "                                        npdv:wellboreCompletionYear   ?year ;" +
                        "                                        npdv:name  ?name ]. " +
                        "		   }";

        String rewriting = getRewriting(query);
    }

    @Test
    public void testDD() throws Exception {

        String query =
                "PREFIX t: <http://www.w3.org/2001/sw/DataAccess/tests/data/TypePromotion/tP-0#> " +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "ASK " +
                        " WHERE { t:double1 rdf:value ?l . " +
                        "         t:double1 rdf:value ?r . " +
                        "         FILTER ( datatype(?l + ?r) = xsd:double ) }";

        String rewriting = getRewriting(query);
    }


    @Test
    public void testNpdQ0() throws Exception {

        String query =
                "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                        "PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
                        "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT DISTINCT ?wc " +
                        "		   WHERE { " +
                        "		      ?wc npdv:coreForWellbore [ rdf:type <http://sws.ifi.uio.no/vocab/npd-v2#A%20A>; " +
                        "                                        rdf:type npdv:B%20B; " +
                        "                                       npdv:name  \"\\\\\" ]. " +
                        "		   }";

        String rewriting = getRewriting(query);
    }

    //@Test
    public void notestNpdQ09() throws Exception {
        String q09 = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                "SELECT *" +
                "WHERE {" +
                "[ ] a npdv:Facility ;" +
                "npdv:name ?facility ;" +
                "npdv:registeredInCountry ?country;" +
                "npdv:idNPD ?id ." +
                "FILTER (?id > \"400000\"^^xsd:integer)" +
                "}" +
                "ORDER BY ?facility";

        String unf = getNPDUnfolding(q09);
        String unf_rew = getNPDUnfoldingThroughRewriting(q09);

        assertEquals(countUnions(unf), countUnions(unf_rew));

    }

    //@Test
    public void notestNpdQ10() throws Exception {
        String q10 = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
                "SELECT DISTINCT * " +
                "WHERE {" +
                "[] a npdv:DiscoveryWellbore ;" +
                "npdv:name ?wellbore;" +
                "npdv:dateUpdated ?date ." +
                "FILTER (?date > \"2013-01-01T00:00:00.0\"^^xsd:dateTime)" +
                "}" +
                "ORDER BY ?wellbore";

        String unf = getNPDUnfolding(q10);
        String unf_rew = getNPDUnfoldingThroughRewriting(q10);

        assertEquals(countUnions(unf), countUnions(unf_rew));

    }

    private int countUnions(String query) {
        int cnt = 1;
        Pattern p = Pattern.compile("UNION");
        Matcher m = p.matcher(query);

        while (m.find()) ++cnt;

        return cnt;
    }

    /**
     * constructs the unfolding passing by rewriting
     *
     * @param query
     * @return
     * @throws Exception
     */

    private String getNPDUnfoldingThroughRewriting(String query) throws Exception {

		Properties pref = new Properties();
		pref.put(QuestCorePreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		pref.put(QuestCorePreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.put(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		pref.put(QuestCorePreferences.REWRITE, QuestConstants.TRUE);

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .properties(pref)
                .build();
        QuestOWL reasoner = factory.createReasoner(config);

		QuestOWLConnection qconn =  reasoner.getConnection();
		QuestOWLStatement st = qconn.createStatement();

		String unfolding = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
		st.close();
		
		reasoner.dispose();
		
		return unfolding;
	}
	
	/**
	 * constructs directly the unfolding
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	
	private String getNPDUnfolding(String query) throws Exception {

        Properties pref = new Properties();
        pref.put(QuestCorePreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
        pref.put(QuestCorePreferences.ABOX_MODE, QuestConstants.CLASSIC);
        //pref.put(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
        //pref.put(QuestPreferences.REWRITE, QuestConstants.TRUE);

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .properties(pref)
                .build();
        QuestOWL reasoner = factory.createReasoner(config);

        QuestOWLConnection qconn =  reasoner.getConnection();
        QuestOWLStatement st = qconn.createStatement();

        String unfolding = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();
        st.close();
		
		reasoner.dispose();
		
		return unfolding;
	}
	
	/**
	 * constructs a rewriting
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	
	private String getRewriting(String query) throws Exception {
		
		QuestOWLFactory fac = new QuestOWLFactory();

        Properties pref = new Properties();
        pref.put(QuestCorePreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
        pref.put(QuestCorePreferences.ABOX_MODE, QuestConstants.CLASSIC);
        pref.put(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
        pref.put(QuestCorePreferences.REWRITE, QuestConstants.TRUE);

        QuestConfiguration config = QuestConfiguration.defaultBuilder()
                .ontologyFile(owlfile)
                .properties(pref).build();

        QuestOWL quest = fac.createReasoner(config);
        QuestOWLConnection qconn = quest.getConnection();
        QuestOWLStatement st = qconn.createStatement();

        String rewriting = st.getRewriting(query);
        st.close();

        quest.dispose();

        return rewriting;
    }
}
