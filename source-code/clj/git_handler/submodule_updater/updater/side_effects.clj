
(ns git-handler.submodule-updater.updater.side-effects
    (:require [deps-edn-handler.api                         :as deps-edn-handler]
              [git-handler.core.env                         :as core.env]
              [git-handler.core.errors                      :as core.errors]
              [git-handler.core.side-effects                :as core.side-effects]
              [git-handler.submodule-updater.builder.state  :as submodule-updater.builder.state]
              [git-handler.submodule-updater.core.env       :as submodule-updater.core.env]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [git-handler.submodule-updater.reader.env     :as submodule-updater.reader.env]
              [git-handler.submodule-updater.updater.env    :as submodule-updater.updater.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-dependency-in-other-submodules!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) commit-sha
  [options submodule-path commit-sha]
  (let [repository-name (get-in @submodule-updater.detector.state/DETECTED-SUBMODULES [submodule-path :repository-name])]
       (println (str "Updating '" repository-name "' dependency in the following submodules' 'deps.edn' files:"))
       (doseq [[% _] @submodule-updater.detector.state/DETECTED-SUBMODULES]
              (when (submodule-updater.reader.env/depends-on? % repository-name)
                    (println (str "Updating '" % "' submodule's 'deps.edn' ..."))
                    (or (try (deps-edn-handler/update-dependency-git-commit-sha! % repository-name commit-sha)
                             (catch Exception e (println e)))
                        (core.errors/error-catched (str "Error updating 'deps.edn' file of submodule '" % "'")))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- update-submodule!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  [options submodule-path]
  (println "-------------")
  (println (str "Updating submodule: '" submodule-path "' ..."))
  (and (core.side-effects/cache-submodule-local-changes! submodule-path)
       (core.env/submodule-head-branch-changed?          submodule-path)
       (if-let [target-branch (submodule-updater.core.env/get-config-item options submodule-path :target-branch "main")]
               (if (core.env/submodule-branch-checked-out? submodule-path target-branch)
                   (if-let [commit-message (submodule-updater.updater.env/get-next-commit-message options submodule-path target-branch)]
                           (and (core.side-effects/push-submodule-cached-changes! submodule-path target-branch commit-message)
                                (if-let [last-local-commit-sha (core.env/get-submodule-last-local-commit-sha submodule-path target-branch)]
                                        (update-dependency-in-other-submodules! options submodule-path last-local-commit-sha))))
                   (core.errors/error-catched (str "Submodule '" submodule-path"' is checked out on another branch than the provided '" target-branch "' target branch"))))))

(defn update-submodules!
  ; @param (map) options
  [options]
  (doseq [[submodule-path] @submodule-updater.builder.state/DEPENDENCY-TREE]
         (update-submodule! options submodule-path))
  (println "-------------")
  (println "Submodules updated"))
