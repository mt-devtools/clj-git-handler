
(ns git-handler.submodule-updater.detector.env
    (:require [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-path->git-url
  ; @ignore
  ;
  ; @description
  ; Returns the submodule' git URL that is found by the 'detect-submodule!' function
  ; in the config file of the submodule in the host project's '.git' directory
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (submodule-path->git-url "submodules/my-repository")
  ;
  ; @return (string)
  [submodule-path]
  (get-in @submodule-updater.detector.state/DETECTED-SUBMODULES [submodule-path :git-url]))
