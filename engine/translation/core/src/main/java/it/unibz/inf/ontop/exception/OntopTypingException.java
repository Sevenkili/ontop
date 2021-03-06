package it.unibz.inf.ontop.exception;

/**
 * Thrown when Ontop is configured to not accept typing errors
 */
public class OntopTypingException extends OntopTranslationException {
    public OntopTypingException(String message) {
        super(message);
    }
}
