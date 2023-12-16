
(ns git-handler.core.utils
    (:require [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn git-url->repository-name
  ; @description
  ; - Derives the 'author/my-repository' string from the given 'git-url'.
  ; - It can read the following types of 'git-url':
  ;   - 'git@github.com:author/my-repository'
  ;   - 'git@github.com:author/my-repository.git'
  ;   - 'https://github.com/author/my-repository'
  ;   - 'https://github.com/author/my-repository.git'
  ;
  ; @param (string) git-url
  ;
  ; @usage
  ; (git-url->repository-name "git@github.com:author/my-repository")
  ; =>
  ; "author/my-repository"
  ;
  ; @usage
  ; (git-url->repository-name "git@github.com:author/my-repository.git")
  ; =>
  ; "author/my-repository"
  ;
  ; @usage
  ; (git-url->repository-name "https://github.com/author/my-repository")
  ; =>
  ; "author/my-repository"
  ;
  ; @usage
  ; (git-url->repository-name "https://github.com/author/my-repository.git")
  ; =>
  ; "author/my-repository"
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
