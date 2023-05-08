
(ns git-handler.submodule-updater.updater.side-effects
    (:require [clj-jgit.porcelain                           :as porcelain]
              [clojure.java.shell                           :as shell]
              [git-handler.submodule-updater.builder.state  :as builder.state]
              [git-handler.submodule-updater.core.env       :as core.env]
              [git-handler.submodule-updater.detector.state :as detector.state]
              [git-handler.submodule-updater.reader.env     :as reader.env]
              [git-handler.submodule-updater.updater.env    :as updater.env]
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
  (shell/sh "git" "add" ".")
  (println "sup"))

(defn push-cached-changes!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) branch
  ; @param (string) commit-message
  [options submodule-path branch commit-message]
  (shell/sh "git" "commit" "-m" commit-message)
  (shell/sh "git" "push" "origin" branch))

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
          (if-let [current-depended-sha (updater.env/get-current-depended-sha submodule-path repository-name)]
                  (do (if (= current-depended-sha commit-sha)
                          (println (str             submodule-path "/deps.edn already updated"))
                          (println (str "Updating " submodule-path "/deps.edn")))
                      (string/replace-part deps-edn current-depended-sha commit-sha))
                  (core.env/error-catched (str "Error reading commit SHA of submodule: " submodule-path)))))

(defn update-dependency-in-other-submodules!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) commit-sha
  [options submodule-path commit-sha]
  (let [repository-name (get-in @detector.state/DETECTED-SUBMODULES [submodule-path :repository-name])]
       (println "Successful pushing from:" submodule-path)
       (println "Returned commit SHA:" commit-sha)
       (println "Updating" repository-name "dependency in the following submodule deps.edn files:")
       (doseq [[% _] @detector.state/DETECTED-SUBMODULES]
              (when (reader.env/depends-on? % repository-name)
                    (if-let [deps-edn (get-updated-deps-edn options % repository-name commit-sha)]
                            (io/write-file! (str % "/deps.edn") deps-edn)
                            (core.env/error-catched (str "Error updating deps.edn of submodule: " %)))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- update-submodule!
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  [options submodule-path]
  (println "-------------")
  (println "Updating submodule:" submodule-path "...")
  (cache-local-changes! options submodule-path)
  (println "sup2")
  (if (updater.env/submodule-local-changed? submodule-path)
      (if-let [branch (updater.env/get-config-item options submodule-path :branch "main")]
              (if-let [commit-message (updater.env/get-next-commit-message options submodule-path branch)]
                      (do (println "Pushing commit:" commit-message "from submodule:" submodule-path "to branch:" branch "...")
                          (let [{:keys [exit] :as dbg} (push-cached-changes! options submodule-path branch commit-message)]
                               (if (= 0 exit)
                                   (if-let [latest-local-commit-sha (updater.env/get-latest-local-commit-sha options submodule-path branch)]
                                           (update-dependency-in-other-submodules! options submodule-path latest-local-commit-sha)
                                           (core.env/error-catched (str "Error getting the latest local commit SHA of: " submodule-path " on branch: " branch)))
                                   (core.env/error-catched (str "Error pushing submodule: " submodule-path " to branch: " branch
                                                               (str "--" dbg))))))
                      (core.env/error-catched (str "Error creating commit message for: " submodule-path))))
      (println "Submodule unchanged:" submodule-path)))

(defn update-submodules!
  ; @param (map) options
  [options]
  (doseq [[submodule-path] @builder.state/DEPENDENCY-TREE]
         (shell/with-sh-dir submodule-path (update-submodule! options submodule-path)))
  (println "-------------")
  (println "Submodules updated"))
