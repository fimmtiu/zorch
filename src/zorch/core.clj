(ns zorch.core
  (:use [zorch story data words dict]))

(defn load-story [name]
  (load-story-file name)  ; data
  (load-story-info)       ; story
  (load-abbr-table)       ; words
  (load-dictionary))      ; dict
