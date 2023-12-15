
(ns git-handler.submodule-updater.core.side-effects
    (:require [git-handler.submodule-updater.builder.side-effects  :as submodule-updater.builder.side-effects]
              [git-handler.submodule-updater.detector.side-effects :as submodule-updater.detector.side-effects]
              [git-handler.submodule-updater.reader.side-effects   :as submodule-updater.reader.side-effects]
              [git-handler.submodule-updater.updater.side-effects  :as submodule-updater.updater.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-submodule-dependencies!
  ; @ignore
  ;
  ; @description
  ; - Pushes the changed submodules' commits and updates the other submodules'
  ;   'deps.edn' files with the returned commit SHA.
  ; - You can specify which folders in your project contains submodules,
  ;   by default the function updates submodules in the '/submodules' folder.
  ; - By using the ':default' property you can use your own commit message generator
  ;   function and change the default target branch
  ; - By default the function uses an actual timestamp as commit messages and the "main" branch to push commits.
  ; - In addition you can specify these settings for each submodules separetely with using the ':config' property.
  ; - This function updates dependencies in 'deps.edn' files that are referenced as in the following format:
  ;   {:deps {author/repository-name {:git/url "https://github.com/author/repository-name"
  ;                                   :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}
  ;
  ; @param (map)(opt) options
  ; {:config (map)(opt)
  ;   {"author/my-repository" (map)(opt)
  ;     {:commit-message-f (function)(opt)
  ;      :on-pushed-f (function)(opt)
  ;      :target-branch (string)(opt)}}
  ;    :default (map)(opt)
  ;     {:commit-message-f (function)(opt)
  ;       Default time.api/timestamp-string
  ;      :on-pushed-f (function)(opt)
  ;      :target-branch (string)(opt)
  ;       Default: "main"}}
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
  ; (defn my-commit-message-f [last-commit-message] ...)
  ; (defn my-on-pushed-f      [submodule-path commit-message commit-sha] ...)
  ; (update-submodule-dependencies! {:config {:default {:commit-message-f my-commit-message-f
  ;                                                     :on-pushed-f      my-on-pushed-f
  ;                                                     :target-branch    "my-branch"}})
  ;
  ; @usage
  ; (defn my-commit-message-f [last-commit-message] ...)
  ; (defn my-on-pushed-f      [submodule-path commit-message commit-sha] ...)
  ; (update-submodule-dependencies! {:config {"author/my-repository" {:commit-message-f my-commit-message-f
  ;                                                                   :on-pushed-f      my-on-pushed-f
  ;                                                                   :target-branch    "my-branch"}}})
  ([]
   (update-submodule-dependencies! {}))

  ([options]
   (try (do (submodule-updater.detector.side-effects/detect-submodules!    options)
            (submodule-updater.reader.side-effects/read-submodules!        options)
            (submodule-updater.builder.side-effects/build-dependency-tree! options)
            (submodule-updater.updater.side-effects/update-submodules!     options))
        (catch Exception e nil))))
