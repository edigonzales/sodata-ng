package ch.so.agi.sodata.service;

public class InvalidLuceneQueryException extends Exception {
    public InvalidLuceneQueryException(String message) {
        super(message);
    }

    public InvalidLuceneQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
