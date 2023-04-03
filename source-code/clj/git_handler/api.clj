
(ns git-handler.api
    (:require [git-handler.gitignore.env                       :as gitignore.env]
              [git-handler.gitignore.side-effects              :as gitignore.side-effects]
              [git-handler.submodule-updater.core.side-effects :as core.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; git-handler.gitignore.env
(def get-gitignore gitignore.env/get-gitignore)
(def ignored?      gitignore.env/ignored?)

; git-handler.gitignore.side-effects
(def ignore! gitignore.side-effects/ignore!)

; git-handler.submodule-updater.core.side-effects
(def update-submodule-dependencies! core.side-effects/update-submodule-dependencies!)
