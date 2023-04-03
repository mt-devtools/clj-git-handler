
(ns git-handler.submodule-updater.detector.side-effects
    (:require [git-handler.submodule-updater.core.utils     :as core.utils]
              [git-handler.submodule-updater.detector.state :as detector.state]
              [io.api                                       :as io]
              [string.api                                   :as string]
              [vector.api                                   :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- store-detected-submodule!
  ; @ignore
  ;
  ; @param (string) submodule-path
  ; @param (map) submodule-props
  [submodule-path submodule-props]
  (swap! detector.state/DETECTED-SUBMODULES assoc submodule-path submodule-props))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- detect-submodule!
  ; @ignore
  ;
  ; @param (string) subdirectory-path
  [subdirectory-path]
  ; Git submodules has a .git file instead of a .git directory.
  ; In this .git file there is a gitdir path which points relatively to
  ; the submodule's .git directory (placed nested in the project's .git directory).
  ;
  ; 1. Detects the .git file in the given subdirectory
  ; 2. Reads the gitdir value from the .git file
  ; 3. Detects the gitdir in the project's .git directory
  ; 4. Detects the config file in the gitdir
  ; 5. Reads the repository url from the config file
  ; 6. Stores the git URL and the repository name in the SUBMODULES atom
  (if-let [git (io/read-file (str subdirectory-path "/.git"))]
          (if-let [git-dir (-> git (string/after-first-occurence  "gitdir: " {:return? false})
                                   (string/before-first-occurence "\n"       {:return? false}))]
                  (if-let [config (io/read-file (str subdirectory-path"/"git-dir"/config"))]
                          (when-let [git-url (-> config (string/after-first-occurence  "[remote \"origin\"]" {:return? false})
                                                        (string/after-first-occurence  "url = "              {:return? false})
                                                        (string/before-first-occurence "\n"))]
                                  (if-let [repository-name (core.utils/git-url->repository-name git-url)]
                                          (store-detected-submodule! subdirectory-path {:git-url git-url :repository-name repository-name})))))))

(defn detect-submodules!
  ; @ignore
  ;
  ; @param (map) options
  ; {:source-paths (strings in vector)(opt)
  ;   Default: ["submodules"]}
  [{:keys [source-paths] :or {source-paths ["submodules"]}}]
  ; 1. Detects the subdirectories on the give paths.
  ; 2. Iterates over the subdirectory list and passes the found subdirectory paths
  ;    to the detect-submodule! function.

  (reset! detector.state/DETECTED-SUBMODULES nil)
  (letfn [(f [subdirectory-paths source-path] (vector/concat-items subdirectory-paths (io/subdirectory-list source-path)))]
         (let [subdirectory-paths (reduce f [] source-paths)]
              (doseq [subdirectory-path subdirectory-paths]
                     (detect-submodule! subdirectory-path)))))
