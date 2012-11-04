(ns zorch.words
  (:use [zorch data story util]))

(def abbr-table-offset (atom nil))

(def alphabets
  [[\space nil nil nil nil nil \a \b \c \d \e \f \g \h \i \j \k \l
    \m \n \o \p \q \r \s \t \u \v \w \x \y \z]
   [\space nil nil nil nil nil \A \B \C \D \E \F \G \H \I \J \K \L
    \M \N \O \P \Q \R \S \T \U \V \W \X \Y \Z]
   [\space nil nil nil nil nil nil \newline \0 \1 \2 \3 \4 \5 \6 \7 \8 \9
    \. \, \! \? \_ \# \' \" \/ \\ \- \: \( \)]])

(defn zchar [alpha index]
  "Returns the ASCII character corresponding to a given Z-character."
  ((alphabets alpha) (int index)))

(defn z2a [z]
  "Returns the ASCII character corresponding to a given ZSCII code."
  (condp includes? z
    [0]  (char 0x00)   ; null
    [8]  (char 0x7f)   ; delete
    [9]  \tab
    [11] \space
    [13] \newline
    [27] (char 0x1b)   ; escape
    (range 32 127) :>> #(char %)
    [129] :arrow-up
    [130] :arrow-down
    [131] :arrow-left
    [132] :arrow-right
    (range 133 145) :>> #(symbol (str "function" %))
    (range 145 155) :>> #(symbol (str "keypad" %))
    (range 155 252) \?    ; FIXME: Add Unicode support!
    [252] :menu-click
    [253] :double-click
    [254] :single-click
    \?
    ))

;; I don't care if it's efficient; it only gets used in the tests.
(defn reverse-zchar [ch]
  "Converts an ASCII character to a vector: [alphabet #, Z-char]."
  (letfn [(find-char [n]
            (let [i (index-of ch (alphabets n))]
              (and i [n i])))]
    (if (= ch \space)
      [nil 0]
      (or (find-char 0) (find-char 1) (find-char 2)))))

(defn get-next-alpha [cur esc]
  "Get the alphabet corresponding to the given escape character."
  (condp = (int esc)
    4    1
    5    2
    0))

;; FIXME: Replace string accumulator with a transient.
(defn make-zscii-seq [st]
  "Converts a string to a sequence of Z-characters (not yet packed)."
  (letfn [(add-char [[alpha index] result]
            (condp = alpha
              nil :>> (fn [_] (conj result index))
              0   :>> (fn [_] (conj result index))
              1   :>> (fn [_] (add-char [0 index] (conj result 4)))
              2   :>> (fn [_] (add-char [0 index] (conj result 5)))))]
    (let [zchars (for [ch (seq st)] (reverse-zchar ch))]
      (seq (reduce #(add-char %2 %1) [] zchars)))))

(defn pack-triplet [final? a b c]
  "Converts three ASCII characters into a two-byte packed Z-character sequence."
  (let [first-byte (bit-or (if final? 128 0)
                           (bit-shift-left (int a) 2)
                           (bit-shift-right (int b) 3))
        second-byte (bit-or (int c)
                            (bit-and (bit-shift-left (int b) 5) 0xFF))]
    (char (bit-or (bit-shift-left first-byte 8) second-byte))))

(defn to-zstring
  "Converts an ASCII string to a packed Z-string."
  ([st] (to-zstring st (story-version)))
  ([st version]
     (letfn [(mark-final [coll]
               (concat (map #(conj % false) (butlast coll))
                       (list (conj (last coll) true))))]
       (let [triplets (partition 3 3 (repeat 5) (make-zscii-seq st))]
         (apply str (map #(apply pack-triplet %) (mark-final triplets)))))))

(defn unpack-triplet [char16]
  "Converts two bytes of packed Z-characters into three separate Z-characters."
  (let [ch (int char16)]
    (map #(char %)
         (list (bit-and (bit-shift-right ch 10) 0x1F)
               (bit-and (bit-shift-right ch 5) 0x1F)
               (bit-and ch 0x1F)))))

(declare zstring)

(defn read-zstring
  "Reads Z-characters from the given address until it finds a terminator."
  ([addr] (read-zstring addr (. Integer MAX_VALUE)))
  ([addr max-words]
     (letfn [(accumulate-words [addr n acc]
               (let [word (load-word addr)]
                 ; (prn "word" word "addr" addr "terminal?" (or (> (bit-and word 0x8000) 0) (>= n max-words)))
                 (if (or (> (bit-and word 0x8000) 0) (>= n max-words))
                   (zstring (conj acc word))
                   (accumulate-words (+ addr 2) (inc n) (conj acc word)))))]
       (accumulate-words addr 1 []))))

(defn read-abbr-string [prefix index]
  "Looks up and returns an abbreviation from the abbreviation table."
  (let [handle (+ @abbr-table-offset (* 64 (- prefix 1)) (* 2 index))]
    (read-zstring (* 2 (load-word handle)))))

(defn zstring
  "Returns the ASCII representation of a packed Z-string."
  ([st] (zstring st (story-version)))
  ([st version]
     (let [zchars (flatten (map #(unpack-triplet %) st))]
       ; (prn "st" st "zchars" (map #(int %) zchars))
       (loop [coll zchars alpha 0 acc ""]
         (if (empty? coll)
           acc
           (let [i (int (first coll))]
             (cond
              (includes? [1 2 3] i)              ; abbreviations
                (recur (nnext coll) 0 (str acc (read-abbr-string i (int (second coll)))))
              (includes? [4 5] i)                ; changing alphabets
                (recur (rest coll) (- i 3) acc)
              (and (= i 6) (= alpha 2))          ; 10-bit ZSCII character escapes
                (let [zscii (bit-or (bit-shift-left (int (nth coll 2)) 5)
                                    (int (nth coll 3)))]
                  (recur (nthnext coll 3) 0 (str acc (z2a zscii))))
              true                               ; ordinary characters
                (recur (rest coll) 0 (str acc (zchar alpha (first coll)))))))))))

(defn load-abbr-table []
  (reset! abbr-table-offset (load-word 0x18)))
