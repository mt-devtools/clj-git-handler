
(ns git-handler.submodule-updater.core.env
    (:require [git-handler.core.errors                      :as core.errors]
              [git-handler.core.utils                       :as core.utils]
              [git-handler.submodule-updater.detector.env   :as submodule-updater.detector.env]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [io.api                                       :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn git-url->submodule-path
  ; @ignore
  ;
  ; @description
  ; - Iterates over the detected submodules and finds which submodule has the same
  ;   'git-url' stored in the 'SUBMODULES' atom.
  ; - If a submodule has the same 'git-url', it returns the matching submodule's path
  ;   (found in the 'SUBMODULES' atom).
  ; - It can read four types of 'git-url' by using the 'git-url->repository-name' function.
  ;
  ; @param (string) git-url
  ;
  ; @usage
  ; (git-url->submodule-path "git@github.com:author/my-repository")
  ;
  ; @return (string)
  [git-url]
  (letfn [(f0 [[submodule-path submodule-props]]
              (if (= (core.utils/git-url->repository-name   git-url)
                     (core.utils/git-url->repository-name (:git-url submodule-props)))
                  (-> submodule-path)))]
         (some f0 @submodule-updater.detector.state/DETECTED-SUBMODULES)))

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
  (if-let [repository-name (-> submodule-path submodule-updater.detector.env/submodule-path->git-url core.utils/git-url->repository-name)]
          (or (get-in options [:config repository-name config-key])
              (get-in options [:config :default        config-key] default-value))
          (core.errors/error-catched (str "Cannot derive repository name from submodule path: '" submodule-path "'"))))
