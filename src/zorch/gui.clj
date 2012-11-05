(ns zorch.gui)

(defn show-warning [& msg]
  "Displays a non-fatal error message."
  ;; FIXME: GUI stuff to follow later.
  (.. System/err (println (apply str msg))))
