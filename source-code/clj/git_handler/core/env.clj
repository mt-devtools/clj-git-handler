
(ns git-handler.core.env
    (:require [clojure.java.shell      :as shell]
              [fruits.string.api       :as string]
              [fruits.regex.api :as regex]
              [fruits.vector.api :as vector]
              [git-handler.core.errors :as core.errors]
              [io.api                  :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-submodule-git-file
  ; @description
  ; Returns the content of the '.git' file in the given submodule's directory (if found)
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (read-submodule-git-file "submodules/my-submodule")
  ; =>
  ; "gitdir: ../../.git/modules/submodules/my-submodule"
  ;
  ; @return (string)
  [submodule-path]
  ; The 'get-submodule-paths' function requires the '{:warn? false}' setting,
  ; otherwise it would print warning messages for every subdirectory that does not contain a submodule.
  (io/read-file (str submodule-path "/.git") {:warn? false}))

(defn get-submodule-git-directory-path
  ; @description
  ; 1. Reads the '.git' file in the given submodule's directory (if found)
  ; 2. Returns the 'gitdir' value from the '.git' file (if any)
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (get-submodule-git-directory-path "submodules/my-submodule")
  ; =>
  ; "../../.git/modules/submodules/my-submodule"
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
  ; (submodule-path? "submodules/my-submodule")
  ;
  ; @return (boolean)
  [submodule-path]
  (if-let [git-file-content (read-submodule-git-file submodule-path)]
          (regex/re-match? git-file-content #"^gitdir\:")))

(defn get-submodule-paths
  ; @description
  ; Returns whether the given directory path corresponds to a git submodule.
  ;
  ; @param (string)(opt) directory-path
  ; Default: "."
  ;
  ; @usage
  ; (get-submodule-paths "submodules")
  ;
  ; @usage
  ; (get-submodule-paths "submodules")
  ; =>
  ; ["submodules/my-submodule"]
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
  ; @usage
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
  ; @usage
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
  ; @usage
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

(defn get-submodule-local-commit-history
  ; @description
  ; Returns the local commit history of the given branch of submodule.
  ;
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @usage
  ; (get-submodule-local-commit-history "submodules/my-submodule" "main")
  ; =>
  ; "commit xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
  ;  Author: Author <00000000+author@users.noreply.github.com>
  ;  Date:   Fri Oct 21 14:14:14 2020 +0000
  ;
  ;      Second commit
  ;
  ;  commit xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
  ;  Author: Author <00000000+author@users.noreply.github.com>
  ;  Date:   Fri Oct 21 12:12:12 2020 +0000
  ;
  ;      Initial commit"
  ;
  ; @return (string)
  [submodule-path branch]
  (let [{:keys [exit out] :as dbg} (shell/with-sh-dir submodule-path (shell/sh "git" "log" "origin" branch))]
       (if (-> exit zero?)
           (-> out)
           (core.errors/error-catched (str "Cannot read local commit history of submodule: '" submodule-path "' on branch: '" branch "'")
                                      (str "Error: " dbg)))))

(defn get-submodule-last-local-commit-message
  ; @description
  ; Returns the last local commit message of the given branch of submodule.
  ;
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @usage
  ; (get-submodule-last-local-commit-message "submodules/my-submodule" "main")
  ; =>
  ; "Initial commit"
  ;
  ; @return (string)
  [submodule-path branch]
  (let [local-commit-history (get-submodule-local-commit-history submodule-path branch)]
       (or (-> local-commit-history (string/after-first-occurence  "Date:" {:return? false})
                                    (string/after-first-occurence  "\n\n"  {:return? false})
                                    (string/before-first-occurence "\n"    {:return? false})
                                    (string/trim)
                                    (string/to-nil {:if-empty? true}))
           (core.errors/error-catched (str "Cannot get last local commit message of submodule: '" submodule-path "' on branch: '" branch "'")))))

(defn get-submodule-last-local-commit-sha
  ; @description
  ; Returns the last local commit SHA of the given branch of submodule.
  ;
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @usage
  ; (get-submodule-last-local-commit-sha "submodules/my-submodule" "main")
  ; =>
  ; "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
  ;
  ; @return (string)
  [submodule-path branch]
  (let [local-commit-history (get-submodule-local-commit-history submodule-path branch)]
       (or (-> local-commit-history (string/after-first-occurence  "commit" {:return? false})
                                    (string/before-first-occurence "\n"     {:return? false})
                                    (string/trim)
                                    (string/to-nil {:if-empty? true}))
           (core.errors/error-catched (str "Cannot get last local commit SHA of submodule: '" submodule-path "' on branch: '" branch "'")))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-local-branch-changed?
  ; @description
  ; Returns whether the given local branch of the submodule contains cached / staged changes.
  ;
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @usage
  ; (submodule-local-branch-changed? "submodules/my-submodule" "main")
  ;
  ; @return (boolean)
  [submodule-path branch])
  ; TODO

(defn submodule-head-branch-changed?
  ; @description
  ; Returns whether the HEAD branch of the submodule contains cached / staged changes.
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (submodule-head-branch-changed? "submodules/my-submodule")
  ;
  ; @return (boolean)
  [submodule-path]
  (shell/with-sh-dir submodule-path (-> (shell/sh "git" "diff" "--name-only" "--cached") :out empty? not)))
