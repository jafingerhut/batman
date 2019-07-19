# batman

A little bit of documentation and code on how "Not A Number" behaves
in Java and Clojure.

I give credit for the name of this project to Gary Bernhardt's
[delightful "Wat" talk](https://www.destroyallsoftware.com/talks/wat).

And for another weird bit of fun, note that the bit representation of
NaNs in the IEEE 754 standard contains lots of bits that can have any
value, so you can use them to store data.  Don't try this anywhere
except at home:
[sneaky_NaNs.clj](https://gist.github.com/gfredericks/af951e56680127f10eb8).


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

Here are some relevant facts to be aware of, distilled from references
linked below:

* The IEEE 754 specification requires these behaviors, which Java
  includes in its language specification, and I have not seen a Java
  implementation yet that violates any of this:
 * If NaN is at least one operand of a floating point operation like
   `+` `-` `*` `/`, the result is NaN.
 * If NaN is at least one operand of an arithmetic equality or
   inequality comparison, the result must be false.  This makes `<=`
   _not_ a total order if you include NaN values.  It is a total order
   if you leave out NaN's.
* This is part of the Java language specification.  I do not yet know
  whether IEEE 754 requires it.
 * `NaN != NaN` is true!
* The Java `==` and `!=` operators, when comparing primitive double or
  float values, is defined to behave as described above.
* The Java `==` and `!=` operators can also be used to compare
  objects, which are not primitive values.  Types like Double and
  Float can contain floating point values within them.  In general,
  Java `==` for two objects tells you only whether they are the same
  object in memory.  For two different objects in memory, it always
  returns false, even if "the values inside the objects" are primitive
  values that would return true if you compared the primitive values
  with `==`.
* The Java method `equals` on Double objects is defined to return true
  when comparing two Double objects containing NaN.
 * This fact seems not very relevant to the Clojure 1.10.1
  implementation behavior, as it seems that the Java `equals` method
  is not used for Double objects by Clojure.  It is mentioned here
  primarily as a counterpoint to anyone who thinks that Java always
  gives the same answer for all ways of comparing NaN values to other
  things -- it does not.  Its behavior of `==` for primitive double
  values is based upon the IEEE 754 specification, and its behavior of
  `equals` for Double objects seems to have an exception for
  `(Double.NaN).equals(Double.NaN)` guided by the desire to be able to
  use `Double.NaN` as a key in a hash table that uses the Java
  `equals` method for key equality, and be able to look it up later.
* Clojure often uses "boxed" Double objects to represent
  double-precision floating point numbers, but also has optimizations
  in several places to represent them as Java variables/fields with
  primitive Java type `double`, for better performance.
* Different Java methods are used to implement Clojure's
  `clojure.core/=`, depending upon whether the call to `=` is inlined
  by the compiler, or called as a not-inlined function.  Inlining of
  several such calls is an important Clojure optimization for many
  common expressions, such as `=`, but in this case it can lead to
  different behavior if you do `(apply = ...)` instead of `(= ...)`.

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
