(ns zorch.random
  (:import (java.util Random)))

(def seed (atom nil))
(def gen (atom (new Random)))

(defn get-random-seed []
  @seed)

(defn set-random-seed [new-seed]
  (reset! seed new-seed)
  (. @gen setSeed new-seed))

(defn get-random
  ([] (get-random 32767))
  ([max] (+ 1 (- (. @gen nextInt max) 1))))

(defn remove-random-seed []
  (reset! seed nil)
  (reset! gen (new Random)))
