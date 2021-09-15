# java-sandbox-agent [![Build Status](https://github.com/DMOJ/java-sandbox-agent/workflows/build/badge.svg)](https://github.com/DMOJ/java-sandbox-agent/actions/)
Java class sandbox with instrumentation support for online judges. 

It implements:

* a much faster, [unsynchronized ASCII-only `System.out` stream](https://github.com/DMOJ/java-sandbox-agent/blob/master/src/main/java/ca/dmoj/java/UnsafePrintStream.java) that's suitable for single-threaded online-judging scenarios
* support for **disallowing particular classes from being loaded**, to force certain solutions trivialized by the standard library
* optional **unbuffering of standard output**, for interactive problems without requiring users to flush manually
* logging of exceptional exits to a state file

## Usage

To run a class `Submission` with agent the following suffices:

```
$ java -client -javaagent:/code/java-sandbox-agent.jar=[option, ...] Submission
```

Supported fields for the `option` list are:

* `nobigmath` &mdash; disables `BigInteger` and `BigDecimal`, raising [appropriate exceptions](https://github.com/DMOJ/java-sandbox-agent/blob/master/src/main/java/ca/dmoj/java/BigIntegerDisallowedException.java) if they are used
* `unicode` &mdash; encodes `System.out` as UTF-8 instead of ASCII, sacrificing performance for Unicode support
* `nobuf` &mdash; sets `System.out` as being line-buffered, for interactive problems
