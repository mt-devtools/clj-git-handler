
(ns git-handler.core.errors)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn error-catched
  ; @ignore
  ;
  ; @param (list of strings) error-message
  [& error-message]
  (doseq [line error-message]
         (println line))
  (throw :error-catched))
