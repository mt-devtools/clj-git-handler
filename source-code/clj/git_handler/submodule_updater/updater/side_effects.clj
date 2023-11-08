
(ns git-handler.submodule-updater.updater.side-effects
    (:require [clojure.java.shell                           :as shell]
              [git-handler.core.env                         :as core.env]
              [git-handler.core.errors                      :as core.errors]
              [git-handler.submodule-updater.builder.state  :as submodule-updater.builder.state]
              [git-handler.submodule-updater.core.env       :as submodule-updater.core.env]
              [git-handler.submodule-updater.detector.state :as submodule-updater.detector.state]
              [git-handler.submodule-updater.reader.env     :as submodule-updater.reader.env]
              [git-handler.submodule-updater.updater.env    :as submodule-updater.updater.env]
              [io.api                                       :as io]
              [string.api                                   :as string]
              [vector.api                                   :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn cache-local-changes!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  [_ _]
  (println "Caching local changes ...")
  (shell/sh "git" "add" "."))

(defn push-cached-changes!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) target-branch
  ; @param (string) commit-message
  [options submodule-path target-branch commit-message]
  (shell/sh "git" "commit" "-m" commit-message)
  (shell/sh "git" "push" "origin" target-branch))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-updated-deps-edn
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) repository-name
  ; @param (string) commit-sha
  [options submodule-path repository-name commit-sha]
  (if-let [deps-edn (io/read-file (str submodule-path "/deps.edn"))]
          (if-let [current-depended-sha (submodule-updater.updater.env/get-current-depended-sha submodule-path repository-name)]
                  (do (if (= current-depended-sha commit-sha)
                          (println (str          "'" submodule-path "/deps.edn' already updated"))
                          (println (str "Updating '" submodule-path "/deps.edn'")))
                      (string/replace-part deps-edn current-depended-sha commit-sha))
                  (core.errors/error-catched (str "Error reading commit SHA of submodule: '" submodule-path "'")))))

(defn update-dependency-in-other-submodules!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) commit-sha
  [options submodule-path commit-sha]
  (let [repository-name (get-in @submodule-updater.detector.state/DETECTED-SUBMODULES [submodule-path :repository-name])]
       (println (str "Successful pushing from: '" submodule-path "'"))
       (println (str "Returned commit SHA: '" commit-sha "'"))
       (println (str "Updating '" repository-name "' dependency in the following submodule 'deps.edn' files:"))
       (doseq [[% _] @submodule-updater.detector.state/DETECTED-SUBMODULES]
              (when (submodule-updater.reader.env/depends-on? % repository-name)
                    (if-let [deps-edn (get-updated-deps-edn options % repository-name commit-sha)]
                            (io/write-file! (str % "/deps.edn") deps-edn)
                            (core.errors/error-catched (str "Error updating deps.edn of submodule: '" % "'")))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- update-submodule!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  [options submodule-path]
  (println "-------------")
  (println "Updating submodule: '" submodule-path "' ...")
  (cache-local-changes! options submodule-path)
  (if (submodule-updater.updater.env/submodule-locally-changed? submodule-path)
      (if-let [target-branch (submodule-updater.core.env/get-config-item options submodule-path :target-branch "main")]
              (if (core.env/submodule-branch-checked-out? submodule-path target-branch)
                  (if-let [commit-message (submodule-updater.updater.env/get-next-commit-message options submodule-path target-branch)]
                          (do (println (str "Pushing commit: '" commit-message "' from submodule: '" submodule-path "' to branch: '" target-branch "' ..."))
                              (let [{:keys [exit] :as dbg} (push-cached-changes! options submodule-path target-branch commit-message)]
                                   (if (-> exit zero?)
                                       (if-let [latest-local-commit-sha (submodule-updater.updater.env/get-latest-local-commit-sha options submodule-path target-branch)]
                                               (update-dependency-in-other-submodules! options submodule-path latest-local-commit-sha)
                                               (core.errors/error-catched (str "Error getting the latest local commit SHA of: '" submodule-path "' on branch: '" target-branch "'")))
                                       (core.errors/error-catched (str "Error pushing submodule: '" submodule-path "' to branch: '" target-branch "'")
                                                                  (str "Error: " dbg)))))
                          (core.errors/error-catched (str "Error creating commit message for: '" submodule-path "'")))
                  (do (println "submodule-path:" submodule-path)
                      (println "options:" options)
                      (println "target-branch:" target-branch)
                      (core.errors/error-catched (str "Submodule '" submodule-path"' is checked out on another branch than the provided '" target-branch "' target branch"))))
              (core.errors/error-catched (str "Unable to read config item ':target-branch' for submodule: '" submodule-path "'")))
      (println (str "Submodule unchanged: '" submodule-path "'"))))

(defn update-submodules!
  ; @param (map) options
  [options]
  (doseq [[submodule-path] @submodule-updater.builder.state/DEPENDENCY-TREE]
         (shell/with-sh-dir submodule-path (update-submodule! options submodule-path)))
  (println "-------------")
  (println "Submodules updated"))
