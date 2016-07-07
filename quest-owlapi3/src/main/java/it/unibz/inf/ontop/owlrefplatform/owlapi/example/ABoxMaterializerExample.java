package it.unibz.inf.ontop.owlrefplatform.owlapi.example;

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

import java.io.*;

import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.impl.OBDACoreModule;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.QuestOWLIndividualAxiomIterator;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;

/**
 * A very simple example that shows how to generate triples in an N-Triple file,
 * using the data of a database and a set of mappings from the OBDA file.
 * 
 * NOTE: This process does not involve reasoning since no ontology is given. The
 * result are the DIRECT triples generated by the mappings.
 */
public class ABoxMaterializerExample {

	/*
	 * Use the sample database using H2 from
	 * https://babbage.inf.unibz.it/trac/obdapublic/wiki/InstallingTutorialDatabases
	 */
	final String inputFile = "src/main/resources/example/exampleBooks.obda";
	final String outputFile = "src/main/resources/example/exampleBooks.txt";
	
	public void generateTriples() throws Exception {

		QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
				.nativeOntopMappingFile(inputFile)
				.build();

        OBDAModel obdaModel = configuration.loadInputMappings()
				.orElseThrow(() -> new IllegalStateException("Mappings must be provided"));

		/*
		 * Start materializing data from database to triples.
		 */

		// TODO: try the streaming mode.
		try (OWLAPIMaterializer materializer = new OWLAPIMaterializer(obdaModel, false)) {
		
		long numberOfTriples = materializer.getTriplesCount();
		System.out.println("Generated triples: " + numberOfTriples);

		/*
		 * Obtain the triples iterator
		 */
		QuestOWLIndividualAxiomIterator triplesIter = materializer.getIterator();
		
		/*
		 * Print the triples into an external file.
		 */
		File fout = new File(outputFile);
		if (fout.exists()) {
			fout.delete(); // clean any existing output file.
		}
		
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fout, true)))) {
			while (triplesIter.hasNext()) {
				OWLIndividualAxiom individual = triplesIter.next();
				out.println(individual.toString());
			}
			out.flush();
		}
		}
	}

	public static void main(String[] args) {
		try {
			ABoxMaterializerExample example = new ABoxMaterializerExample();
			example.generateTriples();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
