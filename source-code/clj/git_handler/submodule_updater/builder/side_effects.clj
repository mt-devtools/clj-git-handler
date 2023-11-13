
(ns git-handler.submodule-updater.builder.side-effects
    (:require [git-handler.core.errors                      :as core.errors]
              [git-handler.submodule-updater.builder.env    :as submodule-updater.builder.env]
              [git-handler.submodule-updater.builder.state  :as submodule-updater.builder.state]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [vector.api                                   :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn build-dependency-tree!
  ; @ignore
  ;
  ; @description
  ; Iterates over the detected submodules and adds their paths to the dependency tree
  ; if they have INNER dependencies.
  ;
  ; @param (map) options
  ; @param (integer)(opt) kill-switch
  ; In case the function couldn't resolve a dependency it could easily go to an infinite loop
  ; and the 'kill-switch' parameter's job is to stop that runaway recursion.
  [options & [kill-switch]]

  ; When this function is called the first time, it clears the 'DEPENDENCY-TREE' atom
  ; and it uses the 'kill-switch' to determine whether the function is applied for the first time.
  (if-not kill-switch (reset! submodule-updater.builder.state/DEPENDENCY-TREE nil))

  ; Builds a dependency tree of inner dependencies in the host project of the submodules
  ; that are detected in the provided source directories.
  (if (< (or kill-switch 0) 256) ; <- Stops a runaway recursion
      (if-not (submodule-updater.builder.env/dependency-tree-built?)
              (do (doseq [[submodule-path _] @submodule-updater.detector.state/DETECTED-SUBMODULES]
                         (if-not (submodule-updater.builder.env/submodule-added-to-dependency-tree? submodule-path)
                                 (if (submodule-updater.builder.env/submodule-non-depend? submodule-path)
                                     (swap! submodule-updater.builder.state/DEPENDENCY-TREE vector/conj-item [submodule-path]))))

                  ; Calls itself recursively ...
                  (build-dependency-tree! options (inc (or kill-switch 0)))))
      (core.errors/error-catched (str "Building dependency tree stopped by kill switch, error while recursing!")
                                 (str "Unresolved dependencies:")
                                 (submodule-updater.builder.env/get-unresolved-dependencies))))
