# Gitlet
A version-control system that mimics some of the basic features of the popular system Git. A version-control system is essentially a backup system for related collections of files. The main functionality that Gitlet supports is:
1. Saving the contents of entire directories of files - committing
    1. saved contents themselves are called commits.
2. Restoring a version of one or more files or entire commits - checking out those files or that commit.
3. Viewing the history of your backups - log.
4. Maintaining related sequences of commits - branches.
5. Merging changes made in one branch into another.

The point of a version-control system is to help you when creating complicated (or even not-so-complicated) projects, or when collaborating with others on a project. You save versions of the project periodically. If at some later point in time you accidentally mess up your code, then you can restore your source to a previously committed version (without losing any of the changes you made since then). If your collaborators make changes embodied in a commit, you can incorporate (merge) these changes into your own version.
IMPORTANT: CAN ONLY COMMIT OR ADD ONE FILE AT A TIME NOT FOLDERS

# Internal Structures
blobs: Essentially the contents of files.
trees: Directory structures mapping names to references to blobs and other trees (subdirectories).
In order for Gitlet to work, it will need a place to store old copies of files and other metadata. All of this stuff must be stored in a directory called .gitlet, just as this information is stored in directory .git for the real git system (files with a . in front are hidden files. You will not be able to see them by default on most operating systems. On Unix, the command ls -a will show them.) A Gitlet system is considered "initialized" in a particular location if it has a .gitlet directory there. Most Gitlet commands (except for the init command) only need to work when used from a directory where a Gitlet system has been initialized—i.e. a directory that has a .gitlet directory. The files that aren't in your .gitlet directory (which are copies of files from the repository that you are using and editing, as well as files you plan to add to the repository) are referred to as the files in your working directory.

# Commands
1. Init
    1. Usage: java gitlet.Main init
    2. Description: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit (just like that, with no punctuation). It will have a single branch: master, which initially points to this initial commit, and master will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates (this is called "The (Unix) Epoch", represented internally by the time 0.) Since the initial commit in all repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit (they will all have the same UID) and all commits in all repositories will trace back to it.
2. add
    1. Usage: java gitlet.Main add [file name]
    2. Description: Adds a copy of the file as it currently exists to the staging area (see the description of the commit command). For this reason, adding a file is also called staging the file for addition. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area should be somewhere in .gitlet. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back). The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.
3. commit 
    1. Usage: java gitlet.Main commit [message]
    2. Description: Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files. By default, each commit's snapshot of files will be exactly the same as its parent commit's snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren't tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).
4. rm
    1. Usage: java gitlet.Main rm [file name]
    2. Description: Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).
5. log 
    1. Usage: java gitlet.Main log
    2. Description: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. (In regular Git, this is what you get with git log --first-parent). This set of commit nodes is called the commit's history. For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message.
6. global-log
    1. Usage: java gitlet.Main global-log
    2. Description: Like log, except displays information about all commits ever made. The order of the commits does not matter.
7. find
    1. Usage: java gitlet.Main find [commit message]
    2. Description: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the commit command below.
8. status
    1. Usage: java gitlet.Main status
    2. Description: Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal. 
9. checkout
Checkout is a kind of general command that can do a few different things depending on what its arguments are. There are 3 possible use cases. In each section below, you'll see 3 bullet points. Each corresponds to the respective usage of checkout.
    1. Usages:
        1. java gitlet.Main checkout -- [file name]
        2. java gitlet.Main checkout [commit id] -- [file name]
        3. java gitlet.Main checkout [branch name]
    2. Descriptions:
        1. Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
        2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
        3. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).
10. branch
    1. Usage: java gitlet.Main branch [branch name]
    2. Description: Creates a new branch with the given name, and points it at the current head node. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch to the newly created branch (just as in real Git). Before you ever call branch, your code should be running with a default branch called "master".
11. rm-branch
    1. Usage: java gitlet.Main rm-branch [branch name]
    2. Description: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
12. reset
    1. Usage: java gitlet.Main reset [commit id]
    2. Description: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch's head to that commit node. See the intro for an example of what happens to the head pointer after using reset. The [commit id] may be abbreviated as for checkout. The staging area is cleared. The command is essentially checkout of an arbitrary commit that also changes the current branch head.
13. merge
    1. Usage: java gitlet.Main merge [branch name]
    2. Description: Merges files from the given branch into the current branch.
# Additional details about merge
The split point is a latest common ancestor of the current and given branch heads:
    1. A common ancestor is a commit to which there is a path (of 0 or more parent pointers) from both branch heads.
    2. A latest common ancestor is a common ancestor that is not an ancestor of any other common ancestor. For example, although the leftmost commit in the diagram above is a common ancestor of master and branch, it is also an ancestor of the commit immediately to its right, so it is not a latest common ancestor. If the split point is the same commit as the given branch, then we do nothing; the merge is complete, and the operation ends with the message Given branch is an ancestor of the current branch.
    3. If the split point is the current branch, then the effect is to check out the given branch, and the operation ends after printing the message Current branch fast-forwarded. Otherwise, we continue with the steps below.
    
1. Any files that have been modified in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch (checked out from the commit at the front of the given branch). These files should then all be automatically staged. To clarify, if a file is "modified in the given branch since the split point" this means the version of the file as it exists in the commit at the front of the given branch has different content from the version of the file at the split point.
2. Any files that have been modified in the current branch but not in the given branch since the split point should stay as they are.
3. Any files that have been modified in both the current and given branch in the same way (i.e., both to files with the same content or both removed) are left unchanged by the merge. If a file is removed in both, but a file of that name is present in the working directory that file is not removed from the working directory (but it continues to be absent—not staged—in the merge).
4. Any files that were not present at the split point and are present only in the current branch should remain as they are.
5. Any files that were not present at the split point and are present only in the given branch should be checked out and staged.
6. Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed (and untracked).
7. Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.
8. Any files modified in different ways in the current and given branches are in conflict. "Modified in different ways" can mean that the contents of both are changed and different from other, or the contents of one are changed and the other file is deleted, or the file was absent at the split point and has different contents in the given and current branches.
9. Once files have been updated according to the above, and the split point was not the current branch or the given branch, merge automatically commits with the log message Merged [given branch name] into [current branch name]. Then, if the merge encountered a conflict, print the message Encountered a merge conflict. on the terminal (not the log). Merge commits differ from other commits: they record as parents both the head of the current branch (called the first parent) and the head of the branch given on the command line to be merged in.
