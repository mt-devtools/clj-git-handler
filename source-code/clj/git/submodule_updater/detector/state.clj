
(ns git.submodule-updater.detector.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (map)
; {"submodules/my-repository" {:git-url "git@github.com:author/my-repository.git"
;                              :repository-name "author/my-repository"}}
(def DETECTED-SUBMODULES (atom {}))
