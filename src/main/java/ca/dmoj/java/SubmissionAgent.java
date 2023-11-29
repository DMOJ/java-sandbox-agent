package ca.dmoj.java;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

public class SubmissionAgent {
    public static void premain(String argv, Instrumentation inst) throws UnsupportedEncodingException {
        boolean unsafe = false;
        boolean unicode = false;
        boolean noBigMath = false;
        boolean noBuf = false;

        if (argv != null) {
            for (String opt : argv.split(",")) {
                if (opt.equals("unsafe")) unsafe = true;
                if (opt.equals("unicode")) unicode = true;
                if (opt.equals("nobigmath")) noBigMath = true;
                if (opt.equals("nobuf")) noBuf = true;
            }
        }

        final Thread selfThread = Thread.currentThread();

        ArrayList<DisallowedClassRule> disallowedClassRules = new ArrayList<>();
        if (!unsafe) {
            disallowedClassRules.add(new DisallowedClassRule("sun/reflect/Unsafe", UnsafeDisallowedException::new));
            disallowedClassRules.add(new DisallowedClassRule("sun/misc/Unsafe", UnsafeDisallowedException::new));
        }
        if (noBigMath) {
            disallowedClassRules.add(new DisallowedClassRule("java/math/BigInteger", BigIntegerDisallowedException::new));
            disallowedClassRules.add(new DisallowedClassRule("java/math/MutableBigInteger", BigIntegerDisallowedException::new));
            disallowedClassRules.add(new DisallowedClassRule("java/math/BigDecimal", BigDecimalDisallowedException::new));
        }
        inst.addTransformer(new DisallowedClassesClassFileTransformer(disallowedClassRules.toArray(new DisallowedClassRule[0])));

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
                ExceptionHandler.dumpExceptionAndExit(error);
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
}
