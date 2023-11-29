package ca.dmoj.java;

public class DisallowedClassRule {
    protected interface ExceptionFactory {
        Exception create();
    }

    protected String className;
    protected ExceptionFactory exception;

    public DisallowedClassRule(String className, ExceptionFactory exception) {
        this.className = className;
        this.exception = exception;
    }

    public String getClassName() {
        return className;
    }

    public Exception getException() {
        return exception.create();
    }
}
