(ns zorch.data
  (:use [zorch.util])
  (:import (java.io RandomAccessFile)
           (java.nio.channels FileChannel$MapMode)))

(def mem (atom nil))
(def routines-offset (atom nil))
(def strings-offset  (atom nil))

(defn load-bytes [addr len]
  (if (> (+ addr len) (. @mem capacity))
    (throw (IllegalArgumentException. "load-bytes request outside of memory")))
  (for [i (range addr (+ addr len))]
    (bit-and (int (. @mem get i)) 0xFF)))

(defn construct-word [high low]
  (bit-and (bit-or (bit-shift-left high 8) low) 0xFFFF))

(defn load-words [addr len]
  ;;(prn "addr" addr "blort" (partition 2 (load-bytes addr (* 2 len))))
  (map #(apply construct-word %) (partition 2 (load-bytes addr (* 2 len)))))

(defn load-byte [addr] (first (load-bytes addr 1)))
(defn load-word [addr] (first (load-words addr 1)))

(defn unpack-addr [addr offset version]
  (condp includes? version
    [1 2 3] (* 2 addr)
    [4 5]   (* 4 addr)
    [6 7]   (+ (* 4 addr) (* 8 offset))
    [8]     (* 8 addr)))

(defn unpack-routine-addr [addr version]
  (unpack-addr addr @routines-offset version))
(defn unpack-strings-addr [addr version]
  (unpack-addr addr @strings-offset version))

(defn load-story-file [filename]
  (let [f (. (RandomAccessFile. filename "rw") getChannel)]
    (reset! mem (. f map (. FileChannel$MapMode PRIVATE) 0 (. f size)))
    (reset! routines-offset (* (load-word 0x28) 8))
    (reset! strings-offset  (* (load-word 0x2A) 8))))










;; ;; The header format is S11.1 (page 60).
;; (make-atoms mem story flags1 flags2 abbrs-table standard-number)

;; (defrecord StoryFile [version high-mem-offset initial-pc dictionary-offset
;;                       objects-offset globals-offset static-mem-offset
;;                       routines-offset strings-offset])

;; ;; Public accessors!
;; (defn story-mem [] mem)
;; (make-story-accessors version initial-pc routines-offset strings-offset)

;; (defn load-story-file [filename]
;;   (reset! mem (char-array (slurp filename :encoding "US-ASCII")))
;;   (reset! flags1 (load-byte 0x1))
;;   (reset! flags2 (load-byte 0x10))
;;   (reset! abbrs-table (load-word 0x18))
;;   (reset! standard-number (load-word 0x32))
;;   (let [version (load-byte 0x0)]
;;     (reset! story (StoryFile. version                     ; version
;;                               (load-word 0x4)             ; high-mem-offset
;;                               (if (< version 6)           ; initial-pc
;;                                 (load-word 0x6)
;;                                 (unpack-routine-addr (load-word 0x6)))
;;                               (load-word 0x8)             ; dictionary-offset
;;                               (load-word 0xA)             ; objects-offset
;;                               (load-word 0xC)             ; globals-offset
;;                               (load-word 0xE)             ; static-mem-offset
;;                               (* (load-word 0x28) 8)      ; routines-offset
;;                               (* (load-word 0x2A) 8)))))  ; strings-offset


;; ;;;;;;;; LASCIATE

;; (defn- when-version [sf vers byte bit]
;;   "Return the value of the given bit if the interpreter's version is high enough."
;;   (and (>= (sf :version) vers) (bit-test (sc byte) bit)))

;; ;; The header format is S11.1 (page 60).
;; (defrecord Story-File [version flags1 flags2])

;; (defn can-show-images?   [sf] false)  ; (when-version sf 6 :flags1 1)
;; (defn game-wants-images? [sf] false)  ; (when-version sf 5 :flags2 3)
;; (defn game-wants-undo?   [sf] false)  ; (when-version sf 5 :flags2 4)
;; (defn game-wants-mouse?  [sf] false)  ; (when-version sf 5 :flags2 5)
;; (defn game-wants-colors? [sf] false)  ; (when-version sf 5 :flags2 6)
;; (defn game-wants-sound?  [sf] false)  ; (when-version sf 5 :flags2 7)
;; (defn game-wants-menus?  [sf] false)  ; (when-version sf 6 :flags2 8)
;; (defn has-bold?          [sf] false)  ; (when-version sf 4 :flags1 2)
;; (defn has-colours?       [sf] false)  ; (when-version sf 5 :flags1 0)
;; (defn has-italic?        [sf] false)  ; (when-version sf 4 :flags1 3)
;; (defn has-sound?         [sf] false)  ; (when-version sf 6 :flags1 5)
;; (defn has-timed-input?   [sf] false)  ; (when-version sf 4 :flags1 7)

;; (defn can-split-screen? [sf]
;;   (and (< (sf :version) 4) (bit-test (sf :flags1) 5)))

;; (defn default-font-is-monospace? [sf]
;;   (and (< (sf :version) 4) (bit-test (sf :flags1) 6)))

;; (defn status-line-mode [sf]
;;   "nil, :score or :time (S8.2, 8.2.3)"
;;   (if (>= (sf :version) 4)
;;     :time
;;     (if (bit-set? (sf :flags1) 4)
;;       nil
;;       (if (bit-set? (sf :flags1) 1) :time :score)))

;; (defn- load-mem [addr len]
;;   (when-not (or (in-dynamic-memory? addr) (in-static-memory? addr))
;;     (throw (Exception. "Can't load from high memory!")))
;;   (aget ))

