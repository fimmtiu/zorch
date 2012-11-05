(ns zorch.test.util
  (:use [zorch.util])
  (:use [clojure.test]))

(deftest test-to-seq-array
  (is (= '(\f \o \o) (to-seq (char-array '(\f \o \o))))))

(deftest test-to-seq-string
  (is (= '(\f \o \o) (to-seq "foo"))))

(deftest test-includes
  (is (includes? [1 2 3] 3))
  (is (nil? (includes? [1 2 3] 4))))

(deftest test-index-filter
  (is (= (index-filter #(= % \a) (seq "banana")) [1 3 5]))
  (is (empty? (index-filter #(= % \a) (seq "sununu")))))

(deftest test-index-of
  (is (= (index-of \e (seq "monkey")) 4))
  (is (nil? (index-of \q (seq "monkey")))))

(deftest test-seq->int
  (is (= (seq->int '(1 3 7 15)) 252117761))
  (is (= (seq->int [1 3 7 15]) 252117761))
  (is (= (seq->int '(1 3 7 15 31)) 133396103937)))
