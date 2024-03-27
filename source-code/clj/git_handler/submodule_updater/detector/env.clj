
(ns git-handler.submodule-updater.detector.env
    (:require [git-handler.core.utils :as core.utils]
              [common-state.api :as common-state]
              [fruits.map.api :as map]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-detected-submodules
  ; @ignore
  ;
  ; @description
  ; Returns the detected submodules.
  ;
  ; @param (map) options
  ;
  ; @return (map)
  [_]
  (common-state/get-state :git-handler :submodule-updater :detected-submodules))

(defn get-detected-dependencies
  ; @ignore
  ;
  ; @description
  ; Returns the detected dependencies.
  ;
  ; @param (map) options
  ;
  ; @return (map)
  [_]
  (common-state/get-state :git-handler :submodule-updater :detected-dependencies))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-source-path-detected-submodules
  ; @ignore
  ;
  ; @description
  ; Returns the detected submodules of a specific source path.
  ;
  ; @param (map) options
  ; @param (string) source-path
  ;
  ; @return (map)
  [options source-path]
  (letfn [(f0 [%] (-> % :source-path (= source-path)))]
         (-> (get-detected-submodules options)
             (map/keep-values-by f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn git-url-detected?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given Git URL corresponds to a detected submodule.
  ;
  ; @param (map) options
  ; @param (string) git-url
  ;
  ; @return (boolean)
  [options git-url]
  (letfn [(f0 [%] (= (core.utils/git-url->repository-name (:git-url %))
                     (core.utils/git-url->repository-name   git-url)))]
         (-> (get-detected-submodules options)
             (map/any-value-matches? f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-submodule-of-git-url
  ; @ignore
  ;
  ; @description
  ; Returns which detected submodule corresponds to the given Git URL.
  ;
  ; @param (map) options
  ; @param (string) git-url
  ;
  ; @return (string)
  [options git-url]
  (letfn [(f0 [submodule-path submodule-props]
              (= (core.utils/git-url->repository-name   git-url)
                 (core.utils/git-url->repository-name (:git-url submodule-props))))]
         (-> (get-detected-submodules options)
             (map/first-matching-key f0 {:provide-value? true}))))

(defn get-submodule-dependencies
  ; @ignore
  ;
  ; @description
  ; Returns the detected dependencies of of a specific submodule.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ;
  ; @return (vectors in vector)
  [options submodule-path]
  (-> (get-detected-dependencies options)
      (get submodule-path)))

(defn get-submodule-git-url
  ; @ignore
  ;
  ; @description
  ; Returns the Git URL of a specific detected submodule.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ;
  ; @return (string)
  [_ submodule-path]
  (common-state/get-state :git-handler :submodule-updater :detected-submodules submodule-path :git-url))

(defn get-submodule-repository-name
  ; @ignore
  ;
  ; @description
  ; Returns the repository name of a specific detected submodule.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ;
  ; @return (string)
  [_ submodule-path]
  (common-state/get-state :git-handler :submodule-updater :detected-submodules submodule-path :repository-name))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-depends-on?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if a specific submodule depends on the given repository.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) repository-name
  ;
  ; @return (boolean)
  [options submodule-path repository-name]
  ; The values must be converted into strings. Otherwise, somehow they are always different!
  (letfn [(f0 [[% _ _]]
              (println (type %)
                       (type repository-name))
              (= (str %)
                 (str repository-name)))]
         (-> (get-submodule-dependencies options submodule-path)
             (vector/any-item-matches? f0))))
