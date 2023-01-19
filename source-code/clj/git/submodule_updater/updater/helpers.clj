
(ns git.submodule-updater.updater.helpers
    (:require [clojure.java.shell                     :as shell]
              [git.submodule-updater.core.helpers     :as core.helpers]
              [git.submodule-updater.detector.helpers :as detector.helpers]
              [io.api                                 :as io]
              [string.api                             :as string]
              [time.api                               :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-config-item
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (*) config-key
  ; @param (*) default-value
  ;
  ; @return (*)
  [options submodule-path config-key & [default-value]]
  (if-let [repository-name (-> submodule-path detector.helpers/submodule-path->git-url core.helpers/git-url->repository-name)]
          (or (get-in options [repository-name config-key])
              (get-in options [:default        config-key] default-value))
          (core.helpers/error-catched (str "Cannot derive repository name from submodule path: " submodule-path))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn submodule-local-changed?
  ; @param (string) submodule-path
  [_]
  ; cached = staged
  (-> (shell/sh "git" "diff" "--name-only" "--cached")
      :out empty? not))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-current-depended-sha
  ; @param (string) submodule-path
  ; @param (string) repository-name
  ;
  ; @usage
  ; (get-current-depended-sha "submodules/my-repository" "author/another-repository")
  ;
  ; @return (string)
  [submodule-path repository-name]
  ; Reads the current commit SHA of the repository-name from the deps.edn file
  ; of the given submodule path
  (if-let [deps-edn (io/read-file (str submodule-path "/deps.edn"))]
          (or (-> deps-edn (string/after-first-occurence  repository-name {:return? false})
                           (string/after-first-occurence  ":sha"          {:return? false})
                           (string/after-first-occurence  "\""            {:return? false})
                           (string/before-first-occurence "\""            {:return? false}))
              (core.helpers/error-catched (str "Cannot read current depended SHA from submodule: " submodule-path " of dependency: " repository-name)))
          (core.helpers/error-catched (str "Cannot read the deps.edn file in submodule: " submodule-path))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-latest-local-commit-sha
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @return (string)
  [_ submodule-path branch]
  (let [{:keys [exit out] :as dbg} (shell/sh "git" "log" "origin" branch)]
       (if (= 0 exit)
           (-> out (string/after-first-occurence  "commit" {:return? false})
                   (string/before-first-occurence "\n"     {:return? false})
                   (string/trim)
                   (string/use-nil))
           (core.helpers/error-catched (str "Cannot read latest commit SHA of submodule: " submodule-path " on branch: " branch)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-latest-local-commit-message
  ; @param (map) options
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @return (string)
  [_ submodule-path branch]
  (let [{:keys [exit out] :as dbg} (shell/sh "git" "log" "origin" branch)]
       (if (= 0 exit)
           (-> out (string/after-first-occurence  "Date:" {:return? false})
                   (string/after-first-occurence  "\n\n"  {:return? false})
                   (string/before-first-occurence "\n"    {:return? false})
                   (string/trim)
                   (string/use-nil))
           (core.helpers/error-catched (str "Cannot read latest commit message of submodule: " submodule-path " on branch: " branch)
                                       (str "--" dbg)))))

(defn get-next-commit-message
  ; @param (map) options
  ; {:config (map)(opt)
  ;   {"author/my-repository" {:commit-message-f (function)(opt)
  ;                            :branch (string)(opt)}}
  ;  :default (map)(opt)
  ;   {:commit-message-f (function)(opt)
  ;    :branch (string)(opt)}}
  ; @param (string) submodule-path
  ; @param (string) branch
  ;
  ; @return (string)
  [options submodule-path branch]
  (if-let [commit-message-f (get-config-item options submodule-path :commit-message-f (fn [%] (time/timestamp-string)))]
          (if-let [latest-local-commit-message (get-latest-local-commit-message options submodule-path branch)]
                  (commit-message-f latest-local-commit-message)
                  (core.helpers/error-catched (str "Cannot read latest local commit message of submodule: " submodule-path " on branch: " branch)))))
