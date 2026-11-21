package com.grupo3.betting.domain.match;

public class InvalidMatchTransitionException extends RuntimeException {
    public InvalidMatchTransitionException(String message) { super(message); }
}
