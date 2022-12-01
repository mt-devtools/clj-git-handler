
# <strong>git.api</strong> namespace
<p>Documentation of the <strong>git/api.clj</strong> file</p>

<strong>[README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > git.api</strong>



### get-gitignore

```
@usage
(get-gitignore)
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn get-gitignore
  []
  (io/read-file ".gitignore"))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [git.api :as git :refer [get-gitignore]]))

(git/get-gitignore)
(get-gitignore)
```

</details>

---

### ignore!

```
@param (string) pattern
@param (string)(opt) block-name
 Default: "git-api"
```

```
@usage
(ignore! "my-file.ext")
```

```
@usage
(ignore! "my-file.ext" "My ignored files")
```

```
@example
(ignore! "my-file.ext" "My ignored files")
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
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [git.api :as git :refer [ignore!]]))

(git/ignore! ...)
(ignore!     ...)
```

</details>

---

### ignored?

```
@param (string) pattern
```

```
@usage
(ignored? "my-file.ext")
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn ignored?
  [pattern]
  (string/contains-part? (get-gitignore)
                         (str "\n"pattern"\n")))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [git.api :as git :refer [ignored?]]))

(git/ignored? ...)
(ignored?     ...)
```

</details>
