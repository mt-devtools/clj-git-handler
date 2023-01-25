
(ns git.api
    (:require [git.gitignore.env                       :as gitignore.env]
              [git.gitignore.side-effects              :as gitignore.side-effects]
              [git.submodule-updater.core.side-effects :as core.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; git.gitignore.env
(def get-gitignore gitignore.env/get-gitignore)
(def ignored?      gitignore.env/ignored?)

; git.gitignore.side-effects
(def ignore! gitignore.side-effects/ignore!)

; gi.submodule-updater.core.side-effects
(def update-submodule-dependencies! core.side-effects/update-submodule-dependencies!)
