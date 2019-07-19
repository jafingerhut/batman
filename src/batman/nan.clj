(ns batman.nan)


(def unexpected-result-count (atom 0))


(defn unexpected [pred result]
  (when-not (pred :check result)
    (swap! unexpected-result-count inc)
    (println "UNEXPECTED RESULT!")
    (println (pred :explain result))
    (println)))


(def simulated-prompt "x=>")


(defn show-expr [expr]
  (println "----------------------------------------")
  (println "x=>" expr))


(defmacro show [pred expr]
  `(do
     (show-expr '~expr)
     ;;(println simulated-prompt '~expr)
     (let [result# ~expr]
       (println result#)
       (println)
       (unexpected ~pred result#))))


(defn expect-= [expected-val]
  (fn [action result]
    (case action
      :check (= expected-val result)
      :explain (str "Expected result to be clojure.core/= to: " expected-val))))


(defn expect-nan []
  (fn [action result]
    (case action
      :check (.isNaN result)
      :explain (str "Expected result to be NaN"))))


(defn run1
  "Show results of various calls of identical?"
  []

  (show (expect-= false) (identical? Double/NaN Double/NaN))
  (println
"Every time the Clojure reader reads Double/NaN, it returns a new Java
object of type Double.  identical? only returns true if two Java
objects are the same one in memory, but these are not.")
  
  (show (expect-= true) (identical? ##NaN ##NaN))
  (println
"At least in Clojure 1.10.1, the Clojure reader returns the same
identical Java object every time it reads the expression ##NaN.  Thus
two occurrences of them are identical.")
  
  (show (expect-= false) (identical? ##NaN Double/NaN))
  (println
"Given the above results, you should not be surprised that these
objects are not the same one in memory.")
  
  )


;; TBD: Is there a built-in way in Clojure or via Java interop to call
;; a method that compares to primitive double values using Java == ?


(defn run2
  "Show results of various calls of Java equals method"
  []

  (show (expect-= true) (.equals Double/NaN Double/NaN))
  (println

"When the equals method of Java's class Double is passed a value of
Double.NaN to compare it against, the Java documentation clearly says
that equals returns true:

https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Double.html#equals(java.lang.Object)

I am not so clear why that documentation says: 'This definition allows
hash tables to operate properly.'")
  
  (show (expect-= true) (.equals ##NaN ##NaN))
  (println
"Same result as previous one, for the same reason.  It does not matter
whether the two Double objects are the same one in memory or not for
the equals method.")

  (show (expect-= true) (.equals ##NaN Double/NaN))
  (println
"Same as previous one.")

  (show (expect-= false) (.equals ##NaN Float/NaN))
  (println
"Float.NaN is a different class of object than Double.NaN, so Java
equals returns false here.")
  
  )


(defn run3
  "Show results of various calls of Clojure = function"
  []


  (show (expect-= false) (= Double/NaN Double/NaN))
  (println
"This causes the method clojure.lang.Util/equiv(double, double) to be called,
which compares the two primitive double values using the Java ==
operator, which returns false.

According to the Java Language Specification section 15.21.1
'Numerical Equality Operators == and !=' linked below, if either
operand of the Java == operator is NaN, the result is false.
Interestingly, it also specifies that the value of the expression 'NaN
!= NaN' is true!

https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.21.1")

  (show (expect-= false) (clojure.lang.Util/equiv Double/NaN Double/NaN))
  (println
"This is just the Clojure Java interop expression that calls the same
method as the previous example.  It returns the same result for the
same reason.")

  (show (expect-= false) (apply = [Double/NaN Double/NaN]))
  (println
"Calls clojure.lang.Util.equiv(Object, Object), which is different
than the method called due to (= Double/NaN Double/NaN) because that
expression is inlined by the Clojure compiler.  See the :inline key in
the definition of function = in the Clojure core.clj source file.

The calls go in this order:

clojure.core/=
clojure.lang.Util.equiv(Object, Object)
clojure.lang.Numbers.equal(Number, Number)
clojure.lang.DoubleOps.equiv(Number, Number)

which evaluates the expression (x.doubleValue() == y.doubleValue()),
which is (Double.NaN == Double.NaN), which is false.")

  (show (expect-= false) (apply = [##NaN ##NaN]))
  (println
"Same sequence of calls and same return value as the previous case.")

  (show (expect-= false) (= ##NaN ##NaN))
  (println
"This case calls clojure.lang.Util.equiv(double, double) just like (=
Double/NaN Double/NaN) does.")
  
  (show (expect-= false) (= ##NaN Double/NaN))
  (println
"Same as previous case.")

  (show (expect-= false) (not= ##NaN ##NaN))
  (println
"clojure.lang.Util.equiv(Object, Object) which takes true branch on if
condition (k1 == k2) because ##NaN are all identical? to each other,
and equiv() returns true.  Then not= function takes that result and
negates it to false.")

  (show (expect-= true) (#'= ##NaN ##NaN))
  (println
"Performs the same calls as previous case, except since there is no
not= function involved, clojure.core/= returns true.")

  (show (expect-= false) (apply = [##NaN ##NaN]))
  (println
"TBD: Why does this case not take the same if (k1 == k2) true branch
that (#'= ##NaN ##NaN) does?  I do not understand what is different
here, unless somewhere the Clojure compiler is creating new Double
objects while constructing the vector object, perhaps?

Call sequence:

clojure.core/apply
clojure.core/=
clojure.lang.Util.equiv(Object, Object)
clojure.lang.Numbers.equal(Number, Number)
clojure.lang.DoubleOps.equiv(Number, Number)

which evaluates the expression (x.doubleValue() == y.doubleValue()),
which is (Double.NaN == Double.NaN), which is false.")

  (show (expect-= true) (apply not= [##NaN ##NaN]))
  (println
"TBD: Similarly weird as for (apply = [##NaN ##NaN]), and except for
not= negating the return from false to true, everything is the same
between them.")

  (show (expect-= true) (apply not= ##NaN [##NaN]))
  (println
"TBD: Similarly weird as for (apply = [##NaN ##NaN]), and except for
not= negating the return from false to true, everything is the same
between them.")

  (show (expect-= false) (apply not= ##NaN ##NaN []))
  (println
"Call trace:

clojure.core/not=
clojure.core/=
clojure.lang.Util.equiv(Object, Object)
Takes the true branch of if (k1 == k2) statement, returning true.

Then not= negates to false.

This is most similar to (not= ##NaN ##NaN) case.")

  (show (expect-= true) (not= Double/NaN Double/NaN))
  (println
"First calls clojure.core/not=, then the same sequence of method calls
as for the expression (apply = [Double/NaN Double/NaN]) above.  That
returns false, then not= negates it to return true.")
  
  (show (expect-= false) (#'= Double/NaN Double/NaN))
  (println
"Same sequence of calls as for (apply = [Double/NaN Double/NaN]))
above, and same return value.")

  (show (expect-= false) (= Double/NaN Double/NaN))
  (println
"clojure.lang.Util.equiv(double, double) returns false.")

  )


(defn run4
  "Arithmetic comparison operations with NaN"
  []

  (show (expect-= false) (== Double/NaN Double/NaN))
  (println
"All arithmetic comparison operations between NaN and a normal double
number return false.

This matches Java's behavior in the Java Language Specification
section 15.20.1 'Numerical Comparison Operators <, <=, >, >=', which
says that it is determined by the IEEE 754 standard.

https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.20.1")
  
  (show (expect-= false) (< Double/NaN 5.0))
  (println
"See (== Double/NaN Double/NaN) above")
  (show (expect-= false) (< 5.0 Double/NaN))
  (println
"See (== Double/NaN Double/NaN) above")
  (show (expect-= false) (<= Double/NaN 5.0))
  (println
"See (== Double/NaN Double/NaN) above")
  (show (expect-= false) (<= 5.0 Double/NaN))
  (println
"See (== Double/NaN Double/NaN) above")
  (show (expect-= false) (> Double/NaN 5.0))
  (println
"See (== Double/NaN Double/NaN) above")
  (show (expect-= false) (> 5.0 Double/NaN))
  (println
"See (== Double/NaN Double/NaN) above")
  (show (expect-= false) (>= Double/NaN 5.0))
  (println
"See (== Double/NaN Double/NaN) above")
  (show (expect-= false) (>= 5.0 Double/NaN))
  (println
"See (== Double/NaN Double/NaN) above")

  )

(defn run5
  "Arithmetic operations with NaN"
  []

  (show (expect-nan) (+ 5.0 ##NaN))
  (println
"All unary and binary arithmetic operations between NaN and a normal
double number return NaN.

This matches Java's behavior in the Java Language Specification
sections listed below, which say that it is determined by the IEEE 754
standard.

https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.15.4
https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.17.1
https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.17.2
https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.18.2")
  (show (expect-nan) (+ ##NaN))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (+ ##NaN 5.0))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (- 5.0 ##NaN))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (- ##NaN 5.0))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (- ##NaN))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (* 5.0 ##NaN))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (* ##NaN 5.0))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (/ 5.0 ##NaN))
  (println
"See (+ 5.0 ##NaN) above")
  (show (expect-nan) (/ ##NaN 5.0))
  (println
"See (+ 5.0 ##NaN) above")

  )


(defn print-properties [props property-names]
  (doseq [nm property-names]
    (println nm " " (get props nm))))


(defn -main
  [& args]
  (run1)
  (run2)
  (run3)
  (run4)
  (run5)
  (println)
  (println "Number of unexpected results:" @unexpected-result-count)
  (print-properties (System/getProperties)
                    ["os.arch" "os.name" "os.version"
                     "java.vendor" "java.version"
                     "java.vm.vendor" "java.vm.name" "java.vm.version"
                     "java.runtime.version"])
  (println "clojure-version" (clojure-version)))


;; Observations by @souenzzo

;; (= ##NaN ##NaN)   ;; run3
;; => false
;; (not= ##NaN ##NaN)  ;; run3
;; => false
;; (apply = [##NaN ##NaN])  ;; run3
;; => false
;; (apply not= [##NaN ##NaN])  ;; run3
;; => true

;; Observations by @alexmiller

;; user=> (apply not= [##NaN ##NaN])  ;; run3
;; true
;; user=> (apply not= ##NaN [##NaN])  ;; run3
;; true
;; user=> (apply not= ##NaN ##NaN [])  ;; run3
;; false


;; Observations by @andy.fingerhut

;; user=> (#'= ##NaN ##NaN)  ;; run3
;; true
;; user=> (= ##NaN ##NaN)  ;; run3
;; false

;; Observation by @potetm

;; (not= ##NaN
;;       ##NaN)    ;; run3
;; => false
;; (not= Double/NaN  ;; run3
;;       Double/NaN)
;; => true


(comment

(require '[batman.nan :as n] :reload)

(n/run1)
(n/run2)
(n/run3)
(n/run4)
(n/run5)
(println "Number of unexpected results:" @n/unexpected-result-count)
(n/run-all)

)
