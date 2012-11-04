(ns zorch.util)

;; How is there not a core function for doing this? I can't find one.
(defmulti to-seq "Converts a Java string or array to a Clojure sequence." class)
(defmethod to-seq String [str]
  (doall (seq (. str toCharArray))))
(defmethod to-seq :default [arr]
  (doall (seq arr)))

(defn includes? [coll elt]
  "Like contains?, except without the suck."
  (some #(= elt %) coll))

(defn index-filter [pred coll]
  "Returns a sequence of collection indices which satisfy the given predicate."
  (when pred
    (for [[idx elt] (map-indexed vector coll) :when (pred elt)] idx)))

(defn index-of [elt coll]
  "Returns the index of the first occurrence of 'elt' in 'coll'."
  (first (index-filter #(= % elt) coll)))

(defmacro defrecord-singleton [name class fields]
  "Generates a singleton record with a bunch of simple accessors."
  `(do (defrecord ~class ~fields)
       (def ~name (atom nil))
       ~@(for [n fields]
           `(defn ~(symbol (str name "-" n)) [] (~(keyword n) @~name)))))

;; FIXME possibly unused now
(defmacro defrecord-mutable-singleton [name class fields]
  "Generates a singleton record with a bunch of simple accessors."
  `(do (defrecord ~class ~fields)
       (def ~name (atom nil))
       ~@(for [n fields]
           `(defn ~(symbol (str name "-" n)) [] (~(keyword n) @~name)))
       ~@(for [n fields]
           `(defn ~(symbol (str "set-" name "-" n "!")) [newval#]
              (reset! ~name (assoc @~name ~(keyword n) newval#))))))
