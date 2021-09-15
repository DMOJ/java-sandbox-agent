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

        if (argv != null) {
            for (String opt : argv.split(",")) {
                if (opt.equals("unicode")) unicode = true;
                if (opt.equals("nobigmath")) noBigMath = true;
                if (opt.equals("nobuf")) noBuf = true;
            }
        }

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
    }

    private static void dumpExceptionAndExit(Throwable exception) {
        System.err.print("7257b50d-e37a-4664-b1a5-b1340b4206c0: ");
        exception.printStackTrace();
        System.exit(1);
    }
}
