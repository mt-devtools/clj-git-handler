
(ns git-handler.core.env
    (:require [clojure.java.shell      :as shell]
              [fruits.string.api       :as string]
              [git-handler.core.errors :as core.errors]
              [io.api                  :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-local-commit-history
  ; @description
  ; Returns the local commit history of the given branch.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) branch
  ;
  ; @usage
  ; (get-local-commit-history "main")
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
  ([branch]
   (get-local-commit-history "." branch))

  ([git-path branch]
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: \"" git-path "\" is not a directory!")))
   (let [{:keys [exit out] :as dbg} (shell/with-sh-dir git-path (shell/sh "git" "log" "origin" branch))]
        (if (-> exit zero?)
            (-> out)
            (core.errors/error-catched (str "Cannot read local commit history of git path: '" git-path "' on branch: '" branch "'")
                                       (str "Error: " dbg))))))

(defn get-last-local-commit-message
  ; @description
  ; Returns the last local commit message of the given branch.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) branch
  ;
  ; @usage
  ; (get-last-local-commit-message "main")
  ; =>
  ; "Second commit"
  ;
  ; @return (string)
  ([branch]
   (get-last-local-commit-message "." branch))

  ([git-path branch]
   (let [local-commit-history (get-local-commit-history git-path branch)]
        (or (-> local-commit-history (string/after-first-occurence  "Date:" {:return? false})
                                     (string/after-first-occurence  "\n\n"  {:return? false})
                                     (string/before-first-occurence "\n"    {:return? false})
                                     (string/trim)
                                     (string/to-nil {:if-empty? true}))
            (core.errors/error-catched (str "Cannot get last local commit message of git path: '" git-path "' on branch: '" branch "'"))))))

(defn get-last-local-commit-sha
  ; @description
  ; Returns the last local commit SHA of the given branch.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) branch
  ;
  ; @usage
  ; (get-last-local-commit-sha "main")
  ; =>
  ; "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
  ;
  ; @return (string)
  ([branch]
   (get-last-local-commit-sha "." branch))

  ([git-path branch]
   (let [local-commit-history (get-local-commit-history git-path branch)]
        (or (-> local-commit-history (string/after-first-occurence  "commit" {:return? false})
                                     (string/before-first-occurence "\n"     {:return? false})
                                     (string/trim)
                                     (string/to-nil {:if-empty? true}))
            (core.errors/error-catched (str "Cannot get last local commit SHA of git path: '" git-path "' on branch: '" branch "'"))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn local-branch-has-changes?
  ; @important
  ; This function is incomplete and may not behave as expected.
  ;
  ; @description
  ; Returns whether the given local branch has changes.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) branch
  ;
  ; @usage
  ; (local-branch-has-changes? "main")
  ; =>
  ; true
  ;
  ; @return (boolean)
  [git-path branch])
  ; TODO

(defn local-branch-has-cached-changes?
  ; @important
  ; This function is incomplete and may not behave as expected.
  ;
  ; @description
  ; Returns whether the given local branch has cached (staged) changes.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) branch
  ;
  ; @usage
  ; (local-branch-has-cached-changes? "main")
  ; =>
  ; true
  ;
  ; @return (boolean)
  [git-path branch])
  ; TODO

(defn head-branch-has-changes?
  ; @description
  ; Returns whether the HEAD branch has changes.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ;
  ; @usage
  ; (head-branch-has-changes?)
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([]
   (head-branch-has-changes? "."))

  ([git-path]
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: \"" git-path "\" is not a directory!")))
   (shell/with-sh-dir git-path (-> (shell/sh "git" "diff" "--name-only") :out empty? not))))

(defn head-branch-has-cached-changes?
  ; @description
  ; Returns whether the HEAD branch has cached (staged) changes.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ;
  ; @usage
  ; (head-branch-has-cached-changes?)
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([]
   (head-branch-has-cached-changes? "."))

  ([git-path]
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: \"" git-path "\" is not a directory!")))
   (shell/with-sh-dir git-path (-> (shell/sh "git" "diff" "--name-only" "--cached") :out empty? not))))
