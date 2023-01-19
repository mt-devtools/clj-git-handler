
(ns git.submodule-updater.core.helpers
    (:require [candy.api                            :refer [return]]
              [git.submodule-updater.detector.state :as detector.state]
              [io.api                               :as io]
              [string.api                           :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn error-catched
  ; @param (list of strings) error-message
  [& error-message]
  (doseq [line error-message]
         (println line))
  (throw :error-catched))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn git-url->repository-name
  ; @description
  ; Extracts the "author/repository-name" from the given 'git-url'.
  ;
  ; It can read four types of 'git-url':
  ; - "git@github.com:author/repository"
  ; - "git@github.com:author/repository.git"
  ; - "https://github.com/author/repository"
  ; - "https://github.com/author/repository.git"
  ;
  ; @param (string) git-url
  ;
  ; @usage
  ; (git-url->repository-name "git@github.com:author/repository")
  ;
  ; @example
  ; (git-url->repository-name "git@github.com:author/repository")
  ; =>
  ; "author/repository"
  ;
  ; @example
  ; (git-url->repository-name "git@github.com:author/repository.git")
  ; =>
  ; "author/repository"
  ;
  ; @example
  ; (git-url->repository-name "https://github.com/author/repository")
  ; =>
  ; "author/repository"
  ;
  ; @example
  ; (git-url->repository-name "https://github.com/author/repository.git")
  ; =>
  ; "author/repository"
  ;
  ; @return (string)
  [git-url]
  (cond (string/starts-with? git-url "git@github.com")
        (-> git-url (string/after-first-occurence  "github.com" {:return? false})
                    (string/after-first-occurence  ":"          {:return? true})
                    (string/before-first-occurence ".git"       {:return? true}))
        (string/starts-with? git-url "https://github.com")
        (-> git-url (string/after-first-occurence  "github.com" {:return? false})
                    (string/after-first-occurence  "/"          {:return? true})
                    (string/before-first-occurence ".git"       {:return? true}))))

(defn git-url->submodule-path
  ; @description
  ; Iterates over the detected submodules and finds which submodule has the same
  ; 'git-url' stored in the SUBMODULES atom.
  ; If a submodule has the same 'git-url', returns with the matching submodule's path
  ; (found in the SUBMODULES atom).
  ;
  ; It can read four types of 'git-url' by using the 'git-url->repository-name' function.
  ;
  ; @param (string) git-url
  ;
  ; @usage
  ; (git-url->submodule-path "git@github.com:author/repository")
  ;
  ; @return (string)
  [git-url]
  (letfn [(f [[submodule-path submodule-props]]
             (if (= (git-url->repository-name   git-url)
                    (git-url->repository-name (:git-url submodule-props)))
                 (return submodule-path)))]
         (some f @detector.state/DETECTED-SUBMODULES)))
