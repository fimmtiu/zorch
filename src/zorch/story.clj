;;; A container for most of the immutable data associated with a story.

(ns zorch.story
  (:use [zorch data util]))

(defrecord-singleton story Story
  [version high-mem-offset initial-pc dictionary-offset objects-offset
   globals-offset static-mem-offset])

(defn load-story-info []
  (let [version (load-byte 0x0)]
    (reset! story (Story. version                     ; version
                          (load-word 0x4)             ; high-mem-offset
                          (if (< version 6)           ; initial-pc
                            (load-word 0x6)
                            (unpack-routine-addr (load-word 0x6) version))
                          (load-word 0x8)             ; dictionary-offset
                          (load-word 0xA)             ; objects-offset
                          (load-word 0xC)             ; globals-offset
                          (load-word 0xE)))))         ; static-mem-offset
