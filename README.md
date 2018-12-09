# java-sandbox-agent [![Build Status](https://ci.dmoj.ca/job/dmoj-java-sandbox/badge/icon)](https://ci.dmoj.ca/job/dmoj-java-sandbox/)
Policy-based Java sandbox with instrumentation support for online judges.

It implements:

* [**security policy-based sandboxing**](https://docs.oracle.com/javase/7/docs/technotes/guides/security/PolicyFiles.html) (see [**DMOJ/judge**](https://github.com/DMOJ/judge) for [example policy](https://github.com/DMOJ/judge/blob/master/dmoj/executors/java-security.policy))
* a much faster, [unsynchronized ASCII-only `System.out` stream](https://github.com/DMOJ/java-sandbox-agent/blob/master/src/main/java/ca/dmoj/java/UnsafePrintStream.java) that's suitable for single-threaded online-judging scenarios
* support for **disallowing particular classes from being loaded**, to force certain solutions trivialized by the standard library
* optional **unbuffering of standard output**, for interactive problems without requiring users to flush manually
* logging of exceptional exits to a state file

## Usage

To run a class `Submission` with agent and policy stored in `/code`, the following suffices:

```
$ java -client -javaagent:/code/java-sandbox-agent.jar=policy:/code/policy[,option ...] Submission
```

After execution, exit state will be written to a `state` file in the same directory as `Submission.class`, containing the name of the exiting exception, or `OK!` if there was none.

Supported fields for the `option` list are:

* `nobigmath` &mdash; disables `BigInteger` and `BigDecimal`, raising [appropriate exceptions](https://github.com/DMOJ/java-sandbox-agent/blob/master/src/main/java/ca/dmoj/java/BigIntegerDisallowedException.java) if they are used
* `unicode` &mdash; encodes `System.out` as UTF-8 instead of ASCII, sacrificing performance for Unicode support
* `nobuf` &mdash; sets `System.out` as being line-buffered, for interactive problems

