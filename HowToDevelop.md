This document describes how to get chilay code, work on it, build, and deploy to the project maven repository. You should have the latest versions of [Mercurial](http://mercurial.selenic.com/) and [Maven](http://maven.apache.org/download.html) installed on your system.

# Getting the code #

Get the latest sources using the command below.

```
hg clone https://USERNAME@code.google.com/p/chilay/ chilay
```

`USERNAME` is your Google ID.

This will create the `chilay` directory with the project code inside. You will also see `.hg` folder in `chilay`. Mercurial uses this folder to work. You don't need to bother with this folder with one exception. The file `hgrc` under `.hg` contains user settings. You need to **add** following information to this file.

```
[ui]
username = NAME SURNAME <USERNAME@gmail.com>

[auth]
google.prefix = code.google.com
google.username = USERNAME
google.password = PASSWORD
google.schemes = https
```

Capitalized parts are user specific information.

`NAME SURNAME` is your name and surname.

`USERNAME` is your Google ID.

`PASSWORD` is a password that Google decides for you. You can see it at https://code.google.com/hosting/settings, assuming that you already logged-in.

# Working on the code #

Use your favourite IDE to modify chilay code. If you are using IntelliJ IDEA, you need to say _Import Project..._ and select chilay directory. Do **not** add newly created project file (.iml for IntelliJ) to the Mercurial (IDEA asks this). Because each developer can have their own project settings. You don't want to share this file.

You will find the main Java classes under `src/main/java`, and test classes under `src/test/java`. This is the usual Maven project structure.

# Building project #

Assuming you are on a console, and in folder `chilay`. Following are useful commands.

Get latest changes:
```
hg pull
hg update
```

Compile the project:
```
mvn compile
```

Install project to local maven repository:
```
mvn install
```

Commit and push to repository
```
hg commit
hg push
```
Note that `hg commit` will open up a text window for you to enter a commit message. If you don't want that use the below command.
```
hg commit -m "Commit message here."
```

Create a jar file with all dependencies under the `target` directory:
```
mvn assembly:single
```

Note that most of these commands are also available within IntelliJ IDEA. Feel free to discover them.

# Deploying the project #

We use a special Mercurial repository, hosted in this Google code project, named `maven-repo`, for distributing builds of chilay.

Unfortunately, Maven cannot directly deploy to this repository, but we need to deploy to a local folder, then push to the repository.

If you are deploying first time, then start with getting a copy of the repository. While in the project directory `chilay`, run the below command.

```
hg clone https://USERNAME@code.google.com/p/chilay.maven-repo/ maven-repo
```

This will create `maven-repo` folder under `chilay`. Then go and modify `chilay/maven-repo/.hg/hgrc` exactly as you did before.

This was a one time job. After that whenever you will deploy, you only need to update already existing local copy of repository with the below commands (you are in `maven-repo` directory).

```
hg pull
hg update
```

To start deploying, **go back to `chilay` directory** and run the below command.
```
mvn deploy
```

This will create necessary files to distribute latest chilay jar under `maven-repo` folder. The last step is to commit and push this deploy to Mercurial (you are in `maven-repo` folder again).
```
hg addremove
hg commit
hg push
```

The `addremove` command is for adding new files and removing older files that the deploy process may have deleted.