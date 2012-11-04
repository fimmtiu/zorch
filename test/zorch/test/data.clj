(ns zorch.test.data
  (:use [zorch data util])
  (:use [clojure.test])
  (:import [java.nio ByteBuffer]))

(def roffset 3)
(def soffset 6)

(deftest basic
  (testing "load-bytes"
    (is (thrown? IllegalArgumentException #"load-bytes" (load-bytes 999 99)))
    (is (= (load-bytes 2 3) (map int (to-seq "nke")))))

  (testing "construct-word"
    (is (= 258 (construct-word 0x1 0x2))))

  (testing "load-words"
    (is (thrown? IllegalArgumentException #"load-bytes" (load-words 999 1)))
    (is (= '(28267 25977) (load-words 2 2))))

  (testing "load-byte"
    (is (thrown? IllegalArgumentException #"load-bytes" (load-byte 999)))
    (is (= (load-byte 2) (int \n))))

  (testing "load-word"
    (is (thrown? IllegalArgumentException #"load-bytes" (load-word 999)))
    (is (= (load-word 2) 28267)))

  (testing "unpack-routine-addr"
    (dorun (for [v [1 2 3]] (is (= (unpack-addr 0x10 roffset v) 0x20))))
    (dorun (for [v [4 5]] (is (= (unpack-addr 0x10 roffset v) 0x40))))
    (dorun (for [v [6 7]] (is (= (unpack-addr 0x10 roffset v) 0x58))))
    (is (= (unpack-addr 0x10 roffset 8) 0x80)))

  (testing "unpack-strings-addr"
    (dorun (for [v [1 2 3]] (is (= (unpack-addr 0x10 soffset v) 0x20))))
    (dorun (for [v [4 5]] (is (= (unpack-addr 0x10 soffset v) 0x40))))
    (dorun (for [v [6 7]] (is (= (unpack-addr 0x10 soffset v) 0x70))))
    (is (= (unpack-addr 0x10 soffset 8) 0x80))))

(deftest zork1
  (is (= (. @zorch.data/mem capacity) 92160)))

(defn test-ns-hook []
  (reset! zorch.data/mem (. ByteBuffer wrap (. "monkey" getBytes)))
  (basic)
  (load-story-file "data/zork1.z3")
  (zork1))
