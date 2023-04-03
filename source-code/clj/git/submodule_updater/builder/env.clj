
(ns git.submodule-updater.builder.env
    (:require [git.submodule-updater.builder.state  :as builder.state]
              [git.submodule-updater.core.env       :as core.env]
              [git.submodule-updater.detector.state :as detector.state]
              [git.submodule-updater.reader.state   :as reader.state]
              [noop.api                             :refer [return]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-added-to-dependency-tree?
  ; @ignore
  ;
  ; @param (string) submodule-path
  ;
  ; @return (boolean)
  [submodule-path]
  (letfn [(f [[% _]] (= % submodule-path))]
         (some f @builder.state/DEPENDENCY-TREE)))

(defn dependency-tree-built?
  ; @ignore
  ;
  ; @return (boolean)
  []
  ; Checks whether the dependency tree is complete or some submodule missing yet
  (letfn [(f [[submodule-path _]]
             (submodule-added-to-dependency-tree? submodule-path))]
         (every? f @detector.state/DETECTED-SUBMODULES)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-has-inner-dependencies?
  ; @ignore
  ;
  ; @param (string) submodule-path
  ;
  ; @return (boolean)
  [submodule-path]
  ; Some submodule does not depend on other INNER submodules
  (if-let [dependencies (get @reader.state/INNER-DEPENDENCIES submodule-path)]
          (and (-> dependencies vector?)
               (-> dependencies empty? not))))

(defn submodule-non-depend?
  ; @ignore
  ;
  ; @param (string) submodule-path
  ;
  ; @return (boolean)
  [submodule-path]
  ; A submodule can be non-depend if it has no known INNER dependencies,
  ; or all of its inner dependencies are already added to the dependency tree.
  (if-let [dependencies (get @reader.state/INNER-DEPENDENCIES submodule-path)]
          (letfn [(f [[dep-name url sha]]
                     (-> url core.env/git-url->submodule-path submodule-added-to-dependency-tree?))]
                 (every? f dependencies))
          (return :submodule-has-no-inner-dependencies)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-unresolved-dependencies
  ; @ignore
  ;
  ; @return (strings in vector)
  []
  (letfn [(f [result [submodule-path _]]
             (if (or (submodule-added-to-dependency-tree? submodule-path)
                     (submodule-non-depend?               submodule-path))
                 (return result)
                 (conj   result submodule-path)))]
         (reduce f [] @detector.state/DETECTED-SUBMODULES)))
