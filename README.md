
# clj-git-handler

### Overview

The <strong>clj-git-handler</strong> is a simple Clojure tool for managing Git.

### deps.edn

```
{:deps {bithandshake/clj-git-handler {:git/url "https://github.com/bithandshake/clj-git-handler"
                                      :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/clj-git-handler/tree/release).

### Documentation

The <strong>clj-git-handler</strong> functional documentation is [available here](documentation/COVER.md).

### Changelog

You can track the changes of the <strong>clj-git-handler</strong> library [here](CHANGES.md).

# Usage

> Some parameters of the following functions and some further functions are not discussed in this file.
  To learn more about the available functionality, check out the [functional documentation](documentation/COVER.md)!

### Index

- [How to add pattern to the `.gitignore` file?](#how-to-add-pattern-to-the-gitignore-file)

- [How to check whether a pattern is added to the `.gitignore` file?](#how-to-check-whether-a-pattern-is-added-to-the-gitignore-file)

- [How to update submodule dependencies?](#how-to-update-submodule-dependencies)

### How to add pattern to the `.gitignore` file?

The [`git-handler.api/ignore!`](documentation/clj/git/API.md/#ignore) function adds a pattern
the `.gitignore` file.

- The return value will be the modified `.gitignore` file's content.

```
(ignore! "my-file.txt")
```

You can specify where is your `.gitignore` file placed.

```
(ignore! "my-file.txt" {:filepath "my-directory/.gitignore"})
```

You can group the added patterns in the `.gitignore` file.

```
(ignore! "my-file.txt" {:group "My ignored files"})
```

### How to check whether a pattern is added to the `.gitignore` file?

The [`git-handler.api/ignored?`](documentation/clj/git/API.md/#ignored) function checks whether
a pattern is added to the `.gitignore` file.

```
(ignored? "my-file.txt")
```

### How to update submodule dependencies?

> This function only operates in Clojure projects using deps.edn to manage their dependencies!

The [`git-handler.api/update-submodule-dependencies!`](documentation/clj/git/API.md/#update-submodule-dependencies)
function detects git submodules within the specified folders and builds a dependency tree
of all found submodules and their relations found in the deps.edn files.
After the dependency tree built, the function iterates over the detected submodules
to push their changes to the specified branch. After every successfully pushing
it takes the returned commit SHA and updates the other submodules's deps.edn files
whit it (only if they depend on the recently pushed submodule).

By using the default options, this function detects submodules in the `submodules` folder,
pushes changes to `main` branches and uses timestamps as commit messages.

```
(update-submodule-dependencies!)
```

To specify which folders contains submodules in your project, use the `:source-paths`
property.

```
(update-submodule-dependencies! {:source-paths ["my-submodules"])
```

To set the default branch or default commit message generator function for all
submodules, use the `:default` property.

```
(defn my-commit-message-f [latest-commit-message] ...)
(update-submodule-dependencies! {:default {:branch "my-branch"
                                           :commit-message-f my-commit-message-f}})
```

To set a specific branch or specific commit message generator function for
specific submodules, use the `:config` property.

```
(defn my-commit-message-f [latest-commit-message] ...)
(update-submodule-dependencies! {:config {"author/my-repository" {:branch "my-branch"
                                                                  :commit-message-f my-commit-message-f}}})
```
