
(ns git-handler.submodule-updater.reader.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (map)
; {"submodules/my-repository" [["author/another-repository"
;                               "git@github.com:author/another-repository"
;                               "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"]]} ; <- commit SHA
(def INNER-DEPENDENCIES (atom []))
