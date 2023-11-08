
(ns git-handler.core.env
    (:require [io.api     :as io]
              [string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-submodule-git-file
  ; @description
  ; Returns the content of the '.git' file in the given submodule's directory (if found)
  ;
  ; @param (string) submodule-path
  ;
  ; @example
  ; (read-submodule-git-file "submodules/my-submodule")
  ; =>
  ; "gitdir: ../../.git/modules/submodules/my-submodule"
  ;
  ; @return (string)
  [submodule-path]
  (io/read-file (str submodule-path "/.git")))

(defn get-submodule-git-directory-path
  ; @description
  ; 1. Reads the '.git' file in the given submodule's directory (if found)
  ; 2. Returns the 'gitdir' value from the '.git' file (if any)
  ;
  ; @param (string) submodule-path
  ;
  ; @example
  ; (get-submodule-git-directory-path "submodules/my-submodule")
  ; =>
  ; "../../.git/modules/submodules/my-submodule"
  ;
  ; @return (string)
  [submodule-path]
  (if-let [git-file-content (read-submodule-git-file submodule-path)]
          (-> git-file-content (string/after-first-occurence  "gitdir: " {:return? false})
                               (string/before-first-occurence "\n"       {:return? false}))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-submodule-config-file
  ; @description
  ; Returns the config file's content found in the git directory of the submodule
  ; (nested in the host project's '.git' directory).
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (read-submodule-config-file "submodules/my-submodule")
  ;
  ; @return (string)
  [submodule-path]
  (if-let [git-directory-path (get-submodule-git-directory-path submodule-path)]
          (io/read-file (str submodule-path"/"git-directory-path"/config"))))

(defn get-submodule-git-url
  ; @description
  ; Reads the remote origin git url of the submodule from the submodule's config file.
  ;
  ; @param (string) submodule-path
  ;
  ; @example
  ; (get-submodule-git-url "submodules/my-submodule")
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
  ; Returns the HEAD file's content found in the git directory of the submodule
  ; (nested in the host project's '.git' directory).
  ;
  ; @param (string) submodule-path
  ;
  ; @example
  ; (read-submodule-head-file "submodules/my-submodule")
  ; =>
  ; "ref: refs/heads/main"
  ;
  ; @return (string)
  [submodule-path]
  (if-let [git-directory-path (get-submodule-git-directory-path submodule-path)]
          (io/read-file (str submodule-path"/"git-directory-path"/HEAD"))))

(defn get-submodule-head-branch
  ; @description
  ; Returns the actual HEAD branch of the submodule.
  ;
  ; @param (string) submodule-path
  ;
  ; @example
  ; (get-submodule-head-branch "submodules/my-submodule")
  ; =>
  ; "main"
  ;
  ; @return (string)
  [submodule-path]
  (if-let [head-file-content (read-submodule-head-file submodule-path)]
          (-> head-file-content (string/after-first-occurence  "refs/heads/" {:return? false})
                                (string/before-first-occurence " "           {:return? true})
                                (string/before-first-occurence "\n"          {:return? true}))))

(defn submodule-branch-checked-out?
  ; @description
  ; Returns TRUE if the the passed branch is the actual HEAD branch of the submodule.
  ;
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @usage
  ; (submodule-branch-checked-out? "submodules/my-submodule" "main")
  ;
  ; @return (boolean)
  [submodule-path branch]
  (= branch (get-submodule-head-branch submodule-path)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-submodule-deps-edn
  ; @description
  ; Returns the parsed content of the 'deps.edn' file found in the submodule's directory.
  ;
  ; @param (string) submodule-path
  ;
  ; @example
  ; (read-submodule-deps-edn "submodules/my-submodule")
  ; =>
  ; "\n{:paths ["src"]\n :deps {author/repository-name {...}}}\n"
  ;
  ; @return (map)
  [submodule-path]
  (io/read-edn-file (str submodule-path "/deps.edn")))
