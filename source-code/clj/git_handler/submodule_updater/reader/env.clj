
(ns git-handler.submodule-updater.reader.env
    (:require [git-handler.core.utils                       :as core.utils]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [git-handler.submodule-updater.reader.state   :as submodule-updater.reader.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn inner-dependency?
  ; @ignore
  ;
  ; @description
  ; - Takes a git URL and iterates over the previously detected submodules.
  ; - If one of the other detected submodules has the same URL as its dependency it qualifies
  ;   the the given git URL's corresponding submodule as an inner dependency and returns TRUE.
  ;
  ; @param (string) git-url
  ;
  ; @return (boolean)
  [git-url]
  (letfn [(f0 [[_ %]] (= (core.utils/git-url->repository-name (:git-url %))
                         (core.utils/git-url->repository-name   git-url)))]
         (some f0 @submodule-updater.detector.state/DETECTED-SUBMODULES)))

(defn get-submodule-inner-dependencies
  ; @ignore
  ;
  ; @param (string) submodule-path
  ;
  ; @return (boolean)
  [submodule-path]
  (get @submodule-updater.reader.state/INNER-DEPENDENCIES submodule-path))

(defn depends-on?
  ; @ignore
  ;
  ; @description
  ; Checks whether the given submodule-path depends on the given repository-name.
  ;
  ; @param (string) submodule-path
  ; @param (string) repository-name
  ;
  ; @usage
  ; (depends-on? "my-submodules/my-repository" "author/another-repository")
  ;
  ; @return (boolean)
  [submodule-path repository-name]
  ; The values have to be converted to strings. Otherwise, somehow they are always different!
  (let [dependencies (get @submodule-updater.reader.state/INNER-DEPENDENCIES submodule-path)]
       (letfn [(f0 [[% _ _]] (= (str %)
                                (str repository-name)))]
              (some f0 dependencies))))
