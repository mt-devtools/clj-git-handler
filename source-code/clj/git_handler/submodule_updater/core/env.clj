
(ns git-handler.submodule-updater.core.env
    (:require [git-handler.core.errors                      :as core.errors]
              [git-handler.submodule-updater.detector.env   :as submodule-updater.detector.env]
              [io.api                                       :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-config-value
  ; @ignore
  ;
  ; @description
  ; - Returns a specific configuration value.
  ; - If the value is not found in the config map of the submodule, it tries to get it from the default config map,
  ;   and if no value is found in the default config map either, it returns the given 'default-value'.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (*) config-key
  ; @param (*) default-value
  ;
  ; @usage
  ; (get-config-value {:config {"author/my-repository" {:target-branch "development"}      ;; <- The primary source of the configuration value is the configuration map of the submodule.
  ;                             :default               {:target-branch "default-branch"}}} ;; <- The secondary source of the configuration value is the default configuration map.
  ;                   "my-submodules/my-repository"
  ;                   :target-branch
  ;                   "fallback-branch")                                                   ;; <-  The fallback source of the configuration value is the given 'default-value'.
  ; =>
  ; "development"
  ;
  ;
  ; @return (*)
  [options submodule-path config-key & [default-value]]
  (if-let [repository-name (submodule-updater.detector.env/get-submodule-repository-name options submodule-path)]
          (or (get-in options [:config repository-name config-key])
              (get-in options [:config :default        config-key] default-value))
          (core.errors/error-catched (str "Cannot derive repository name from submodule path: '" submodule-path "'"))))
