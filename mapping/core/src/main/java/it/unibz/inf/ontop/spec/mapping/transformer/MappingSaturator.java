package it.unibz.inf.ontop.spec.mapping.transformer;


import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;

public interface MappingSaturator {

    Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox);
}
