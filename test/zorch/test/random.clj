(ns zorch.test.random
  (:use [zorch.random])
  (:use [clojure.test]))

(defn get-a-thing [g max]
  (+ 1 (- (. g nextInt max) 1)))

(defn get-five
  ([]
     (doall (repeatedly 5 #(get-random))))
  ([seed]
     (set-random-seed seed)
     (get-five)))

(deftest get-and-set-seed
  (set-random-seed 10)
  (is 10 (get-random-seed)))

(deftest randomize-seed
  (let [a (get-five 10)]
    (remove-random-seed)
    (is (not= 10 (get-random-seed)))
    (is (not= a (get-five)))))

(deftest seeds-make-stuff-predictable
  (let [a (get-five 10)]
    (remove-random-seed)
    (get-random)
    (let [b (get-five 10)]
      (is (= a b)))))

(deftest not-the-same
  (let [a (get-five 10)]
    (is (= (count (distinct a)) (count a)))))
