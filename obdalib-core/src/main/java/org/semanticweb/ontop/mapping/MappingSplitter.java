package org.semanticweb.ontop.mapping;

/*
 * #%L
 * ontop-obdalib-core
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

import java.net.URI;
import java.util.*;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.utils.IDGenerator;

/**
 * This class split the mappings
 * 
 *  <pre> q1, q2, ... qn <- SQL </pre>
 *  
 *   into n mappings
 *   
 *  <pre> q1 <-SQL , ..., qn <- SQL </pre>
 * 
 * 
 * @author xiao
 *
 */
public class MappingSplitter {

	private static List<OBDAMappingAxiom> splitMappings(List<OBDAMappingAxiom> mappings,
														NativeQueryLanguageComponentFactory nativeQLFactory) {

		List<OBDAMappingAxiom> newMappings = new ArrayList<OBDAMappingAxiom>();
		
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

		for (OBDAMappingAxiom mapping : mappings) {

			String id = mapping.getId();

			CQIE targetQuery = (CQIE) mapping.getTargetQuery();

			Function head = targetQuery.getHead();
			List<Function> bodyAtoms = targetQuery.getBody();

			if(bodyAtoms.size() == 1){
				// For mappings with only one body atom, we do not need to change it
				newMappings.add(mapping);
			} else {
				for (Function bodyAtom : bodyAtoms) {
					String newId = IDGenerator.getNextUniqueID(id + "#");
					
					CQIE newTargetQuery = dfac.getCQIE(head, bodyAtom);
					OBDAMappingAxiom newMapping = nativeQLFactory.create(newId, mapping.getSourceQuery(), newTargetQuery);
					newMappings.add(newMapping);
				}
			}

		}

		return newMappings;

	}

	/**
	 * this method split the mappings in {@link obdaModel} with sourceURI 
	 * 
	 * @param obdaModel
	 * @param sourceURI
	 */
	public static OBDAModel splitMappings(OBDAModel obdaModel, URI sourceURI, NativeQueryLanguageComponentFactory nativeQLFactory) {
		ImmutableList<OBDAMappingAxiom> splittedMappings = ImmutableList.copyOf(splitMappings(obdaModel.getMappings(sourceURI),
				nativeQLFactory));

        Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>(obdaModel.getMappings());
        //Overwrite
        mappingIndex.put(sourceURI, splittedMappings);
        try {
            return obdaModel.newModel(obdaModel.getSources(), mappingIndex);
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Error: Duplicate Mappings generated by the MappingSplitter");
        }
	}
}
