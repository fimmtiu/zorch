(ns zorch.test.dict
  (:use [zorch dict core])
  (:use [clojure test]))

(deftest dict-zork1
  (testing "load-dictionary"
    (is (= @word-separators '(\, \. \")))
    (is (= (count @dictionary) 697))
    (is (= (@dictionary "egypti") '(41 158 215 46 34 224 0)))
    (is (= (@dictionary "zorkmi") '(126 151 194 78 128 1 0)))
    (is (= (@dictionary "xyzzy")  '(119 223 255 197 65 207 0))))

  (testing "dict-lookup"
    (is (= (dict-lookup "egyptian") '(41 158 215 46 34 224 0)))
    (is (= (dict-lookup "Egyptian") '(41 158 215 46 34 224 0)))
    (is (= (dict-lookup "zorkmid")  '(126 151 194 78 128 1 0)))
    (is (= (dict-lookup "xyzzy")    '(119 223 255 197 65 207 0)))))

(defn test-ns-hook []
  (load-story "data/zork1.z3")
  (dict-zork1))
