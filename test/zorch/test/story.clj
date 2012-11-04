(ns zorch.test.story
  (:use [zorch story core])
  (:use [clojure.test]))

(deftest story-zork1
  (is (= (story-version) 3))
  (is (= (story-high-mem-offset) 20023))
  (is (= (story-initial-pc) 20229))
  (is (= (story-dictionary-offset) 15137))
  (is (= (story-objects-offset) 688))
  (is (= (story-globals-offset) 8817))
  (is (= (story-static-mem-offset) 11859)))

(defn test-ns-hook []
  (load-story "data/zork1.z3")
  (story-zork1))
