
(ns git-handler.submodule-updater.core.env
    (:require [git-handler.submodule-updater.core.utils     :as core.utils]
              [git-handler.submodule-updater.detector.state :as detector.state]
              [io.api                                       :as io]
              [noop.api                                     :refer [return]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn error-catched
  ; @ignore
  ;
  ; @param (list of strings) error-message
  [& error-message]
  (println "ege")
  (doseq [line error-message]
         (println line))
  (throw :error-catched))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn git-url->submodule-path
  ; @ignore
  ;
  ; @description
  ; Iterates over the detected submodules and finds which submodule has the same
  ; 'git-url' stored in the SUBMODULES atom.
  ; If a submodule has the same 'git-url', returns with the matching submodule's path
  ; (found in the SUBMODULES atom).
  ;
  ; It can read four types of 'git-url' by using the 'git-url->repository-name' function.
  ;
  ; @param (string) git-url
  ;
  ; @usage
  ; (git-url->submodule-path "git@github.com:author/repository")
  ;
  ; @return (string)
  [git-url]
  (letfn [(f [[submodule-path submodule-props]]
             (if (= (core.utils/git-url->repository-name   git-url)
                    (core.utils/git-url->repository-name (:git-url submodule-props)))
                 (return submodule-path)))]
         (some f @detector.state/DETECTED-SUBMODULES)))
