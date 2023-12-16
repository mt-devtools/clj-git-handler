
(ns git-handler.api
    (:require [git-handler.core.env                            :as core.env]
              [git-handler.core.side-effects                   :as core.side-effects]
              [git-handler.core.utils                          :as core.utils]
              [git-handler.gitignore.env                       :as gitignore.env]
              [git-handler.gitignore.side-effects              :as gitignore.side-effects]
              [git-handler.submodule-updater.core.side-effects :as submodule-updater.core.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (git-handler.core.env)
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

; @redirect (git-handler.core.side-effects)
(def cache-submodule-local-changes! core.side-effects/cache-submodule-local-changes!)
(def push-submodule-cached-changes! core.side-effects/push-submodule-cached-changes!)

; @redirect (git-handler.core.utils)
(def git-url->repository-name core.utils/git-url->repository-name)

; @redirect (git-handler.gitignore.env)
(def get-gitignore gitignore.env/get-gitignore)
(def ignored?      gitignore.env/ignored?)

; @redirect (git-handler.gitignore.side-effects)
(def ignore! gitignore.side-effects/ignore!)

; @redirect (git-handler.submodule-updater.core.side-effects)
(def update-submodule-dependencies! submodule-updater.core.side-effects/update-submodule-dependencies!)
