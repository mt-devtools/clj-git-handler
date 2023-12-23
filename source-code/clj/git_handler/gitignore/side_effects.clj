
(ns git-handler.gitignore.side-effects
    (:require [fruits.string.api            :as string]
              [git-handler.gitignore.config :as gitignore.config]
              [git-handler.gitignore.env    :as gitignore.env]
              [io.api                       :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn ignore!
  ; @description
  ; - Appends the given pattern to the '.gitignore' file-
  ; - Returns the updated '.gitignore' file's content.
  ;
  ; @param (string) pattern
  ; @param (map)(opt) options
  ; {:group (string)(opt)
  ;   Default: "clj-git-handler"
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
  ; @usage
  ; (ignore! "my-file.ext" {:group "My ignored files"})
  ; =>
  ; "\n# My ignored files\nmy-file.ext\n"
  ;
  ; @return (string)
  ([pattern]
   (ignore! pattern {}))

  ([pattern {:keys [group] :or {group "clj-git-handler"} :as options}]
   (let [gitignore (gitignore.env/get-gitignore options)]
        (letfn [(group-exists?    [group]     (string/contains-part? gitignore (str "# "group)))
                (write-gitignore! [gitignore] (println (str "clj-git-handler adding pattern to .gitignore: \""pattern"\""))
                                              (io/write-file! ".gitignore" gitignore {:create? true})
                                              (-> gitignore))]
               (cond (gitignore.env/ignored? pattern options)
                     (-> gitignore)
                     (-> group group-exists?)
                     (let [gitignore (str (string/to-first-occurence gitignore (str "# "group))
                                          (str "\n"pattern)
                                          (string/after-first-occurence gitignore (str "# "group)))]
                          (write-gitignore! gitignore))
                     :else
                     (let [gitignore (str (string/ends-with! gitignore "\n")
                                          (str "\n# "group"\n"pattern"\n"))]
                          (write-gitignore! gitignore)))))))
