
(ns git-handler.submodules.env
    (:require [fruits.regex.api        :as regex]
              [fruits.string.api       :as string]
              [fruits.vector.api       :as vector]
              [io.api                  :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-submodule-git-file
  ; @description
  ; Returns the content of the '.git' file (if found) at the given submodule path.
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (read-submodule-git-file "my-submodules/my-submodule")
  ; =>
  ; "gitdir: ../../.git/modules/my-submodules/my-submodule"
  ;
  ; @return (string)
  [submodule-path]
  ; The 'get-submodule-paths' function requires the '{:warn? false}' setting.
  ; Otherwise, it would print warning messages for every subdirectory that does not contain a submodule.
  (io/read-file (str submodule-path "/.git") {:warn? false}))

(defn get-submodule-git-directory-path
  ; @description
  ; - Reads the '.git' file (if found) at the given submodule path.
  ; - Returns the 'gitdir' value (if any) from the '.git' file.
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (get-submodule-git-directory-path "my-submodules/my-submodule")
  ; =>
  ; "../../.git/modules/my-submodules/my-submodule"
  ;
  ; @return (string)
  [submodule-path]
  (if-let [git-file-content (read-submodule-git-file submodule-path)]
          (-> git-file-content (string/after-first-occurence  "gitdir: " {:return? false})
                               (string/before-first-occurence "\n"       {:return? false}))))

(defn submodule-path?
  ; @description
  ; Returns whether the given directory path corresponds to a git submodule.
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (submodule-path? "my-submodules/my-submodule")
  ; =>
  ; true
  ;
  ; @return (boolean)
  [submodule-path]
  (if-let [git-file-content (read-submodule-git-file submodule-path)]
          (regex/re-match? git-file-content #"^gitdir\:")))

(defn get-submodule-paths
  ; @description
  ; Returns the submodule paths found at the given directory path.
  ;
  ; @param (string)(opt) directory-path
  ; Default: "."
  ;
  ; @usage
  ; (get-submodule-paths "my-submodules")
  ; =>
  ; ["my-submodules/my-submodule"]
  ;
  ; @return (strings in vector)
  ([]
   (get-submodule-paths "."))

  ([directory-path]
   (-> directory-path io/all-subdirectory-list (vector/keep-items-by submodule-path?))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-submodule-config-file
  ; @description
  ; Returns the content of the config file that corresponds to the given submodule.
  ; (Submodule config files are placed in the host project's '.git' directory).
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (read-submodule-config-file "my-submodules/my-submodule")
  ; =>
  ; "..."
  ;
  ; @return (string)
  [submodule-path]
  (if-let [git-directory-path (get-submodule-git-directory-path submodule-path)]
          (io/read-file (str submodule-path"/"git-directory-path"/config"))))

(defn get-submodule-git-url
  ; @description
  ; Returns the remote origin git url from the config file that corresponds to the given submodule.
  ; (Submodule config files are placed in the host project's '.git' directory).
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (get-submodule-git-url "my-submodules/my-submodule")
  ; =>
  ; "git@github.com:author/my-submodule.git"
  ;
  ; @return (string)
  [submodule-path]
  (if-let [config-file-content (read-submodule-config-file submodule-path)]
          (-> config-file-content (string/after-first-occurence  "[remote \"origin\"]" {:return? false})
                                  (string/after-first-occurence  "url = "              {:return? false})
                                  (string/before-first-occurence "\n"                  {:return? true}))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-submodule-head-file
  ; @description
  ; Returns the content of the HEAD file that corresponds to the given submodule.
  ; (Submodule HEAD files are placed in the host project's '.git' directory).
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (read-submodule-head-file "my-submodules/my-submodule")
  ; =>
  ; "ref: refs/heads/main"
  ;
  ; @return (string)
  [submodule-path]
  (if-let [git-directory-path (get-submodule-git-directory-path submodule-path)]
          (io/read-file (str submodule-path"/"git-directory-path"/HEAD"))))
