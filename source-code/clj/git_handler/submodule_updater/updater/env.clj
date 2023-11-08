
(ns git-handler.submodule-updater.updater.env
    (:require [clojure.java.shell                         :as shell]
              [git-handler.core.env                       :as core.env]
              [git-handler.core.errors                    :as core.errors]
              [git-handler.core.utils                     :as core.utils]
              [git-handler.submodule-updater.core.env     :as submodule-updater.core.env]
              [git-handler.submodule-updater.detector.env :as submodule-updater.detector.env]
              [string.api                                 :as string]
              [time.api                                   :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-locally-changed?
  ; @ignore
  ;
  ; @param (string) submodule-path
  [_]
  ; cached = staged
  (-> (shell/sh "git" "diff" "--name-only" "--cached")
      :out empty? not))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-current-depended-sha
  ; @ignore
  ;
  ; @description
  ; Reads the current commit SHA of the 'repository-name' from the deps.edn file
  ; of the given submodule path.
  ;
  ; @param (string) submodule-path
  ; @param (string) repository-name
  ;
  ; @usage
  ; (get-current-depended-sha "submodules/my-repository" "author/another-repository")
  ;
  ; @return (string)
  [submodule-path repository-name]
  (if-let [deps-edn (core.env/read-submodule-deps-edn submodule-path)]
          (or (-> deps-edn (string/after-first-occurence  repository-name {:return? false})
                           (string/after-first-occurence  ":sha"          {:return? false})
                           (string/after-first-occurence  "\""            {:return? false})
                           (string/before-first-occurence "\""            {:return? false}))
              (core.errors/error-catched (str "Cannot read current depended SHA from submodule: '" submodule-path "' of dependency: '" repository-name "'")))
          (core.errors/error-catched (str "Cannot read the deps.edn file in submodule: '" submodule-path "'"))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-latest-local-commit-sha
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @return (string)
  [_ submodule-path branch]
  (let [{:keys [exit out] :as dbg} (shell/sh "git" "log" "origin" branch)]
       (if (-> exit zero?)
           (-> out (string/after-first-occurence  "commit" {:return? false})
                   (string/before-first-occurence "\n"     {:return? false})
                   (string/trim)
                   (string/use-nil))
           (core.errors/error-catched (str "Cannot read latest commit SHA of submodule: '" submodule-path "' on branch: '" branch "'")))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-latest-local-commit-message
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @return (string)
  [_ submodule-path branch]
  (let [{:keys [exit out] :as dbg} (shell/sh "git" "log" "origin" branch)]
       (if (-> exit zero?)
           (-> out (string/after-first-occurence  "Date:" {:return? false})
                   (string/after-first-occurence  "\n\n"  {:return? false})
                   (string/before-first-occurence "\n"    {:return? false})
                   (string/trim)
                   (string/use-nil))
           (core.errors/error-catched (str "Cannot read latest commit message of submodule: '" submodule-path "' on branch: '" branch "'")
                                      (str "Error: " dbg)))))

(defn get-next-commit-message
  ; @ignore
  ;
  ; @param (map) options
  ; {:config (map)(opt)
  ;   {"author/my-repository" {:commit-message-f (function)(opt)
  ;                            :target-branch (string)(opt)}}
  ;  :default (map)(opt)
  ;   {:commit-message-f (function)(opt)
  ;    :target-branch (string)(opt)}}
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @return (string)
  [options submodule-path branch]
  (if-let [commit-message-f (submodule-updater.core.env/get-config-item options submodule-path :commit-message-f (fn [%] (time/timestamp-string)))]
          (if-let [latest-local-commit-message (get-latest-local-commit-message options submodule-path branch)]
                  (try (commit-message-f latest-local-commit-message)
                       (catch Exception e nil))
                  (core.errors/error-catched (str "Cannot read latest local commit message of submodule: '" submodule-path "' on branch: '" branch "'")))
          (core.errors/error-catched (str "Unable to read config item ':commit-message-f' for submodule: '" submodule-path "'"))))
