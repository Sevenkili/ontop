package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-quest-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;
import org.junit.Before;
import org.junit.Test;

/* 
 * Test class for infinite loop in the ontology "final_project_original.owl"
 * after the method eliominateCycles is called there is still a cycle in the graph with the nodes
 * [Ehttp://www.semanticweb.org/orchidlioness/ontologies/2014/4/final_project#writes^-, http://www.semanticweb.org/orchidlioness/ontologies/2014/4/final_project#Work]
 * it generates a loop in eliminateRedundantEdge
 */

public class DAGLoopTest {

	Ontology onto;
	@Before
	public void setUp() throws Exception {
		onto = OWLAPITranslatorUtility.loadOntologyFromFile("src/test/resources/test/dag/final_project_original.owl");
	}

	

	@Test
	public void testLoop() throws Exception {
		// generate DAG
		TBoxReasoner dag = TBoxReasonerImpl.create(onto);
	}
}
