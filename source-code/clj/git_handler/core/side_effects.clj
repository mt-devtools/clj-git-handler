
(ns git-handler.core.side-effects
    (:require [clojure.java.shell      :as shell]
              [git-handler.core.env    :as core.env]
              [git-handler.core.errors :as core.errors]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn cache-submodule-local-changes!
  ; @description
  ; - Caches (stages) the local changes of the HEAD branch of the given submodule.
  ; - Returns TRUE in case of successful caching.
  ;
  ; @param (string) submodule-path
  ;
  ; @usage
  ; (cache-submodule-local-changes! "submodules/my-submodule")
  ;
  ; @return (boolean)
  [submodule-path]
  (println (str "Caching local changes for submodule: '" submodule-path "' ..."))
  (let [{:keys [exit] :as dbg} (shell/with-sh-dir submodule-path (shell/sh "git" "add" "."))]
       ; ...
       (if (-> exit zero? not)
           (core.errors/error-catched (str "Cannot cache local changes of submodule: '" submodule-path "'")
                                      (str "Error: " dbg)))
       ; ...
       (if (core.env/submodule-head-branch-changed? submodule-path)
           (println (str "Local changes cached for submodule: '" submodule-path "'"))
           (println (str "Submodule '" submodule-path "' has no local changes")))
       ; ...
       (-> exit zero?)))

(defn push-submodule-cached-changes!
  ; @description
  ; - Pushes the cached (staged) local changes of the given branch of the given submodule.
  ; - Returns TRUE in case of successful pushing.
  ;
  ; @param (string) submodule-path
  ; @param (string) target-branch
  ; @param (string) commit-message
  ;
  ; @usage
  ; (push-submodule-cached-changes! "submodules/my-submodule" "main" "My commit")
  ;
  ; @return (boolean)
  [submodule-path target-branch commit-message]
  (println (str "Pushing commit: '" commit-message "' from submodule: '" submodule-path "' to branch: '" target-branch "' ..."))
  (let [{:keys [exit] :as dbg} (shell/with-sh-dir submodule-path (do (shell/sh "git" "commit" "-m" commit-message)
                                                                     (shell/sh "git" "push" "origin" target-branch)))]
       ; ...
       (if (-> exit zero? not)
           (core.errors/error-catched (str "Cannot push commit for submodule: '" submodule-path "' to branch: '" target-branch "'")
                                      (str "Error: " dbg)))
       ; ...
       (println (str "Commit successfully pushed"))
       ; ...
       (-> exit zero?)))
