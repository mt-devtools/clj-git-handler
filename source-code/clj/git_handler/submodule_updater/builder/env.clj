
(ns git-handler.submodule-updater.builder.env
    (:require [fruits.map.api :as map]
              [fruits.vector.api :as vector]
              [git-handler.submodule-updater.detector.env :as submodule-updater.detector.env]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-dependency-cascade
  ; @ignore
  ;
  ; @description
  ; Returns the dependency cascade.
  ;
  ; @param (map) options
  ;
  ; @return (strings in vector)
  [_]
  (common-state/get-state :git-handler :submodule-updater :dependency-cascade))

(defn get-dependency-tree
  ; @ignore
  ;
  ; @description
  ; Returns the dependency tree.
  ;
  ; @param (map) options
  ;
  ; @return (strings in vector)
  [_]
  (common-state/get-state :git-handler :submodule-updater :dependency-tree))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-added-to-dependency-cascade?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if a specific submodule is added to the dependency cascade.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ;
  ; @return (boolean)
  [options submodule-path]
  (letfn [(f0 [%] (= % submodule-path))]
         (-> (get-dependency-cascade options)
             (vector/any-item-matches? f0))))

(defn dependency-cascade-built?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if all detected submodules are added to the dependency cascade.
  ;
  ; @param (map) options
  ;
  ; @return (boolean)
  [options]
  (letfn [(f0 [%] (submodule-added-to-dependency-cascade? options %))]
         (-> (submodule-updater.detector.env/get-detected-submodules options)
             (map/all-keys-match? f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn add-submodule-to-dependency-cascade?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if all detected dependencies of a specific submodule are added to the dependency cascade.
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ;
  ; @return (boolean)
  [options submodule-path]
  (letfn [(f0 [[_ git-url _]] (->> (submodule-updater.detector.env/get-submodule-of-git-url options git-url)
                                   (submodule-added-to-dependency-cascade? options)))]
         (if-let [dependencies (submodule-updater.detector.env/get-submodule-dependencies options submodule-path)]
                 (-> dependencies (vector/all-items-match? f0))
                 :submodule-has-no-detected-dependencies)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-unresolved-dependencies
  ; @ignore
  ;
  ; @description
  ; Returns the submodules that are not added to the dependency cascade.
  ;
  ; @param (map) options
  ;
  ; @return (strings in vector)
  [options]
  (letfn [(f0 [submodule-path] (submodule-added-to-dependency-cascade? options submodule-path))]
         (-> (submodule-updater.detector.env/get-detected-submodules options)
             (map/remove-keys-by f0)
             (map/keys))))
