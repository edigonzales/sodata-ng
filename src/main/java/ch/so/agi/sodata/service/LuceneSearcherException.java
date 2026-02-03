package ch.so.agi.sodata.service;

public class LuceneSearcherException extends Exception {
    public LuceneSearcherException(String message) {
        super(message);
    }

    public LuceneSearcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
