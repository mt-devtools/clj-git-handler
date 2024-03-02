
(ns git-handler.core.side-effects
    (:require [clojure.java.shell      :as shell]
              [git-handler.core.env    :as core.env]
              [git-handler.core.errors :as core.errors]
              [io.api                  :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-out-to-branch!
  ; @description
  ; - Checks out to the given branch.
  ; - Returns TRUE in case of checkout.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) branch
  ;
  ; @usage
  ; (check-out-to-branch! "my-branch")
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([branch]
   (check-out-to-branch! "." branch))

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

(defn push-cached-changes!
  ; @description
  ; - Pushes the cached (staged) local changes of the given branch.
  ; - Returns TRUE in case of successful pushing.
  ;
  ; @param (string)(opt) git-path
  ; Default: "."
  ; @param (string) target-branch
  ; @param (string) commit-message
  ;
  ; @usage
  ; (push-cached-changes! "my-branch" "My commit")
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([target-branch commit-message]
   (push-cached-changes! "." target-branch commit-message))

  ([git-path target-branch commit-message]
   (println (str "Pushing commit: '" commit-message "' on git path: '" git-path "' to branch: '" target-branch "' ..."))
   (if-not (io/directory? git-path)
           (core.errors/error-catched (str "Git path: '" git-path "' is not a directory!")))
   (let [{:keys [exit] :as dbg} (shell/with-sh-dir git-path (do (shell/sh "git" "commit" "-m"     commit-message)
                                                                (shell/sh "git" "push"   "origin" target-branch)))]
        ; ...
        (if (-> exit zero? not)
            (core.errors/error-catched (str "Cannot push commit on git path: '" git-path "' to branch: '" target-branch "'")
                                       (str "Error: " dbg)))
        ; ...
        (println (str "Commit successfully pushed"))
        ; ...
        (-> exit zero?))))
