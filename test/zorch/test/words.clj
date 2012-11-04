(ns zorch.test.words
  (:use [zorch words core util])
  (:use [clojure.test]))

(deftest zscii
  (testing "z2a"
    (is (= (z2a 65) \A))
    (is (= (z2a 129) :arrow-up))
    (is (= (z2a 135) :function3))
    (is (= (z2a 145) :keypad2)))

  (testing "zchar 0"
    (is (= (zchar 0 0x00) \space))
    (is (= (zchar 0 0x06) \a))
    (is (= (zchar 0 0x1f) \z)))

  (testing "zchar 1"
    (is (= (zchar 1 0x00) \space))
    (is (= (zchar 1 0x06) \A))
    (is (= (zchar 1 0x1f) \Z)))

  (testing "zchar 2"
    (is (= (zchar 2 0x00) \space))
    (is (= (zchar 2 0x07) \newline))
    (is (= (zchar 2 0x1b) \\))
    (is (= (zchar 2 0x1f) \))))

  (testing "reverse-zchar"
    (is (= (reverse-zchar \space) [nil 0x00]))
    (is (= (reverse-zchar \newline) [2 0x07]))
    (is (= (reverse-zchar \a) [0 0x06]))
    (is (= (reverse-zchar \C) [1 0x08]))
    (is (= (reverse-zchar \0) [2 0x08]))
    (is (= (reverse-zchar \<) nil))
    (is (= (reverse-zchar \^) nil)))

  (testing "get-next-alpha"
    (is (= (get-next-alpha 0 2) 0))
    (is (= (get-next-alpha 0 4) 1))
    (is (= (get-next-alpha 2 2) 0))
    (is (= (get-next-alpha 2 4) 1))
    (is (= (get-next-alpha 0 3) 0))
    (is (= (get-next-alpha 0 5) 2))
    (is (= (get-next-alpha 2 3) 0))
    (is (= (get-next-alpha 2 5) 2)))

  (testing "make-zscii-seq"
    (is (= (doall (make-zscii-seq "honk")) '(13 20 19 16)))
    (is (= (doall (make-zscii-seq "HoNk")) '(4 13 20 4 19 16)))
    (is (= (doall (make-zscii-seq "Ho,Nk")) '(4 13 20 5 19 4 19 16)))
    (is (= (doall (make-zscii-seq "Ho,Nks0ck!")) '(4 13 20 5 19 4 19 16 24 5 8 8 16 5 20))))

  (testing "to-zstring"
    (is (= (map #(int %) (seq (to-zstring "honk"))) '(13971 49317)))
    (is (= (map #(int %) (seq (to-zstring "HoNk"))) '(4532 37488)))
    (is (= (map #(int %) (seq (to-zstring "Ho,Nk"))) '(4532 5732 52741)))
    (is (= (map #(int %) (seq (to-zstring "Ho,Nks0ck!"))) '(4532 5732 19992 5384 49332))))

  (testing "zstring"
    (is (= (zstring (to-zstring "honk")) "honk"))
    (is (= (zstring (to-zstring "HoNk")) "HoNk"))
    (is (= (zstring (to-zstring "Ho,Nk")) "Ho,Nk"))
    (is (= (zstring (to-zstring "Ho,Nks0ck!")) "Ho,Nks0ck!"))))

(deftest zscii-zork1
  (testing "read-abbr-string"
    (is (= (read-abbr-string 1 0) "the "))
    (is (= (read-abbr-string 1 1) "The "))
    (is (= (read-abbr-string 1 2) "You "))))

(defn test-ns-hook []
  (load-story "data/zork1.z3")
  (zscii-zork1))
