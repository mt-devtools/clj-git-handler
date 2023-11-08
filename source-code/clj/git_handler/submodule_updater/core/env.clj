
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
  ; (git-url->submodule-path "git@github.com:author/repository")
  ;
  ; @return (string)
  [git-url]
  (letfn [(f [[submodule-path submodule-props]]
             (if (= (core.utils/git-url->repository-name   git-url)
                    (core.utils/git-url->repository-name (:git-url submodule-props)))
                 (-> submodule-path)))]
         (some f @submodule-updater.detector.state/DETECTED-SUBMODULES)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-config-item
  ; @ignore
  ;
  ; @description
  ; - Returns a specified configuration value.
  ; - If the value is not found in the 'options' map under the submodule's name,
  ;   it tries to get it from the default values (in the 'options' map) and if no
  ;   default value is found either it returns the given 'default-value'.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (*) config-key
  ; @param (*) default-value
  ;
  ; @usage
  ; (get-config-item {:config  {"author/my-repository" {:target-branch "development"}} <- 1. Tries to get the value from the submodule's config
  ;                   :default {:target-branch "default-branch"}}                      <- 2. Tries to get the value from the default config
  ;                  "fallback-branch")                                                <- 3. If neither is found, it returns the provided 'default-value'
  ;
  ; @return (*)
  [options submodule-path config-key & [default-value]]
  (if-let [repository-name (-> submodule-path submodule-updater.detector.env/submodule-path->git-url core.utils/git-url->repository-name)]
          (do  (println "path:" [repository-name config-key])
            (or (get-in options [repository-name config-key])
                (get-in options [:default        config-key] default-value)))
          (core.errors/error-catched (str "Cannot derive repository name from submodule path: '" submodule-path "'"))))
