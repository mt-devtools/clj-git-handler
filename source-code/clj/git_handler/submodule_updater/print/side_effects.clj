
(ns git-handler.submodule-updater.print.side-effects
    (:require [common-state.api :as common-state]
              [git-handler.submodule-updater.builder.env :as submodule-updater.builder.env]
              [git-handler.submodule-updater.detector.env :as submodule-updater.detector.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-dependency-tree!
  ; @ignore
  ;
  ; @description
  ; Prints the dependency tree to the console.
  ;
  ; @param (map) options
  [options]
  (println)
  (doseq [[submodule-path dependencies] (submodule-updater.builder.env/get-dependency-tree options)]
         (println (str "The following submodule dependencies are found for submodule '" submodule-path "':"))
         (doseq [% dependencies] (println % " - " (submodule-updater.detector.env/get-submodule-git-url options %)))
         (println)))
