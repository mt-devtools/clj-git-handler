
(ns git.side-effects
    (:require [candy.api  :refer [return]]
              [io.api     :as io]
              [string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-gitignore
  ; @usage
  ; (get-gitignore)
  ;
  ; @return (string)
  []
  (io/read-file ".gitignore"))

(defn ignored?
  ; @param (string) pattern
  ;
  ; @usage
  ; (ignored? "my-file.ext")
  ;
  ; @return (boolean)
  [pattern]
  (string/contains-part? (get-gitignore)
                         (str "\n"pattern"\n")))

(defn ignore!
  ; @param (string) pattern
  ; @param (string)(opt) block-name
  ;  Default: "git-api"
  ;
  ; @usage
  ; (ignore! "my-file.ext")
  ;
  ; @usage
  ; (ignore! "my-file.ext" "My ignored files")
  ;
  ; @return (string)
  ([pattern]
   (ignore! pattern "git-api"))

  ([pattern block-name]
   (let [gitignore (get-gitignore)]
        (letfn [(block-exists?    [block-name] (string/contains-part? gitignore (str "# "block-name)))
                (write-gitignore! [gitignore]  (println (str "git.api adding pattern to .gitignore: \""pattern"\""))
                                               (io/write-file! ".gitignore" gitignore {:create? true})
                                               (return gitignore))]
               (cond (ignored? pattern)
                     (return gitignore)
                     (block-exists? block-name)
                     (let [gitignore (str (string/to-first-occurence gitignore (str "# "block-name))
                                          (str "\n"pattern)
                                          (string/after-first-occurence gitignore (str "# "block-name)))]
                          (write-gitignore! gitignore))
                     :else
                     (let [gitignore (str (string/ends-with! gitignore "\n")
                                          (str "\n# "block-name"\n"pattern"\n"))]
                          (write-gitignore! gitignore)))))))
