
(ns git-handler.api
    (:require [git-handler.core.env                            :as core.env]
              [git-handler.core.utils                          :as core.utils]
              [git-handler.gitignore.env                       :as gitignore.env]
              [git-handler.gitignore.side-effects              :as gitignore.side-effects]
              [git-handler.submodule-updater.core.side-effects :as core.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; git-handler.core.env
(def read-submodule-git-file          core.env/read-submodule-git-file)
(def get-submodule-git-directory-path core.env/get-submodule-git-directory-path)
(def read-submodule-config-file       core.env/read-submodule-config-file)
(def get-submodule-git-url            core.env/get-submodule-git-url)
(def read-submodule-head-file         core.env/read-submodule-head-file)
(def get-submodule-head-branch        core.env/get-submodule-head-branch)
(def submodule-branch-checked-out?    core.env/submodule-branch-checked-out?)
(def read-submodule-deps-edn          core.env/read-submodule-deps-edn)

; git-handler.core.utils
(def git-url->repository-name core.utils/git-url->repository-name)

; git-handler.gitignore.env
(def get-gitignore gitignore.env/get-gitignore)
(def ignored?      gitignore.env/ignored?)

; git-handler.gitignore.side-effects
(def ignore! gitignore.side-effects/ignore!)

; git-handler.submodule-updater.core.side-effects
(def update-submodule-dependencies! core.side-effects/update-submodule-dependencies!)
