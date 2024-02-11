
(ns git-handler.api
    (:require [git-handler.core.env                            :as core.env]
              [git-handler.core.side-effects                   :as core.side-effects]
              [git-handler.core.utils                          :as core.utils]
              [git-handler.gitignore.env                       :as gitignore.env]
              [git-handler.gitignore.side-effects              :as gitignore.side-effects]
              [git-handler.submodule-updater.core.side-effects :as submodule-updater.core.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial How to add a pattern to the '.gitignore' file?
;
; The [ignore!](#ignore_) function appends the given pattern to the '.gitignore' file, and returns the updated file content.
;
; @usage
; (ignore! "my-file.txt")



; @tutorial How to check whether a pattern is added to the '.gitignore' file?
;
; The [ignored?](#ignored_) function checks whether the given pattern is added to the '.gitignore' file.
;
; @usage
; (ignored? "my-file.txt")



; @tutorial How to update submodule dependencies?
;
; @important
; The 'update-submodule-dependencies!' function operates only in Clojure projects that use 'deps.edn' file to manage dependencies!
;
; The [update-submodule-dependencies!](#update-submodule-dependencies_) function detects git submodules within the specified folders,
; and builds a dependency tree of the found submodules and their relations to each other (using their 'deps.edn' file to figure out relations).
;
; When the dependency tree is built, the function iterates over the detected submodules
; and pushes their local changes to the specified branches. After successful pushings,
; it takes the returned commit SHAs and updates the 'deps.edn' file in other submodules
; (if they depend on the pushed submodule).
;
; With default options, this function detects submodules in the 'submodules' folder,
; pushes changes to 'main' branches and uses timestamps as commit messages.
;
; @usage
; (update-submodule-dependencies!)
;
; @usage
; (update-submodule-dependencies! {:source-paths ["my-submodules"]})
;
; @usage
; (defn my-commit-message-f [previous-commit-message] ...)
; (update-submodule-dependencies! {:config {:default {:commit-message-f my-commit-message-f
;                                                     :target-branch    "my-branch"}}})
;
; @usage
; (defn my-commit-message-f [latest-commit-message] ...)
; (update-submodule-dependencies! {:config {"author/my-repository" {:commit-message-f my-commit-message-f
;                                                                   :target-branch    "my-branch"}}})
;
; This function updates dependencies (in 'deps.edn' files) that are referenced in the following format:
;
; @code
; {:deps {author/my-repository {:git/url "..." :sha "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}}

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (git-handler.core.env/*)
(def read-submodule-git-file                 core.env/read-submodule-git-file)
(def get-submodule-git-directory-path        core.env/get-submodule-git-directory-path)
(def submodule-path?                         core.env/submodule-path?)
(def get-submodule-paths                     core.env/get-submodule-paths)
(def read-submodule-config-file              core.env/read-submodule-config-file)
(def get-submodule-git-url                   core.env/get-submodule-git-url)
(def read-submodule-head-file                core.env/read-submodule-head-file)
(def get-submodule-head-branch               core.env/get-submodule-head-branch)
(def submodule-branch-checked-out?           core.env/submodule-branch-checked-out?)
(def get-submodule-local-commit-history      core.env/get-submodule-local-commit-history)
(def get-submodule-last-local-commit-message core.env/get-submodule-last-local-commit-message)
(def get-submodule-last-local-commit-sha     core.env/get-submodule-last-local-commit-sha)
(def submodule-local-branch-changed?         core.env/submodule-local-branch-changed?)
(def submodule-head-branch-changed?          core.env/submodule-head-branch-changed?)

; @redirect (git-handler.core.side-effects/*)
(def cache-submodule-local-changes! core.side-effects/cache-submodule-local-changes!)
(def push-submodule-cached-changes! core.side-effects/push-submodule-cached-changes!)

; @redirect (git-handler.core.utils/*)
(def git-url->repository-name core.utils/git-url->repository-name)

; @redirect (git-handler.gitignore.env/*)
(def get-gitignore gitignore.env/get-gitignore)
(def ignored?      gitignore.env/ignored?)

; @redirect (git-handler.gitignore.side-effects/*)
(def ignore! gitignore.side-effects/ignore!)

; @redirect (git-handler.submodule-updater.core.side-effects/*)
(def update-submodule-dependencies! submodule-updater.core.side-effects/update-submodule-dependencies!)
