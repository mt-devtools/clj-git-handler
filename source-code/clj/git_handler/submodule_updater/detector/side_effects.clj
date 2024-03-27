
(ns git-handler.submodule-updater.detector.side-effects
    (:require [git-handler.core.utils                       :as core.utils]
              [git-handler.core.errors                       :as core.errors]
              [git-handler.submodule-updater.detector.env :as submodule-updater.detector.env]
              [git-handler.submodules.env                   :as submodules.env]
              [io.api                                       :as io]
              [common-state.api :as common-state]
              [deps-edn-handler.api :as deps-edn-handler]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn map-source-path!
  ; @ignore
  ;
  ; @description
  ; Maps the given source path detecting submodules within.
  ;
  ; @param (map) options
  ; @param (string) source-path
  [_ source-path]
  (doseq [git-filepath (io/search-files source-path #"/.git$")]
         (let [submodule-path (io/filepath->parent-path git-filepath)]
              (if-let [git-url (submodules.env/get-submodule-git-url submodule-path)]
                      (if-let [repository-name (core.utils/git-url->repository-name git-url)]
                              (common-state/assoc-state! :git-handler :submodule-updater :detected-submodules submodule-path {:git-url git-url :repository-name repository-name :source-path source-path})
                              (core.errors/error-catched (str "Unable to derive repository name from Git URL: " git-url)))
                      (core.errors/error-catched (str "Unable to get remote origin Git URL of submodule: " submodule-path))))))

(defn detect-submodules!
  ; @ignore
  ;
  ; @description
  ; Iterates over the given source paths detecting submodules within.
  ;
  ; @param (map) options
  ; {:source-paths (strings in vector)(opt)
  ;   Default: ["submodules"]
  ;  ...}
  [{:keys [source-paths] :or {source-paths ["submodules"]} :as options}]
  (doseq [source-path source-paths]
         (map-source-path! options source-path)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn detect-submodule-dependencies!
  ; @ignore
  ;
  ; @description
  ; Iterates over the detected submodules searching for dependencies within their 'deps.edn' file that correspond to another detected submodule.
  ;
  ; @param (map) options
  ; {:source-paths (strings in vector)(opt)
  ;   Default: ["submodules"]
  ;  ...}
  [options]
  (doseq [[submodule-path _] (common-state/get-state :git-handler :submodule-updater :detected-submodules)]
         (println (str "Reading 'deps.edn' file of submodule: '" submodule-path "' ..."))
         (if-let [{:keys [deps]} (deps-edn-handler/read-deps-edn submodule-path)]
                 (doseq [[repository-name {:git/keys [url] :keys [sha]}] deps]
                        (if (submodule-updater.detector.env/git-url-detected? options url)
                            (common-state/update-state! :git-handler :submodule-updater update-in [:detected-dependencies submodule-path]
                                                                                        vector/conj-item [repository-name url sha])))
                 (core.errors/error-catched (str "Unable to read 'deps.edn' file of submodule: " submodule-path)))))
