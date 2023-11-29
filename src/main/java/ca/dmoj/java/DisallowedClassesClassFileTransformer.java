package ca.dmoj.java;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class DisallowedClassesClassFileTransformer implements ClassFileTransformer {
    protected DisallowedClassRule[] rules;

    public DisallowedClassesClassFileTransformer(DisallowedClassRule... rules) {
        this.rules = rules;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
                            throws IllegalClassFormatException {
        if (className == null) return classfileBuffer;

        Exception disallowed = null;
        for (DisallowedClassRule rule : rules) {
            // If the class ever loaded it's because a submission used it
            if (className.startsWith(rule.getClassName())) {
                disallowed = rule.getException();
                break;
            }
        }

        if (disallowed != null) ExceptionHandler.dumpExceptionAndExit(disallowed);

        // Don't actually retransform anything
        return classfileBuffer;
    }
}
