
(ns git-handler.submodule-updater.builder.side-effects
    (:require [fruits.vector.api                            :as vector]
              [git-handler.core.errors                      :as core.errors]
              [git-handler.submodule-updater.builder.env    :as submodule-updater.builder.env]
              [git-handler.submodule-updater.detector.env :as submodule-updater.detector.env]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn build-dependency-cascade!
  ; @ignore
  ;
  ; @note
  ; The dependency cascade is an ordered vector of submodules where submodules cannot precede their dependencies.
  ;
  ; @description
  ; Iterates over the detected submodules adding their paths to the dependency cascade.
  ;
  ; @param (map) options
  [options]
  (let [dependency-cascade (submodule-updater.builder.env/get-dependency-cascade options)]
       (when-not (submodule-updater.builder.env/dependency-cascade-built? options)
                 (doseq [[submodule-path _] (submodule-updater.detector.env/get-detected-submodules options)]
                        (if-not (submodule-updater.builder.env/submodule-added-to-dependency-cascade? options submodule-path)
                                (if (submodule-updater.builder.env/add-submodule-to-dependency-cascade? options submodule-path)
                                    (common-state/update-state! :git-handler :submodule-updater update :dependency-cascade vector/conj-item submodule-path))))
                 ; The 'build-dependency-cascade!' function calls itself recursively if the dependency cascade has changed during its last iteration.
                 ; If the cascade hasn't changed, there is no need to continue the recursion.
                 (if-not (= dependency-cascade (submodule-updater.builder.env/get-dependency-cascade options))
                         (build-dependency-cascade! options)
                         (if-not (submodule-updater.builder.env/dependency-cascade-built? options)
                                 (core.errors/error-catched (str "Cannot build dependency cascade!")
                                                            (str "Unresolved dependencies:")
                                                            (submodule-updater.builder.env/get-unresolved-dependencies options)))))))

(defn build-dependency-tree!
  ; @ignore
  ;
  ; @note
  ; The dependency tree is a dependency cascade extending submodule with their dependencies.
  ;
  ; @description
  ; Iterates over the dependency cascade adding each submodule and its dependencies to the dependency tree.
  ;
  ; @param (map) options
  [options]
  (doseq [submodule-path (submodule-updater.builder.env/get-dependency-cascade options)]
         (common-state/update-state! :git-handler :submodule-updater update :dependency-tree vector/conj-item [submodule-path []])
         (doseq [% (submodule-updater.builder.env/get-dependency-cascade options)]
                (let [repository-name (submodule-updater.detector.env/get-submodule-repository-name options %)]
                     (if (submodule-updater.detector.env/submodule-depends-on? options submodule-path repository-name)
                         (common-state/update-state! :git-handler :submodule-updater update :dependency-tree
                                                                                     vector/update-last-item
                                                                                     vector/update-last-item
                                                                                     vector/conj-item %))))))
