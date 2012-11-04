(ns zorch.objects
  (:use [zorch data story]))

(def object-tree (atom nil))
(def prop-defaults (atom nil))

(defn load-object-tree []
  (let [offset (load-word 0x0a)]
    ()))
