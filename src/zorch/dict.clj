(ns zorch.dict
  (:use [zorch data words story]))

(def word-separators (atom nil))
;; (def dict-entry-length (atom nil))   ;; FIXME: Is this even used anywhere else?
(def dictionary (atom nil))

(defn dict-lookup
  ([word] (dict-lookup word (story-version)))
  ([word version]
     (let [w (clojure.string/lower-case word) max-len (if (<= version 3) 6 9)]
       (@dictionary (subs w 0 (min max-len (count w)))))))

(defn load-dictionary []
  (let [offset (load-word 0x08)]
    (let [sep-count (load-byte offset)]
      (reset! word-separators (map #(char %) (load-bytes (inc offset) sep-count)))
      (let [dict-len (load-word (+ offset 2 sep-count))
            entry-len (load-byte (+ offset 1 sep-count))]
        (reset! dictionary
                (loop [i 0 dict (transient {})]
                  (let [addr (+ offset 4 sep-count (* i entry-len))]
                    (if (< i dict-len)
                      (recur (inc i) (conj! dict [(read-zstring addr 2) (load-bytes addr entry-len)]))
                      (persistent! dict)))))))))
