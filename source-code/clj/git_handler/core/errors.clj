
(ns git-handler.core.errors)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn error-catched
  ; @ignore
  ;
  ; @param (list of strings) error-message
  [& error-message]
  (println)
  (doseq [line error-message]
         (println line))
  (println)
  (throw (Exception. "Error catched")))
