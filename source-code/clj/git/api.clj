
(ns git.api
    (:require [git.gitignore.helpers                   :as gitignore.helpers]
              [git.gitignore.side-effects              :as gitignore.side-effects]
              [git.submodule-updater.core.side-effects :as core.side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; git.gitignore.helpers
(def get-gitignore gitignore.helpers/get-gitignore)
(def ignored?      gitignore.helpers/ignored?)

; git.gitignore.side-effects
(def ignore! gitignore.side-effects/ignore!)

; gi.submodule-updater.core.side-effects
(def update-submodule-dependencies! core.side-effects/update-submodule-dependencies!)
