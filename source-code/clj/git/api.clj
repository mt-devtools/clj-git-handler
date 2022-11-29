
(ns git.api
    (:require [git.side-effects :as side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; git.side-effects
(def get-gitignore side-effects/get-gitignore)
(def ignored?      side-effects/ignored?)
(def ignore!       side-effects/ignore!)
