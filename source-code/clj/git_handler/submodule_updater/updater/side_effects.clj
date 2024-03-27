
(ns git-handler.submodule-updater.updater.side-effects
    (:require [deps-edn-handler.api                         :as deps-edn-handler]
              [git-handler.core.env                         :as core.env]
              [git-handler.core.errors                      :as core.errors]
              [git-handler.core.side-effects                :as core.side-effects]
              [git-handler.submodule-updater.core.env       :as submodule-updater.core.env]
              [git-handler.submodule-updater.detector.env    :as submodule-updater.detector.env]
              [git-handler.submodule-updater.builder.env    :as submodule-updater.builder.env]
              [git-handler.submodule-updater.updater.env    :as submodule-updater.updater.env]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-dependency-in-other-submodules!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) commit-sha
  [options submodule-path commit-sha]
  (let [repository-name (submodule-updater.detector.env/get-submodule-repository-name options submodule-path)]
       (println (str "Updating '" repository-name "' dependency in the 'deps.edn' file of the following submodules:"))
       (doseq [% (submodule-updater.builder.env/get-dependency-cascade options)]
              (when (submodule-updater.detector.env/submodule-depends-on? options % repository-name)
                    (println (str "Updating 'deps.edn' file of submodule: '" % "' ..."))
                    (or (try (deps-edn-handler/update-git-dependency-commit-sha! % repository-name commit-sha)
                             (catch Exception e (println e)))
                        (core.errors/error-catched (str "Cannot update 'deps.edn' file of submodule '" % "'")))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-on-pushed-f!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  [options submodule-path commit-message last-local-commit-sha]
  (if-let [on-pushed-f (submodule-updater.core.env/get-config-value options submodule-path :on-pushed-f)]
          (try (on-pushed-f submodule-path commit-message last-local-commit-sha)
               (catch Exception e nil))))

(defn- update-submodule!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  [options submodule-path]
  (println "-------------")
  (println (str "Updating submodule: '" submodule-path "' ..."))
  (and (core.side-effects/cache-local-changes!   submodule-path)
       (core.env/head-branch-has-cached-changes? submodule-path)
       (let [target-branch (submodule-updater.core.env/get-config-value options submodule-path :target-branch "main")]
            (if (core.env/branch-checked-out? submodule-path target-branch)
                (if-let [commit-message (submodule-updater.updater.env/get-next-commit-message options submodule-path target-branch)]
                        (and (core.side-effects/commit-cached-changes! submodule-path commit-message)
                             (core.side-effects/push-local-commits!    submodule-path target-branch)
                             (when-let [last-local-commit-sha (core.env/get-last-local-commit-sha submodule-path target-branch)]
                                       (apply-on-pushed-f!                     options submodule-path commit-message last-local-commit-sha)
                                       (update-dependency-in-other-submodules! options submodule-path                last-local-commit-sha))))
                (core.errors/error-catched (str "Submodule '" submodule-path"' is checked out on another branch than the provided '" target-branch "' target branch"))))))

(defn update-submodules!
  ; @ignore
  ;
  ; @param (map) options
  [options]
  (doseq [submodule-path (submodule-updater.builder.env/get-dependency-cascade options)]
         (update-submodule! options submodule-path))
  (println "-------------")
  (println "Submodules updated"))
