(ns zorch.objects
  (:use [zorch data story util gui]))

;; Because all of this stuff is located in dynamic memory, we have to be
;; fairly paranoid about not making Clojure objects out of things that
;; could potentially get changed during play -- object names, property
;; values, etc.
;;
;; Except for attributes. Man, screw *those* guys.

(defn- large-objects? []
  (> (story-version) 3))

(defn- object-len []
  (if (large-objects?) 14 9))

(defn- default-props-count []
  (if (large-objects?) 63 31))

(def object-tree (atom nil))
(def prop-defaults (atom nil))
(def prop-tables (atom nil))


;; Attributes!

(defrecord Attributes [len bits])

(defn- make-attributes [bytes]
  (->Attributes (* 8 (count bytes)) (seq->int bytes)))

(defn- access-attr [attrs i fname func]
  (if (>= i (:len attrs))
    (do (show-warning "Attribute out of range for " fname ": " i)
        false)
    (let [bit (+ (* 8 (quot i 8)) (- 7 (rem i 8)))]
      (func attrs (bit-shift-left 1 bit)))))

(defn- has-attr? [attrs i]
  (access-attr attrs i "test" #(> (bit-and (:bits %1) %2) 0)))

(defn- set-attr [attrs i]
  (access-attr attrs i "set" #(assoc %1 :bits (bit-or (:bits %1) %2))))

(defn- clear-attr [attrs i]
  (access-attr attrs i "clear" #(assoc %1 :bits (bit-and (:bits %1) (bit-not %2)))))


;; Properties!

(defrecord Property [num datalen addr size])

(defmulti load-property
  (fn [_ _] (if (large-objects?) [:large] [:small])))

(defmethod load-property [:small] [addr]
  (let [size (load-byte addr)]
    (if (zero? size)
      nil
      (let [num (rem size 32) len (quot size 32)]
        (->Property num len (inc addr) (inc len))))))

(defn- large-prop-len [size next]
  (cond
   (> (bit-and size 0x80) 0)   (if (> (bit-and next 0x3F) 0)
                                 (bit-and next 0x3F)
                                 64)
   (> (bit-and size 0x40) 0)   2
   :else                       1))

(defmethod load-property [:large] [addr]
  (let* [size (load-byte addr)
         len (large-prop-len size (load-byte (inc addr)))
         num (bit-and size 0x3F)
         offset (if (> (bit-and size 0x80) 0) 2 1)]
      (->Property num len (+ addr offset) (+ offset len))))

(defn- get-prop-data [prop]
  (load-bytes (:addr prop) (:len prop)))


;; Lots of properties!

(defrecord PropTable [namelen addr proplist])

(defn- load-prop-table [addr]
  (let [namelen (load-byte addr) templist (transient [])]
    (loop [propaddr (+ addr 1 (* 2 namelen))]
      (let [prop (load-property propaddr)]
        (when prop
          (conj! templist prop)
          (recur (+ propaddr (:size prop)))))
      (->PropTable name addr (persistent! templist)))))

(defn- load-property-tables []
  "Loads the property tables for every object in the object tree."
  (let [tempprops (transient {})]
    (doseq [obj object-tree]
      (conj! tempprops (load-prop-table (obj-prop-addr obj))))
    (reset! prop-tables (persistent! tempprops))))


;; FIXME LAST STOP -- everything below this point is still work-in-progress.


(defprotocol ZObject
  "A node in the game's object tree."
  (obj-has-attr? [obj attr] "Does this object have the given attribute set?")
  (obj-set-attr [obj attr] "Sets the given attribute flag on this object.")
  (obj-clear-attr [obj attr] "Clears the given attribute flag on this object.")
  (obj-prop [obj prop] "Returns the value of the given property for this object.")
  (obj-prop-addr [obj prop] "Like obj-prop, but returns the value's address.")
  (obj-prop-len [obj prop] "Returns the length of the property's value."))

(defrecord SmallZObject
;;  "A 9-byte object from versions 1 through 3."
  [id attrs parent-id sibling-id child-id prop-addr]
  ZObject
  (obj-prop-addr [obj prop]
    (:attrs obj
)

(defrecord LargeZObject
;;  "A 14-byte object from versions 4 through 8."
  [id attrs parent-id sibling-id child-id prop-addr]
  ZObject

)

(defmulti load-object
  (fn [_ _] (if (large-objects?) [:large] [:small])))

(defmethod load-object [:small] [id addr]
  (->SmallZObject id (make-attributes (load-bytes addr 4))
                  (load-word (+ addr 7)) (load-byte (+ addr 4))
                  (load-byte (+ addr 5)) (load-byte (+ addr 6))))

(defmethod load-object [:large] [id addr]
  (->LargeZObject id (make-attributes (load-bytes addr 6))
                  (load-word (+ addr 12)) (load-word (+ addr 6))
                  (load-word (+ addr 8)) (load-word (+ addr 10))))

(defn- load-object-tree []
  "Load all the objects into an array. (Yes, I know it's not a tree.)"
  (let [temptree (transient [])]
    (reset! prop-defaults (load-words (story-objects-offset) (default-props-count)))
    (loop [id 1
           offset (+ (* 2 (default-props-count)) (story-objects-offset))
           lowest-addr 999999999]
      (if (>= offset lowest-addr)
        (reset! object-tree (persistent! temptree))
        (do
          (conj! temptree (load-object id offset))
          (println (last temptree))
          (recur (inc id)
                 (+ offset (object-len))
                 (min (obj-prop-addr (last temptree)) lowest-addr)))))))

(defn load-objects-and-properties []
  "Load all the objects and property tables."
  (load-object-tree)
  (load-property-tables))
