(ns zorch.objects
  (:use [zorch data story util gui]))

(defrecord Attributes [len bits])

(defn make-attributes [bytes]
  (->Attributes (* 8 (count bytes)) (seq->int bytes)))

(defn- access-attr [attrs i out-of-range fname func]
  (if (>= i (:len attrs))
    (do (show-warning "Attribute out of range for " fname ": " i)
        out-of-range)
    (let [bit (+ (* 8 (quot i 8)) (- 7 (rem i 8)))]
      (func attrs (bit-shift-left 1 bit)))))

(defn has-attr? [attrs i]
  (access-attr attrs i false "test" #(> (bit-and (:bits %1) %2) 0)))

(defn set-attr [attrs i]
  (access-attr attrs i attrs "set" #(assoc %1 :bits (bit-or (:bits %1) %2))))

(defn clear-attr [attrs i]
  (access-attr attrs i attrs "clear" #(assoc %1 :bits (bit-and (:bits %1) (bit-not %2)))))

(def object-tree (atom nil))
(def prop-defaults (atom nil))

(defn load-object-tree []
  (let [offset (load-word 0x0a)]
    ()))
