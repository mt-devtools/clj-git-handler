
(ns git-handler.submodule-updater.reader.env
    (:require [git-handler.submodule-updater.core.utils     :as core.utils]
              [git-handler.submodule-updater.detector.state :as detector.state]
              [git-handler.submodule-updater.reader.state   :as reader.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn inner-dependency?
  ; @ignore
  ;
  ; @param (string) git-url
  ;
  ; @return (boolean)
  [git-url]
  ; Takes a git URL and iterates over the previously detected submodules.
  ; If one of the detected submodules has the same URL qualifies it as an inner
  ; dependency.
  (letfn [(f [[_ %]] (= (core.utils/git-url->repository-name (:git-url %))
                        (core.utils/git-url->repository-name   git-url)))]
         (some f @detector.state/DETECTED-SUBMODULES)))

(defn get-submodule-inner-dependencies
  ; @ignore
  ;
  ; @param (string) submodule-path
  ;
  ; @return (boolean)
  [submodule-path]
  (get @reader.state/INNER-DEPENDENCIES submodule-path))

(defn depends-on?
  ; @ignore
  ;
  ; @param (string) submodule-path
  ; @param (string) repository-name
  ;
  ; @usage
  ; (depends-on? "submodules/my-repository" "author/another-repository")
  ;
  ; @return (boolean)
  [submodule-path repository-name]
  ; Checks whether the given submodule-path depends on the given repository-name.
  ;
  ; Somehow the values have to be converted to strings, otherwise they are always different!
  (let [dependencies (get @reader.state/INNER-DEPENDENCIES submodule-path)]
       (letfn [(f [[% _ _]] (= (str %)
                               (str repository-name)))]
              (some f dependencies))))
