package com.jsp.ems.exception;

/** Unchecked wrapper for SQL / JDBC failures. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) { super(message, cause); }
}
