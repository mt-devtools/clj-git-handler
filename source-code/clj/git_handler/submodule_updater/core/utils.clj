
(ns git-handler.submodule-updater.core.utils
    (:require [string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn git-url->repository-name
  ; @ignore
  ;
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
