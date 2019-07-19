# batman

A little bit of documentation and code on how "Not A Number" behaves
in Java and Clojure.

I give credit for the name of this project to Gary Bernhardt's
[delightful "Wat" talk](https://www.destroyallsoftware.com/talks/wat).


## Usage

To run some Clojure code tests in this repository:

```bash
$ clojure -m batman.nan
```

There are some sample output files produced by this program [in the
`doc` directory](doc).


## NaN is weird

By this I mean that the IEEE 754 floating point "Not A Number" NaN
'value' behaves in ways that you may not expect when you first run
across the idea.  Part of that weirdness comes from the IEEE
specification itself, for Clojure/Java part of that weirdness comes
from the Java implementation of operations on floating point numbers,
and Clojure/Java introduces some of its own oddities on top of all of
that.

Running the program above checks that all results of the evaluated
expressions have the expected values, where by expected values I mean
the ones that I verified were obtained with these OS, JDK, and Clojure
version combinations:

* OS X 10.13.6, Oracle JDK 1.8.0_192, Clojure 1.9.0 and 1.10.1
* OS X 10.13.6, AdoptOpenJDK 11.0.3, Clojure 1.9.0 and 1.10.1

I am not aware of any changes in the Java Language Specification in
this area for a long time.  I would not be surprised if the Java
portion of the operations here have been defined the same way, which
seem to often be according to IEEE 754 specification requirements,
since Java has been a language.

Many of these test use the `##NaN` symbolic expression, which was new
with Clojure 1.9.0.  It would only be a little bit tedious to make a
version of this program that skipped those tests if one wanted to run
the remaining ones with Clojure 1.8.0 or earlier, but I have not done
so.

References:

* Wikipedia [article about NaN](https://en.wikipedia.org/wiki/NaN)
* [Java
specification](https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.21.1)
  for comparing numbers using Java `==` and `!=` operators.
* [Java documentation on `equals` method for class
  `Double`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Double.html#equals(java.lang.Object))

There are more links to some specific behaviors in the output from the
program, and the source code in the batman.nan namespace.


## Other stuff

The file `double-equiv-tracing-patch-v1.diff` in the `doc` directory
is a patch that can be applied to Clojure 1.10.1 source code, and
enables printing of tracing debug information of Java method calls
that implement the behavior of `=` and `not=` when called with Java
double type arguments.  These can be helpful in determining what code
is executing when evaluating some of the Clojure expressions.


## License

Copyright © 2019 Andy Fingerhut

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.
