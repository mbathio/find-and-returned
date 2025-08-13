// OAuth2AuthenticationProcessingException.java
package com.retrouvtout.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception pour les erreurs d'authentification OAuth2
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {
    
    public OAuth2AuthenticationProcessingException(String msg, Throwable t) {
        super(msg, t);
    }

    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }
}