package com.interviewforge.practice;

public class InsufficientQuestionsException extends RuntimeException {
    public InsufficientQuestionsException(int requested, int available) {
        super("Requested " + requested + " questions, but only " + available + " matching questions are available.");
    }
}
