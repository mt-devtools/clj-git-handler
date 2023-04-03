
(ns git.submodule-updater.builder.side-effects
    (:require [git.submodule-updater.builder.env    :as builder.env]
              [git.submodule-updater.core.env       :as core.env]
              [git.submodule-updater.builder.state  :as builder.state]
              [git.submodule-updater.detector.state :as detector.state]
              [vector.api                           :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn build-dependency-tree!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (integer)(opt) kill-switch
  [options & [kill-switch]]

  ; Uses the kill-switch to determines whether the function is applied first time.
  ; When it called first time, clears the DEPENDENCY-TREE atom.
  (if-not kill-switch (reset! builder.state/DEPENDENCY-TREE nil))

  ; Builds a dependency tree of inner dependencies in the host project
  ; detected in the specified source directories
  (if (< (or kill-switch 0) 48) ; <- Stops a runaway recursion
      (if-not (builder.env/dependency-tree-built?)
              (do (doseq [[submodule-path _] @detector.state/DETECTED-SUBMODULES]
                         (if-not (builder.env/submodule-added-to-dependency-tree? submodule-path)
                                 (if (builder.env/submodule-non-depend? submodule-path)
                                     (swap! builder.state/DEPENDENCY-TREE vector/conj-item [submodule-path]))))

                  ; Calls itself recursively ...
                  (build-dependency-tree! options (inc (or kill-switch 0)))))
      (core.env/error-catched "Building dependency tree stopped by kill switch, error while recursing!"
                              "Unresolved dependencies:" (builder.env/get-unresolved-dependencies))))
