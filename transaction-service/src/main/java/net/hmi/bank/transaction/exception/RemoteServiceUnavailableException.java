package net.hmi.bank.transaction.exception;

public class RemoteServiceUnavailableException extends RuntimeException {
    public RemoteServiceUnavailableException(String message) {
        super(message);
    }

    public RemoteServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}