
(ns git-handler.api
    (:require [git-handler.core.env                            :as core.env]
              [git-handler.submodules.env                            :as submodules.env]
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

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial How to check whether a pattern is added to the '.gitignore' file?
;
; The [ignored?](#ignored_) function checks whether the given pattern is added to the '.gitignore' file.
;
; @usage
; (ignored? "my-file.txt")

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial How to update submodule dependencies?
;
; The [update-submodule-dependencies!](#update-submodule-dependencies_) function detects git submodules within the specified folders,
; and builds a dependency tree of the found submodules and their relations to each other (using their 'deps.edn' file to figure out relations).
;
; When the dependency tree is built, the function iterates over the detected submodules
; and pushes their local changes to the specified branches. After successful pushings,
; it takes the returned commit SHAs and updates the 'deps.edn' file in other submodules
; (if they depend on the pushed submodule).
;
; With default options, the function detects submodules in the 'submodules' folder,
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

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (git-handler.core.env/*)
(def get-local-commit-history         core.env/get-local-commit-history)
(def get-last-local-commit-message    core.env/get-last-local-commit-message)
(def get-last-local-commit-sha        core.env/get-last-local-commit-sha)
(def get-head-branch-name             core.env/get-head-branch-name)
(def branch-checked-out?              core.env/branch-checked-out?)
(def local-branch-has-changes?        core.env/local-branch-has-changes?)
(def local-branch-has-cached-changes? core.env/local-branch-has-cached-changes?)
(def head-branch-has-changes?         core.env/head-branch-has-changes?)
(def head-branch-has-cached-changes?  core.env/head-branch-has-cached-changes?)

; @redirect (git-handler.core.side-effects/*)
(def rebase-to-branch!      core.side-effects/rebase-to-branch!)
(def checkout-to-branch!    core.side-effects/checkout-to-branch!)
(def cache-local-changes!   core.side-effects/cache-local-changes!)
(def commit-cached-changes! core.side-effects/commit-cached-changes!)
(def push-local-commits!    core.side-effects/push-local-commits!)

; @redirect (git-handler.core.utils/*)
(def git-url->repository-name core.utils/git-url->repository-name)

; @redirect (git-handler.gitignore.env/*)
(def get-gitignore gitignore.env/get-gitignore)
(def ignored?      gitignore.env/ignored?)

; @redirect (git-handler.gitignore.side-effects/*)
(def ignore! gitignore.side-effects/ignore!)

; @redirect (git-handler.submodule-updater.core.side-effects/*)
(def update-submodule-dependencies! submodule-updater.core.side-effects/update-submodule-dependencies!)

; @redirect (git-handler.submodules.env/*)
(def read-submodule-git-file          submodules.env/read-submodule-git-file)
(def get-submodule-git-directory-path submodules.env/get-submodule-git-directory-path)
(def submodule-path?                  submodules.env/submodule-path?)
(def get-submodule-paths              submodules.env/get-submodule-paths)
(def read-submodule-config-file       submodules.env/read-submodule-config-file)
(def get-submodule-git-url            submodules.env/get-submodule-git-url)
(def read-submodule-head-file         submodules.env/read-submodule-head-file)
