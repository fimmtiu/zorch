(ns zorch.test.objects
  (:use [zorch objects])
  (:use [clojure.test]))

(deftest attributes
  (testing "make-attributes"
    (is (= (:len (make-attributes '(1 2))) 16))
    (is (= (:len (make-attributes '(1 2 3 4 5 6))) 48))
    (is (= (:bits (make-attributes '(1 2))) 513))
    (is (= (:bits (make-attributes '(1 2 3 4 5 6))) 6618611909121)))

  (testing "has-attr?"
    (is (has-attr? (make-attributes '(1)) 7))
    (is (not (has-attr? (make-attributes '(1)) 0)))
    (is (has-attr? (make-attributes '(128)) 0))
    (for [i (range 1 8)]
      (is (not (has-attr? (make-attributes '(128)) i))))
    (is (not (has-attr? (make-attributes '(1)) 8))))

  (testing "set-attr"
    (is (has-attr? (set-attr (make-attributes '(1)) 7) 7))
    (is (has-attr? (set-attr (make-attributes '(0)) 7) 7))
    (is (not (has-attr? (set-attr (make-attributes '(0)) 2) 7)))
    (is (not (has-attr? (set-attr (make-attributes '(0)) 26) 26)))     ; out of range
    (is (has-attr? (set-attr (make-attributes '(1 2 3 4 5 6)) 26) 26)))

  (testing "clear-attr"
    (is (not (has-attr? (clear-attr (make-attributes '(1)) 7) 7)))
    (is (not (has-attr? (clear-attr (make-attributes '(0)) 7) 7)))
    (is (not (has-attr? (clear-attr (make-attributes '(0)) 2) 7)))
    (is (not (has-attr? (clear-attr (make-attributes '(0)) 26) 26)))     ; out of range
    (is (not (has-attr? (clear-attr (make-attributes '(1 2 3 4 5 6)) 26) 26)))))
