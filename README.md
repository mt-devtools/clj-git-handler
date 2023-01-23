
# git-api

### Overview

The <strong>git-api</strong> is a simple Clojure tool for managing Git.

### deps.edn

```
{:deps {bithandshake/git-api {:git/url "https://github.com/bithandshake/git-api"
                              :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/git-api/tree/release).

### Documentation

The <strong>git-api</strong> functional documentation is [available here](documentation/COVER.md).

### Changelog

You can track the changes of the <strong>git-api</strong> library [here](CHANGES.md).

### Index

- [How to add pattern to the `.gitignore` file?](#how-to-add-pattern-to-the-gitignore-file)

- [How to check whether a pattern is added to the `.gitignore` file?](#how-to-check-whether-a-pattern-is-added-to-the-gitignore-file)

- [How update submodule dependencies?](#how-to-update-submodule-dependencies)

# Usage

### How to add pattern to the `.gitignore` file?

The [`git.api/ignore!`](documentation/clj/git/API.md/#ignore) function adds a pattern
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

The [`git.api/ignored?`](documentation/clj/git/API.md/#ignored) function checks whether
a pattern is added to the `.gitignore` file.

```
(ignored? "my-file.txt")
```

### How to update submodule dependencies?

> This function only operates in Clojure projects using deps.edn to manage their dependencies!

The [`git.api/update-submodule-dependencies!`](documentation/clj/git/API.md/#update-submodule-dependencies)
function detects git submodules in the specified folders and builds a dependency tree.
After the dependency tree built, the function iterates over the detected submodules
and pushes the changes to the specified branches. After every successful pushing
it takes the returned commit SHA and updates the other submodules's deps.edn files
whit it (if they depend on the pushed submodule).

By using default options, the function detects submodules in the `submodules` folder,
pushes changes to `main` branches and uses timestamps as commit messages.

```
(update-submodule-dependencies!)
```

To specify which folders contains submodules in your project, use the `:source-paths`
property.

```
(update-submodule-dependencies! {:source-paths ["my-submodules"])
```

To set the default branch or commit message generator function for all submodules,
use the `:default` property.

```
(defn my-commit-message-f [latest-commit-message] ...)
(update-submodule-dependencies! {:default {:branch "my-branch"
                                           :commit-message-f my-commit-message-f}})
```

To set a specific branch and commit message generator function for each submodule,
use the `:config` property.

```
(defn my-commit-message-f [latest-commit-message] ...)
(update-submodule-dependencies! {:config {"author/my-repository" {:branch "my-branch"
                                                                  :commit-message-f my-commit-message-f}}})
```
