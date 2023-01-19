
(ns git.submodule-updater.core.side-effects
    (:require [git.submodule-updater.builder.side-effects  :as builder.side-effects]
              [git.submodule-updater.detector.side-effects :as detector.side-effects]
              [git.submodule-updater.reader.side-effects   :as reader.side-effects]
              [git.submodule-updater.updater.side-effects  :as updater.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-submodule-dependencies!
  ; @description
  ; Pushes the changed submodules and updates the other submodules' deps.edn
  ; files with the returned commit SHA.
  ;
  ; You can specify which folders in your project contains submodules,
  ; by default the function updates submodules in the "/submodules" folder.
  ;
  ; By passing the :default property you can use your own commit message generator
  ; function and change the default branch, by default the function uses the current
  ; timestamp as commit messages and the "main" branch to push commits.
  ; In addition you can specify these settings for each submodule by passing
  ; the :config property.
  ;
  ; @param (map)(opt) options
  ; {:config (map)(opt)
  ;   {"author/my-repository" {:commit-message-f (function)(opt)
  ;                            :branch (string)(opt)}}
  ;  :default (map)(opt)
  ;   {:commit-message-f (function)(opt)
  ;     Default time.api/timestamp-string}
  ;    :branch (string)(opt)
  ;     Default: "main"
  ;  :source-paths (vector)(opt)
  ;   Default: ["submodules"]}
  ;
  ; @usage
  ; (update-submodule-dependencies!)
  ;
  ; @usage
  ; (update-submodule-dependencies! {:source-paths ["my-submodules"]})
  ;
  ; @usage
  ; (defn my-commit-message-f [latest-commit-message] ...)
  ; (update-submodule-dependencies! {:default {:branch "my-branch"
  ;                                            :commit-message-f my-commit-message-f}})
  ;
  ; @usage
  ; (defn my-commit-message-f [latest-commit-message] ...)
  ; (update-submodule-dependencies! {:config {"author/my-repository" {:branch "my-branch"
  ;                                                                   :commit-message-f my-commit-message-f}}})
  ([]
   (update-submodule-dependencies! {}))

  ([options]
   (try (do (detector.side-effects/detect-submodules!    options)
            (reader.side-effects/read-submodules!        options)
            (builder.side-effects/build-dependency-tree! options)
            (updater.side-effects/update-submodules!     options))
        (catch Exception e nil))))
