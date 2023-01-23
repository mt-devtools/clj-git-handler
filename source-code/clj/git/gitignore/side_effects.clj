
(ns git.gitignore.side-effects
    (:require [git.gitignore.config  :as gitignore.config]
              [git.gitignore.helpers :as gitignore.helpers]
              [io.api                :as io]
              [noop.api              :refer [return]]
              [string.api            :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn ignore!
  ; @description
  ; Writes the given pattern to the .gitignore file.
  ; You can specify a group to the added pattern by passing the :group property.
  ;
  ; @param (string) pattern
  ; @param (map)(opt) options
  ; {:group (string)(opt)
  ;   Default: "git-api"
  ;  :filepath (string)(opt)
  ;   Default: ".gitignore"}
  ;
  ; @usage
  ; (ignore! "my-file.ext")
  ;
  ; @usage
  ; (ignore! "my-file.ext" {:group "My ignored files"})
  ;
  ; @usage
  ; (ignore! "my-file.ext" {:filepath "my-directory/.gitignore"})
  ;
  ; @example
  ; (ignore! "my-file.ext" {:group "My ignored files"})
  ; =>
  ; "\n# My ignored files\nmy-file.ext\n"
  ;
  ; @return (string)
  ; Returns with the updated .gitignore file's content.
  ([pattern]
   (ignore! pattern {}))

  ([pattern {:keys [group] :or {group "git-api"} :as options}]
   (let [gitignore (gitignore.helpers/get-gitignore options)]
        (letfn [(group-exists?    [group]     (string/contains-part? gitignore (str "# "group)))
                (write-gitignore! [gitignore] (println (str "git.api adding pattern to .gitignore: \""pattern"\""))
                                              (io/write-file! ".gitignore" gitignore {:create? true})
                                              (return gitignore))]
               (cond (gitignore.helpers/ignored? pattern options)
                     (return gitignore)
                     (group-exists? group)
                     (let [gitignore (str (string/to-first-occurence gitignore (str "# "group))
                                          (str "\n"pattern)
                                          (string/after-first-occurence gitignore (str "# "group)))]
                          (write-gitignore! gitignore))
                     :else
                     (let [gitignore (str (string/ends-with! gitignore "\n")
                                          (str "\n# "group"\n"pattern"\n"))]
                          (write-gitignore! gitignore)))))))
