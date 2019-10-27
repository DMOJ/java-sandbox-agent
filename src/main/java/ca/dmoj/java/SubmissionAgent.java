package ca.dmoj.java;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class SubmissionAgent {
    public static void premain(String argv, Instrumentation inst) throws UnsupportedEncodingException {
        boolean unicode = false;
        boolean noBigMath = false;
        boolean noBuf = false;
        String policy = null;

        if (argv != null) {
            for (String opt : argv.split(",")) {
                if (opt.equals("unicode")) unicode = true;
                if (opt.equals("nobigmath")) noBigMath = true;
                if (opt.equals("nobuf")) noBuf = true;

                // Split on "policy:" so that paths like R:/tmp/security.policy don't get processed incorrectly
                if (opt.startsWith("policy:")) policy = opt.split("policy:")[1];
            }
        }

        if (policy == null) throw new IllegalStateException("must specify policy file");
        if (!new File(policy).exists()) throw new IllegalStateException("policy file does not exist: " + policy);

        final Thread selfThread = Thread.currentThread();

        if (noBigMath) {
            inst.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                        ProtectionDomain protectionDomain, byte[] classfileBuffer)
                                        throws IllegalClassFormatException {
                    if (className == null) return classfileBuffer;

                    RuntimeException disallowed = null;
                    // If the class ever loaded it's because a submission used it
                    if (className.startsWith("java/math/BigInteger") ||
                        className.startsWith("java/math/MutableBigInteger")) {
                        disallowed = new BigIntegerDisallowedException();
                    } else if (className.startsWith("java/math/BigDecimal")) {
                        disallowed = new BigDecimalDisallowedException();
                    }

                    if (disallowed != null) dumpExceptionAndExit(disallowed);

                    // Don't actually retransform anything
                    return classfileBuffer;
                }
            });
        }

        if (noBuf) {
            // Create output PrintStream set to autoflush:
            // > the output buffer will be flushed whenever a byte array is written, one of the println
            // > methods is invoked, or a newline character or byte ('\n') is written
            // This should be sufficient for interactive problems.
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true));
        } else {
            // Swap System.out for a faster alternative.
            System.setOut(new UnsafePrintStream(new FileOutputStream(FileDescriptor.out), unicode));
        }

        selfThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable error) {
                dumpExceptionAndExit(error);
            }
        });

        // UnsafePrintStream buffers heavily, so we must make sure to flush it at the end of execution.
        // Requires addShutdownHook permission
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.flush();
                System.err.flush();
            }
        }));

        // Set security policy here so that we don't need to grant submissions addShutdownHook, setIO and
        // writeFileDescriptor to all user submissions.
        System.setProperty("java.security.policy", policy);
        System.setSecurityManager(new SecurityManager());
    }

    private static void dumpExceptionAndExit(Throwable exception) {
        try {
            // Use a FileOutputStream instead of a File; otherwise, if the user.dir property permission
            // is given by a policy file, it could potentially allow malicious submissions to change the
            // cwd and have the state file written anywhere.
            // FileOutputStream ignores the user.dir property when resolving paths.
            // TODO: not a real concern, but this could be made to use the codebase path as a base for
            // an absolute path.
            PrintStream state = new PrintStream(new BufferedOutputStream(new FileOutputStream("state")));
            if (exception != null) {
                state.println(exception.getClass().getName());
            } else {
                // End with ! in the event that some sketchy user-defined exception is ever called OK;
                // ! is an invalid character in class names.
                state.println("OK!");
            }
            state.close();
        } catch (FileNotFoundException ignored) {
            // state file won't not exist, "abnormal termination" on the Python side
        }

        System.exit(1);
    }
}
