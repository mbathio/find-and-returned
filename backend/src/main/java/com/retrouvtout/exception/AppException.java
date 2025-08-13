// AppException.java
package com.retrouvtout.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception générale de l'application
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class AppException extends RuntimeException {
    
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}