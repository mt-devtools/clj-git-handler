
(ns git-handler.submodule-updater.detector.side-effects
    (:require [fruits.string.api                            :as string]
              [fruits.vector.api                            :as vector]
              [git-handler.core.env                         :as core.env]
              [git-handler.core.utils                       :as core.utils]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [io.api                                       :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- store-detected-submodule!
  ; @ignore
  ;
  ; @description
  ; Stores the given 'submodule-props' in the 'DETECTED-SUBMODULES' atom.
  ;
  ; @param (string) submodule-path
  ; @param (map) submodule-props
  [submodule-path submodule-props]
  (swap! submodule-updater.detector.state/DETECTED-SUBMODULES assoc submodule-path submodule-props))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- detect-submodule!
  ; @ignore
  ;
  ; @description
  ; - Stores the git URL and the repository name of the submodule in the 'DETECTED-SUBMODULES' atom.
  ; - Git submodules has a '.git' file and not a '.git' directory as in normal git modules.
  ;   In the '.git' file there is a 'gitdir' path which points relatively to
  ;   the submodule's '.git' directory (which is placed in the root project's '.git' directory).
  ;
  ; @param (string) submodule-path
  [submodule-path]
  (if-let [git-url (core.env/get-submodule-git-url submodule-path)]
          (if-let [repository-name (core.utils/git-url->repository-name git-url)]
                  (store-detected-submodule! submodule-path {:git-url git-url :repository-name repository-name}))))

(defn detect-submodules!
  ; @ignore
  ;
  ; @description
  ; 1. Detects the subdirectories at the given source paths that contain a '.git' file.
  ; 2. Iterates over the subdirectory list and passes the found subdirectory paths to the 'detect-submodule!' function.
  ;
  ; @param (map) options
  ; {:source-paths (strings in vector)(opt)
  ;   Default: ["submodules"]}
  [{:keys [source-paths] :or {source-paths ["submodules"]}}]
  (reset! submodule-updater.detector.state/DETECTED-SUBMODULES nil)
  (letfn [; Cuts off the "/.git" part from end of the '.git' file's path
          ; "my-submodule/.git" => "my-submodule"
          (cut-f [git-file] (-> git-file (string/not-ends-with! "/.git")))

          ; Cuts off the "/.git" part from the end of all '.git' file's paths in the given vector.
          ; ["my-submodule/.git"] => ["my-submodule"]
          (go-up-f [git-files] (-> git-files (vector/->items cut-f)))

          ; Searches for '.git' files at the given source path.
          (search-f [source-path] (-> source-path (io/search-files #"\.git\b")))

          ; Collects all the submodule paths into the 'submodule-paths' vector.
          ; A submodule is a subdirectory where a '.git' file is found.
          (collect-f [submodule-paths source-path] (-> source-path search-f go-up-f (vector/concat-items submodule-paths)))]

         ; ...
         (let [submodule-paths (reduce collect-f [] source-paths)]
              (doseq [submodule-path submodule-paths]
                     (detect-submodule! submodule-path)))))
