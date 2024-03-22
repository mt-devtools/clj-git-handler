
(ns git-handler.submodule-updater.reader.side-effects
    (:require [deps-edn-handler.api                         :as deps-edn-handler]
              [fruits.vector.api                            :as vector]
              [git-handler.core.env                         :as core.env]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [git-handler.submodule-updater.reader.env     :as submodule-updater.reader.env]
              [git-handler.submodule-updater.reader.state   :as submodule-updater.reader.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- store-inner-dependency!
  ; @ignore
  ;
  ; @param (string) submodule-path
  ; @param (string) dependency-name
  ; @param (map) dependency-props
  ; {:sha (string)
  ;  :git/url (string)}
  [submodule-path dependency-name {:git/keys [url] :keys [sha] :as dependency-props}]
  (swap! submodule-updater.reader.state/INNER-DEPENDENCIES update submodule-path vector/conj-item
         [dependency-name url sha]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- read-submodule!
  ; @ignore
  ;
  ; @description
  ; 1. Reads the submodule's 'deps.edn' file
  ; 2. Iterates over the dependencies that are found in the 'deps.edn' file
  ; 3. If a previously detected other submodule is represented in the 'deps.edn' file as a dependency,
  ;    that represented submodule will be declared as an inner dependency of this submodule and will be
  ;    stored in the 'INNER-DEPENDENCIES' atom.
  ;
  ; @param (string) submodule-path
  ; @param (map) submodule-props
  ; {:git-url (string)}
  [submodule-path {:keys [git-url]}]
  (println (str "Reading 'deps.edn' file of submodule: '" submodule-path "' ..."))
  (if-let [{:keys [deps]} (deps-edn-handler/read-deps-edn submodule-path)]
          (doseq [[dependency-name {:git/keys [url] :as dependency-props}] deps]
                 (if (submodule-updater.reader.env/inner-dependency? url)
                     (store-inner-dependency! submodule-path dependency-name dependency-props)))))

(defn read-submodules!
  ; @ignore
  ;
  ; @description
  ; Iterates over the detected submodules and passes each submodule's path and
  ; their detected properties to the 'read-submodule!' function.
  ;
  ; @param (map) options
  [_]
  (reset! submodule-updater.reader.state/INNER-DEPENDENCIES nil)
  (doseq [[submodule-path submodule-props] @submodule-updater.detector.state/DETECTED-SUBMODULES]
         (read-submodule! submodule-path submodule-props)))
