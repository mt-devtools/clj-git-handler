
(ns git-handler.submodule-updater.updater.env
    (:require [git-handler.core.env                   :as core.env]
              [git-handler.core.errors                :as core.errors]
              [git-handler.submodule-updater.core.env :as submodule-updater.core.env]
              [time.api                               :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-next-commit-message
  ; @ignore
  ;
  ; @param (map) options
  ; {:config (map)(opt)
  ;   {"author/my-repository" (map)(opt)
  ;     {:commit-message-f (function)(opt)
  ;      :target-branch (string)(opt)}
  ;    :default (map)(opt)
  ;     {:commit-message-f (function)(opt)
  ;      :target-branch (string)(opt)}}
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @return (string)
  [options submodule-path branch]
  (letfn [(f0 [_] (time/timestamp-string))]
         (let [commit-message-f (submodule-updater.core.env/get-config-item options submodule-path :commit-message-f f0)]
              (if-let [last-local-commit-message (core.env/get-submodule-last-local-commit-message submodule-path branch)]
                      (or (try (commit-message-f last-local-commit-message)
                               (catch Exception e nil))
                          (core.errors/error-catched (str "Cannot create commit message for: '" submodule-path "'")))))))
