package ca.dmoj.java;

class ExceptionHandler {
    public static void dumpExceptionAndExit(Throwable exception) {
        System.err.print("7257b50d-e37a-4664-b1a5-b1340b4206c0: ");
        exception.printStackTrace();
        System.exit(1);
    }
}
