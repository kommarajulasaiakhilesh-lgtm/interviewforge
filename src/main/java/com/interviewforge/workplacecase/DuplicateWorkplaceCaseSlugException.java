package com.interviewforge.workplacecase;

public class DuplicateWorkplaceCaseSlugException extends RuntimeException {
    public DuplicateWorkplaceCaseSlugException() { super("A workplace case with this slug already exists."); }
}
