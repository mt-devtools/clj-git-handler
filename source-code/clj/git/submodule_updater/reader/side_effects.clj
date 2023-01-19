
(ns git.submodule-updater.reader.side-effects
    (:require [git.submodule-updater.detector.state :as detector.state]
              [git.submodule-updater.reader.helpers :as reader.helpers]
              [git.submodule-updater.reader.state   :as reader.state]
              [io.api                               :as io]
              [vector.api                           :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- store-inner-dependency!
  ; @param (string) submodule-path
  ; @param (string) dependency-name
  ; @param (map) dependency-props
  ; {:sha (string)
  ;  :git/url (string)}
  [submodule-path dependency-name {:git/keys [url] :keys [sha] :as dependency-props}]
  (swap! reader.state/INNER-DEPENDENCIES update submodule-path vector/conj-item
         [dependency-name url sha]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- read-submodule!
  ; @param (string) submodule-path
  ; @param (map) submodule-props
  ; {:git-url (string)}
  [submodule-path {:keys [git-url]}]
  ; 1. Reads the submodule's deps.edn file
  ; 2. Iterates over the dependencies
  ; 3. If a previously detected submodule found in the dependency list, it will
  ;    be qualified as an inner dependency and will be stored in the INNER-DEPENDENCIES atom.
  (if-let [{:keys [deps]} (io/read-edn-file (str submodule-path "/deps.edn"))]
          (doseq [[dependency-name {:git/keys [url] :as dependency-props}] deps]
                 (if (reader.helpers/inner-dependency? url)
                     (store-inner-dependency! submodule-path dependency-name dependency-props)))))

(defn read-submodules!
  ; @param (map) options
  [_]
  ; Iterates over the detected submodules and passes the submodule paths and
  ; their detected properties to the read-submodule! function.
  (reset! reader.state/INNER-DEPENDENCIES nil)
  (doseq [[submodule-path submodule-props] @detector.state/DETECTED-SUBMODULES]
         (read-submodule! submodule-path submodule-props)))
