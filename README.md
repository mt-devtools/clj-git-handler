
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

# Usage

### How to add pattern to the `.gitignore` file?

The [`git.api/ignore!`](documentation/clj/git/API.md/#ignore) function adds a pattern
the `.gitignore` file.

- The return value will be the modified `.gitignore` file's content.

```
(ignore! "my-file.txt")
```

You can specify where you `.gitignore` file is placed.

```
(ignore! "my-file.txt" {:filepath "my-directory/.gitignore"})
```

You can group your added patterns in the `.gitignore` file.

```
(ignore! "my-file.txt" {:group "My ignored files"})
```

### How to check whether a pattern is added to the `.gitignore` file?

The [`git.api/ignored?`](documentation/clj/git/API.md/#ignored) function checks whether
a pattern is added to the `.gitignore` file.

```
(ignored? "my-file.txt")
```
