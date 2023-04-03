
(ns git-handler.submodule-updater.detector.env
    (:require [git-handler.submodule-updater.detector.state :as detector.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-path->git-url
  ; @ignore
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (submodule-path->git-url "submodules/my-repository")
  ;
  ; @return (string)
  [submodule-path]
  (get-in @detector.state/DETECTED-SUBMODULES [submodule-path :git-url]))
