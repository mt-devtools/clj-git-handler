
(ns git-handler.gitignore.env
    (:require [fruits.string.api            :as string]
              [git-handler.gitignore.config :as gitignore.config]
              [io.api                       :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-gitignore
  ; @description
  ; Returns the content of the '.gitignore' file.
  ;
  ; @param (map)(opt) options
  ; {:filepath (string)(opt)
  ;   Default: ".gitignore"}
  ;
  ; @usage
  ; (get-gitignore)
  ;
  ; @usage
  ; (get-gitignore {:filepath "my-directory/.gitignore"})
  ;
  ; @return (string)
  ([]
   (get-gitignore {}))

  ([{:keys [filepath] :or {filepath gitignore.config/DEFAULT-GITIGNORE-FILEPATH}}]
   (io/read-file filepath)))

(defn ignored?
  ; @description
  ; Checks whether the given pattern is added to the '.gitignore' file.
  ;
  ; @param (string) pattern
  ; @param (map)(opt) options
  ; {:filepath (string)(opt)
  ;   Default: ".gitignore"}
  ;
  ; @usage
  ; (ignored? "my-file.ext")
  ;
  ; @usage
  ; (ignored? "my-file.ext" {:filepath "my-directory/.gitignore"})
  ;
  ; @return (boolean)
  ([pattern]
   (ignored? pattern {}))

  ([pattern options]
   (string/contains-part? (get-gitignore options)
                          (str "\n"pattern"\n"))))
