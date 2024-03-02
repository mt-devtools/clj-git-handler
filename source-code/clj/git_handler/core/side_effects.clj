
(ns git-handler.core.side-effects
    (:require [clojure.java.shell      :as shell]
              [git-handler.core.env    :as core.env]
              [git-handler.core.errors :as core.errors]
              [io.api                  :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn rebase-to-branch!
  ; @description
  ; - Performs a rebase of the HEAD branch to the tip of the given branch.
  ; - Returns TRUE in case of rebase.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) base-branch
  ;
  ; @usage
  ; (rebase-to-branch! "another-branch")
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([base-branch]
   (rebase-to-branch! "." base-branch))

  ([git-path base-branch]
   (println (str "Rebasing HEAD branch to branch '" base-branch "' on git path: '" git-path "' ..."))
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: '" git-path "' is not a directory!")))
   (let [{:keys [exit] :as dbg} (shell/with-sh-dir git-path (shell/sh "git" "rebase" (str "refs/heads/" base-branch)))]
        ; ...
        (if (-> exit zero? not)
            (core.errors/error-catched (str "Cannot rebase HEAD branch to branch '" base-branch "' on git path: '" git-path "'")
                                       (str "Error: " dbg)))
        ; ...
        (println (str "HEAD branch successfully rebased to '" base-branch "'"))
        ; ...
        (-> exit zero?))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn checkout-to-branch!
  ; @description
  ; - Checks out to the given branch.
  ; - Returns TRUE in case of checkout.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) branch
  ;
  ; @usage
  ; (checkout-to-branch! "my-branch")
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([branch]
   (checkout-to-branch! "." branch))

  ([git-path branch]
   (println (str "Checking out to branch '" branch "' on git path: '" git-path "' ..."))
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: '" git-path "' is not a directory!")))
   (let [{:keys [exit] :as dbg} (shell/with-sh-dir git-path (shell/sh "git" "checkout" branch))]
        ; ...
        (if (-> exit zero? not)
            (core.errors/error-catched (str "Cannot check out to branch '" branch "' on git path: '" git-path "'")
                                       (str "Error: " dbg)))
        ; ...
        (println (str "Branch successfully checked out"))
        ; ...
        (-> exit zero?))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn cache-local-changes!
  ; @description
  ; - Caches (stages) the local changes of the HEAD branch.
  ; - Returns TRUE in case of successful caching.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ;
  ; @usage
  ; (cache-local-changes!)
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([]
   (cache-local-changes! "."))

  ([git-path]
   (println (str "Caching local changes on git path: '" git-path "' ..."))
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: '" git-path "' is not a directory!")))
   (let [{:keys [exit] :as dbg} (shell/with-sh-dir git-path (shell/sh "git" "add" "."))]
        ; ...
        (if (-> exit zero? not)
            (core.errors/error-catched (str "Cannot cache local changes on git path: '" git-path "'")
                                       (str "Error: " dbg)))
        ; ...
        (if (core.env/head-branch-has-cached-changes? git-path)
            (println (str "Local changes cached on git path: '" git-path "'"))
            (println (str "Git path '" git-path "' has no local changes")))
        ; ...
        (-> exit zero?))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn commit-cached-changes!
  ; @description
  ; - Commits the cached (staged) local changes of the given branch.
  ; - Returns TRUE in case of successful commiting.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) commit-message
  ;
  ; @usage
  ; (commit-cached-changes! "My commit")
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([commit-message]
   (commit-cached-changes! "." commit-message))

  ([git-path commit-message]
   (println (str "Commiting cached changes on git path: '" git-path "' as commit: '" commit-message "' ..."))
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: '" git-path "' is not a directory!")))
   (let [{:keys [exit] :as dbg} (shell/with-sh-dir git-path (shell/sh "git" "commit" "-m" commit-message))]
        ; ...
        (if (-> exit zero? not)
            (core.errors/error-catched (str "Cannot commit cached changes on git path: '" git-path "'")
                                       (str "Error: " dbg)))
        ; ...
        (println (str "Cached changes successfully commited"))
        ; ...
        (-> exit zero?))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn push-local-commits!
  ; @description
  ; - Pushes the local commits of the given branch.
  ; - Returns TRUE in case of successful pushing.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) target-branch
  ;
  ; @usage
  ; (push-local-commits! "my-branch")
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([target-branch]
   (push-local-commits! "." target-branch))

  ([git-path target-branch]
   (println (str "Pushing local commits on git path: '" git-path "' to branch: '" target-branch "' ..."))
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: '" git-path "' is not a directory!")))
   (let [{:keys [exit] :as dbg} (shell/with-sh-dir git-path (shell/sh "git" "push" "origin" target-branch))]
        ; ...
        (if (-> exit zero? not)
            (core.errors/error-catched (str "Cannot push local commits on git path: '" git-path "' to branch: '" target-branch "'")
                                       (str "Error: " dbg)))
        ; ...
        (println (str "Local commits successfully pushed"))
        ; ...
        (-> exit zero?))))
