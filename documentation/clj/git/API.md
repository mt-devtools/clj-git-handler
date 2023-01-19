
# git.api Clojure namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > git.api

### Index

- [get-gitignore](#get-gitignore)

- [ignore!](#ignore)

- [ignored?](#ignored)

- [update-submodule-dependencies!](#update-submodule-dependencies)

### get-gitignore

```
@description
Reads and returns the content of the .gitignore file.
```

```
@param (map)(opt) options
{:filepath (string)(opt)
  Default: ".gitignore"}
```

```
@usage
(get-gitignore)
```

```
@usage
(get-gitignore {:filepath "my-directory/.gitignore"})
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn get-gitignore
  ([]
   (get-gitignore {}))

  ([{:keys [filepath] :or {filepath gitignore.config/DEFAULT-GITIGNORE-FILEPATH}}]
   (io/read-file filepath)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [git.api :refer [get-gitignore]]))

(git.api/get-gitignore ...)
(get-gitignore         ...)
```

</details>

---

### ignore!

```
@description
Writes the given pattern to the .gitignore file.
You can specify a group to the added pattern by passing the :group property.
```

```
@param (string) pattern
@param (map)(opt) options
{:group (string)(opt)
  Default: "git-api"
 :filepath (string)(opt)
  Default: ".gitignore"}
```

```
@usage
(ignore! "my-file.ext")
```

```
@usage
(ignore! "my-file.ext" {:group "My ignored files"})
```

```
@usage
(ignore! "my-file.ext" {:filepath "my-directory/.gitignore"})
```

```
@example
(ignore! "my-file.ext" {:group "My ignored files"})
=>
"\n# My ignored files\nmy-file.ext\n"
```

```
@return (string)
Returns with the updated .gitignore file's content.
```

<details>
<summary>Source code</summary>

```
(defn ignore!
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
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [git.api :refer [ignore!]]))

(git.api/ignore! ...)
(ignore!         ...)
```

</details>

---

### ignored?

```
@description
Checks whether the given pattern already ignored in the .gitignore file.
```

```
@param (string) pattern
@param (map)(opt) options
{:filepath (string)(opt)
  Default: ".gitignore"}
```

```
@usage
(ignored? "my-file.ext")
```

```
@usage
(ignored? "my-file.ext" {:filepath "my-directory/.gitignore"})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn ignored?
  ([pattern]
   (ignored? pattern {}))

  ([pattern options]
   (string/contains-part? (get-gitignore options)
                          (str "\n"pattern"\n"))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [git.api :refer [ignored?]]))

(git.api/ignored? ...)
(ignored?         ...)
```

</details>

---

### update-submodule-dependencies!

```
@description
Pushes the changed submodules and updates the other submodules' deps.edn
files with the returned commit SHA.
You can specify which folders in your project contains submodules,
by default the function updates submodules in the "/submodules" folder.
By passing the :default property you can use your own commit message generator
function and change the default branch, by default the function uses the current
timestamp as commit messages and the "main" branch to push commits.
In addition you can specify these settings for each submodule by passing
the :config property.
```

```
@param (map)(opt) options
{:config (map)(opt)
  {"author/my-repository" {:commit-message-f (function)(opt)
                           :branch (string)(opt)}}
 :default (map)(opt)
  {:commit-message-f (function)(opt)
    Default time.api/timestamp-string}
   :branch (string)(opt)
    Default: "main"
 :source-paths (vector)(opt)
  Default: ["submodules"]}
```

```
@usage
(update-submodule-dependencies!)
```

```
@usage
(update-submodule-dependencies! {:source-paths ["my-submodules"]})
```

```
@usage
(defn my-commit-message-f [latest-commit-message] ...)
(update-submodule-dependencies! {:default {:branch "my-branch"
                                           :commit-message-f my-commit-message-f}})
```

```
@usage
(defn my-commit-message-f [latest-commit-message] ...)
(update-submodule-dependencies! {:config {"author/my-repository" {:branch "my-branch"
                                                                  :commit-message-f my-commit-message-f}}})
```

<details>
<summary>Source code</summary>

```
(defn update-submodule-dependencies!
  ([]
   (update-submodule-dependencies! {}))

  ([options]
   (try (do (detector.side-effects/detect-submodules!    options)
            (reader.side-effects/read-submodules!        options)
            (builder.side-effects/build-dependency-tree! options)
            (updater.side-effects/update-submodules!     options))
        (catch Exception e nil))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [git.api :refer [update-submodule-dependencies!]]))

(git.api/update-submodule-dependencies! ...)
(update-submodule-dependencies!         ...)
```

</details>

---

This documentation is generated by the [docs-api](https://github.com/bithandshake/docs-api) engine

