package com.grupo3.betting.domain;

public class InvalidMatchTransitionException extends RuntimeException {
    public InvalidMatchTransitionException(String message) { super(message); }
}
