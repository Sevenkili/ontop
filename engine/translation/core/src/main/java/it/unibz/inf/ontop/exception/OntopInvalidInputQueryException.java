package it.unibz.inf.ontop.exception;

public class OntopInvalidInputQueryException extends OntopTranslationException {

    public OntopInvalidInputQueryException(String message) {
        super(message);
    }

    public OntopInvalidInputQueryException(Exception e) {
        super(e);
    }
}
