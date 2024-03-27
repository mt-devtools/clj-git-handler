
(ns git-handler.submodule-updater.builder.side-effects
    (:require [fruits.vector.api                            :as vector]
              [git-handler.core.errors                      :as core.errors]
              [git-handler.submodule-updater.builder.env    :as submodule-updater.builder.env]
              [git-handler.submodule-updater.builder.state  :as submodule-updater.builder.state]
              [git-handler.submodule-updater.reader.env :as submodule-updater.reader.env]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn build-dependency-cascade!
  ; @ignore
  ;
  ; @description
  ; Iterates over the detected submodules adding their paths to the dependency cascade (if they have INNER dependencies).
  ;
  ; @param (map) options
  ; @param (integer)(opt) kill-switch
  ; In case the function couldn't resolve a dependency it could easily go to an infinite loop
  ; and the 'kill-switch' parameter's job is to stop that runaway recursion.
  [options & [kill-switch]]

  ; When this function is called the first time, it clears the 'DEPENDENCY-CASCADE' atom
  ; and it uses the 'kill-switch' to determine whether the function is applied for the first time.
  (if-not kill-switch (reset! submodule-updater.builder.state/DEPENDENCY-CASCADE nil))

  ; Builds a dependency cascade of inner dependencies within the host project of the submodules
  ; that are detected in the provided source directories.
  (if (< (or kill-switch 0) 256) ; <- Stops a runaway recursion
      (when-not (submodule-updater.builder.env/dependency-cascade-built?)
                (doseq [[submodule-path _] @submodule-updater.detector.state/DETECTED-SUBMODULES]
                       (if-not (submodule-updater.builder.env/submodule-added-to-dependency-cascade? submodule-path)
                               (if (submodule-updater.builder.env/submodule-non-depend? submodule-path)
                                   (swap! submodule-updater.builder.state/DEPENDENCY-CASCADE vector/conj-item [submodule-path]))))

                ; Calls itself recursively ...
                (build-dependency-cascade! options (inc (or kill-switch 0))))
      (core.errors/error-catched (str "Building dependency cascade has been stopped by kill switch, maximum call stack size exceeded!")
                                 (str "Unresolved dependencies:")
                                 (submodule-updater.builder.env/get-unresolved-dependencies))))

(defn build-dependency-tree!
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (map) options
  [options]
  (doseq [[submodule-path] @submodule-updater.builder.state/DEPENDENCY-CASCADE]
         (common-state/update-state! :git-handler :submodule-updater vector/conj-item [submodule-path []])
         (let [repository-name (get-in @submodule-updater.detector.state/DETECTED-SUBMODULES [submodule-path :repository-name])]
              (doseq [[% _] @submodule-updater.detector.state/DETECTED-SUBMODULES]
                     (if (submodule-updater.reader.env/depends-on? % repository-name)
                         (common-state/update-state! :git-handler :submodule-updater vector/update-last-item
                                                                                     vector/update-last-item
                                                                                     vector/conj-item %))))))
