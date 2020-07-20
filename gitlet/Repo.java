package gitlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/** A Repo class for executing different commands.
 *  @author Samarth Bhutani
 */
public class Repo {

    /** The current working directory. */
    private static File _cwd;
    /** The repository set up in the current working directory. */
    private static File _repo;
    /** Staging area folder with sub-files for adding and removing files. */
    private static File _stagingArea;
    /** The file in staging area which contains a StagedObject
     * the StagedObject keeps track of the files to be added in the next commit. */
    private static File _add;
    /** The file in staging area which contains a StagedObject
     * the StagedObject keeps track of the files to be removed in the next commit. */
    private static File _remove;
    /** Folder which contains all the commits made so far.
     * This only contains the initial commit when a repository is created. */
    private static File _commits;
    /** Folder which contains all the blobs (serialized version of the files). */
    private static File _blobs;
    /** Folder which contains reference to the heads of the different branches. */
    private static File _branches;
    /** File whose content is an address to the current branch within the branches folder.
     * This is the master branch when a repository is created. */
    private static File _HEAD;
    /** The master branch within the branches folder which is created when a new repository is created.
     * It's content is the sha1 name of the latest commit. */
    private static File _master;

    /** Initiate the general files which are present in a GIT repository
     * but does not create any of those files or folder. */
    public Repo() {
        _cwd = new File(System.getProperty("user.dir"));
        _repo = Utils.join(_cwd, ".gitlet");
        _stagingArea = Utils.join(_repo, "Staging Area");
        _commits = Utils.join(_repo, "Commits");
        _blobs = Utils.join(_repo, "Blobs");
        _branches = Utils.join(_repo, "Branches");
        _HEAD = Utils.join(_repo, "HEAD");
        _master = Utils.join(_branches, "master");
        _add = Utils.join(_stagingArea, "Add");
        _remove = Utils.join(_stagingArea, "Remove");
    }

    /** Error checker which ensures that certain functions are not called,
     * without an initialized repo and the correct number of operands are provided
     */
    public void error_check(int expected_operand, int given_operand) {
        if (given_operand != expected_operand) {
            System.out.println("Incorrect operands.");
        }
        if (!_repo.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
    }

    /** Creates a GIT repository and the required files in the current working directory.
     * Informs the user if a GIT repository already exists in that location. */
    public static void init() {
        new Repo();
        if (_repo.exists()) {
            System.out.println("Gitlet version-control system"
                    + " already exists in the current directory.");
            return;
        }
        _repo.mkdir();
        _commits.mkdir();
        _blobs.mkdir();
        _branches.mkdir();
        _stagingArea.mkdir();

        Commit initial = new Commit("initial commit", null);
        String address = Utils.sha1(Utils.serialize(initial));
        File initial_file = Utils.join(_commits, address);
        try {
            initial_file.createNewFile();
            _master.createNewFile();
            _HEAD.createNewFile();
            _add.createNewFile();
            _remove.createNewFile();
        } catch (IOException exp) {
            throw new IllegalArgumentException();
        }
        Utils.writeObject(initial_file, initial);
        Utils.writeContents(_master, address);
        Utils.writeContents(_HEAD, _master.toString());
        Utils.writeObject(_add, new StagedObject());
        Utils.writeObject(_remove, new StagedObject());
    }

    /** Adds a given file the staging area so that it is saved in the next commit
     * Reads the StagedObject from the _add file in _stagingArea folder and adds the respective file to it.
     * Special case: if a file is staged for removal,
     *  then reads the StagedObject from the _remove file in _stagingArea folder
     *  and removes it from the list so that it is not staged to be removed anymore.
     *  (does not stage it for adding in this case)
     * @param name : name of the file which is to be added */
    public void add(String name) {
        File fileToAdd = Utils.join(_cwd, name);
        if (!fileToAdd.isFile()) {
            System.out.println("File does not exist.");
        } else {
            StagedObject toBeAdded = Utils.readObject(_add, StagedObject.class);
            StagedObject toBeRemoved = Utils.readObject(_remove, StagedObject.class);
            if (toBeRemoved.getStagedItems().remove(name) != null) {
                Utils.writeObject(_remove, toBeRemoved);
            } else {
                toBeAdded.stageObject(name);
                Utils.writeObject(_add, toBeAdded);
            }
        }
    }

    /** Makes a new commit.
     * Takes the last commit in the current branch and creates a shallow copy.
     * Then reads StagedObject from both _add and _remove files
     *  Adds and removes the respective files from the commit and finally saves it as a new commit
     *  in the commits folder.
     * @param message : the message associated with the new commit. */
    public void commit(String message) {

        if (message.compareTo("") == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }
        boolean errocheck = true;

        File current_branch = new File(Utils.readContentsAsString(_HEAD));
        String commit_name = Utils.readContentsAsString(current_branch);
        File commit_file = Utils.join(_commits, commit_name);
        Commit commit_old = Utils.readObject(commit_file, Commit.class);
        Commit commit_new = new Commit(commit_old, message, commit_name);

        StagedObject toBeAdded = Utils.readObject(_add, StagedObject.class);
        StagedObject toBeRemoved = Utils.readObject(_remove, StagedObject.class);

        if (toBeAdded.getStagedItems().size() != 0) {
            commit_new.getRefs().putAll(toBeAdded.getStagedItems());
            toBeAdded.getStagedItems().clear();
            Utils.writeObject(_add, toBeAdded);
            errocheck = false;
        }
        if (toBeRemoved.getStagedItems().size() != 0) {
            for (String files : toBeRemoved.getStagedItems().keySet()) {
                commit_new.getRefs().remove(files);
            }
            toBeRemoved.getStagedItems().clear();
            Utils.writeObject(_remove, toBeRemoved);
            errocheck = false;
        }
        if (errocheck) {
            System.out.println("No changes added to the commit.");
            return;
        }

        String address = Utils.sha1(Utils.serialize(commit_new));
        File commit_new_file = Utils.join(_commits, address);
        try {
            commit_new_file.createNewFile();
        } catch (IOException exp) {
            throw new IllegalArgumentException();
        }
        Utils.writeObject(commit_new_file, commit_new);
        Utils.writeContents(current_branch, address);
    }

    /** It can do 2 different tasks based on the input
     * checks out the file in the current commit
     * or if the commit id is given, check out the version of the file in that commit.
     * @param inp input arguments which can contain diffent values an can be of different lengths*/
    public void file_checkout(String[] inp) {

        File current_branch = new File(Utils.readContentsAsString(_HEAD));
        String commit_name = Utils.readContentsAsString(current_branch);
        String file_name = null;
        if (inp.length == 3) {
            file_name = inp[2];
            if (inp[1].compareTo("--") != 0) {
                System.out.println("Incorrect operands.");
                return;
            }
        }
        if (inp.length == 4) {
            file_name = inp[3];
            commit_name = inp[1];
            if (inp[2].compareTo("--") != 0) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (commit_name.length() < 10) {
                for (File f : _commits.listFiles()) {
                    if (f.getName().substring(0,commit_name.length()).compareTo(commit_name) == 0) {
                        commit_name = f.getName();
                    } else {
                        System.out.println("No commit with that id exists.");
                        return;
                    }
                }
            }
        }
        File commit_file = Utils.join(_commits, commit_name);
        if (!commit_file.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit_old = Utils.readObject(commit_file, Commit.class);
        File k = Utils.join(_cwd, file_name);
        if (!commit_old.getRefs().containsKey(file_name)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Utils.writeContents(k, Blob.getBlob(commit_old.getRefs().get(file_name)).getContent());
    }

    /** Checks out all the files in the latest commit of the given branch.
     * Checks all cases including conflicts and situation where files are untracked.
     * @param inp the input arguments which contains the branch name.
     */
    public void branch_checkout(String[] inp) {

        File current_branch = new File(Utils.readContentsAsString(_HEAD));
        String commit_name = Utils.readContentsAsString(current_branch);
        String branch_name = inp[1];
        if (current_branch.getName().compareTo(branch_name) == 0) {
            System.out.println(" No need to checkout the current branch. ");
            return;
        }
        File new_branch = Utils.join(_branches, branch_name);
        if (!new_branch.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String commit_name_new = Utils.readContentsAsString(new_branch);
        File commit_file_new = Utils.join(_commits, commit_name_new);
        File commit_file_old = Utils.join(_commits, commit_name);
        Commit commit_old = Utils.readObject(commit_file_old, Commit.class);
        Commit commit_new = Utils.readObject(commit_file_new, Commit.class);
        ArrayList<File> filtracker1 = new ArrayList<>();
        ArrayList<File> filtracker2 = new ArrayList<>();
        for (File f : _cwd.listFiles()) {
            if (!f.isHidden()) {
                filtracker1.add(f);
                filtracker2.add(f);
            }
        }
        for (String s : commit_old.getRefs().keySet()) {
            File temp = Utils.join(_cwd, s);
            filtracker1.remove(temp);
        }
        if (filtracker1.size() != 0) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        }
        for (File f : filtracker2) {
            f.delete();
        }
        for (String s : commit_new.getRefs().keySet()) {
            File k = Utils.join(_cwd, s);
            if (!k.exists()) {
                try {
                    k.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(k,
                    Blob.getBlob(commit_new.getRefs().get(s)).getContent());
        }
        StagedObject toBeAdded = Utils.readObject(_add, StagedObject.class);
        StagedObject toBeRemoved = Utils.readObject(_remove,
                StagedObject.class);
        toBeAdded.getStagedItems().clear();
        toBeRemoved.getStagedItems().clear();
        Utils.writeObject(_add, toBeAdded);
        Utils.writeObject(_remove, toBeRemoved);
        Utils.writeContents(_HEAD, new_branch.toString());
    }

    /** Prints out the log from the current branch */
    public void log() {
        File current_branch = new File(Utils.readContentsAsString(_HEAD));
        String commit_name = Utils.readContentsAsString(current_branch);
        File commit_file;
        Commit ans;
        do {
            commit_file = Utils.join(_commits, commit_name);
            ans = Utils.readObject(commit_file, Commit.class);
            System.out.println("=== ");
            System.out.println("commit " + commit_name);
            System.out.println(ans);
            System.out.println();
            commit_name = ans.getParent();
        } while (commit_name != null);
    }

    /** Stages a file for removal,
     * if the file was staged for addition, removes it form the addition staging area.
     * (Also checks if there is no reason to remove a file then informs the user)
     * @param name name of the file to be removed  */
    public void rm(String name) {
        boolean error_check = false;
        StagedObject toBeAdded = Utils.readObject(_add, StagedObject.class);
        StagedObject toBeRemoved = Utils.readObject(_remove, StagedObject.class);
        if (toBeAdded.getStagedItems().remove(name) != null) {
            Utils.writeObject(_add, toBeAdded);
            error_check = true;
        }
        File current_branch = new File(Utils.readContentsAsString(_HEAD));
        String commit_name = Utils.readContentsAsString(current_branch);
        File commit_file = Utils.join(_commits, commit_name);
        Commit commit_new = Utils.readObject(commit_file, Commit.class);
        if (commit_new.getRefs().containsKey(name)) {
            toBeRemoved.removeObject(name);
            Utils.writeObject(_remove, toBeRemoved);
            if (Utils.join(_cwd, name).exists()) {
                Utils.join(_cwd, name).delete();
            }
            error_check = true;
        }
        if (!error_check) {
            System.out.println("No reason to remove the file.");
        }
    }

    /** Prints out a log of all the commits ever made, including the ones from different branches */
    public void global_log() {
        for (File f : _commits.listFiles()) {
            System.out.println("=== ");
            System.out.println("commit " + f.getName());
            System.out.println(Utils.readObject(f, Commit.class));
            System.out.println();
        }
    }

    /** Tries to find a commit with the given message,
     *  and prints out the details if it finds one.
     * @param message the message associated with the commit one is looking for*/
    public void find(String message) {
        Commit temp;
        boolean error = true;
        for (File f : _commits.listFiles()) {
            temp = Utils.readObject(f, Commit.class);
            if (temp.getMessage().compareTo(message) == 0) {
                System.out.println(f.getName());
                error = false;
            }
        }
        if (error) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Shows the current status of the Git repository showing:
     * files that are staged for addition
     * files that are staged for removal
     * the different branches
     * Does not show modified files not staged for commit.
     * Does not show untracked files. */
    public void status() {
        ArrayList<String> branches = new ArrayList<>();
        ArrayList<String> staged_files = new ArrayList<>();
        ArrayList<String> removed_files = new ArrayList<>();
        String current_branch = Utils.readContentsAsString(_HEAD);
        StagedObject toBeAdded = Utils.readObject(_add, StagedObject.class);
        StagedObject toBeRemoved = Utils.readObject(_remove, StagedObject.class);

        staged_files.addAll(toBeAdded.getStagedItems().keySet());
        removed_files.addAll(toBeRemoved.getStagedItems().keySet());
        for (File f : _branches.listFiles()) {
            if (current_branch.contains(f.getName())) {
                branches.add("*" + f.getName());
            } else {
                branches.add(f.getName());
            }
        }
        Collections.sort(branches);
        Collections.sort(staged_files);
        Collections.sort(removed_files);
        System.out.println("=== Branches ===");
        for (String s : branches) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String s : staged_files) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String s : removed_files) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Creates a new branch if possible.
     * @param new_branch name of the new branch to be created*/
    public void branch(String new_branch) {

        File new_branch_file = Utils.join(_branches, new_branch);
        if (new_branch_file.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        File current_branch = new File(Utils.readContentsAsString(_HEAD));
        String commit_name = Utils.readContentsAsString(current_branch);
        try {
            new_branch_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeContents(new_branch_file, commit_name);
    }

    /** Deletes the given branch if possible.
     * @param branch_name name of branch to be deleted*/
    public void rmbranch(String branch_name) {
        File branch_file = Utils.join(_branches, branch_name);
        if (!branch_file.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String current_branch = Utils.readContentsAsString(_HEAD);
        if (current_branch.compareTo(branch_name) == 0) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branch_file.delete();
    }

    /** Resets the current directory into the same state as the given commit.
     * Informs the user if there is an untracked file in the way before conducting the process
     * @param  commit_new_name file*/
    public void reset(String commit_new_name) {
        File commit_new_file = Utils.join(_commits, commit_new_name);
        if (!commit_new_file.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File current_branch = new File(Utils.readContentsAsString(_HEAD));
        String commit_old_name = Utils.readContentsAsString(current_branch);
        File commit_old_file = Utils.join(_commits, commit_old_name);
        Commit commit_old = Utils.readObject(commit_old_file, Commit.class);
        Commit commit_new = Utils.readObject(commit_new_file, Commit.class);

        ArrayList<File> filtracker1 = new ArrayList<>();
        ArrayList<File> filtracker2 = new ArrayList<>();
        for (File f : _cwd.listFiles()) {
            if (!f.isHidden()) {
                filtracker1.add(f);
                filtracker2.add(f);
            }
        }
        for (String s : commit_old.getRefs().keySet()) {
            File temp = Utils.join(_cwd, s);
            filtracker1.remove(temp);
        }
        for (File f: filtracker1) {
            if (commit_new.getRefs().keySet().contains(f.getName())) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        StagedObject toBeAdded = Utils.readObject(_add, StagedObject.class);
        StagedObject toBeRemoved = Utils.readObject(_remove, StagedObject.class);
        toBeAdded.getStagedItems().clear();
        toBeRemoved.getStagedItems().clear();
        Utils.writeObject(_add, toBeAdded);
        Utils.writeObject(_remove, toBeRemoved);

        for (String s : commit_new.getRefs().keySet()) {
            File k = Utils.join(_cwd, s);
            if (!k.exists()) {
                try {
                    k.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                filtracker2.remove(k);
            }
            Utils.writeContents(k,
                    Blob.getBlob(commit_new.getRefs().get(s)).getContent());
        }
        for (File f : filtracker2) {
            f.delete();
        }
        Utils.writeContents(current_branch, commit_new_name);
    }

    /** Merges the current branch with the given branch.
     * Does so by finding the split point between the two branches and rearnaging files accordingly.
     * Current - current branch or commit
     * Given - Given branch or commit with which it has to be merged with.
     * Split - The split commit betweent the two commits to be merged.
     * @param given_branch_name name of the branch with which one has to merge. */
    public void merge(String given_branch_name) {

        String current_branch = Utils.readContentsAsString(_HEAD);
        String current_commit_name = Utils.readContentsAsString(new File(current_branch));
        File current_commit_file = Utils.join(_commits, current_commit_name);
        Commit current_commit = Utils.readObject(current_commit_file, Commit.class);
        HashMap<String, String> refcurr = new HashMap<>();
        refcurr.putAll(current_commit.getRefs());

        File given_branch_file = Utils.join(_branches, given_branch_name);
        String given_commit_name = Utils.readContentsAsString(given_branch_file);
        File given_commit_file = Utils.join(_commits, given_commit_name);
        Commit given_commit = Utils.readObject(given_commit_file, Commit.class);
        HashMap<String, String> refgiven = new HashMap<>();
        refgiven.putAll(given_commit.getRefs());

        merge_error_check(given_branch_file, current_commit_file, given_commit_file);

        String current_branch_name = Paths.get(current_branch).getFileName().toString();
        String split_commit_name = Commit.findSplit(current_commit_name, given_commit_name);
        File split_commit_file = Utils.join(_commits, split_commit_name);
        Commit split_commit = Utils.readObject(split_commit_file, Commit.class);
        HashMap<String, String> refsplit = new HashMap<>();
        refsplit.putAll(split_commit.getRefs());

        String message = "Merged " + given_branch_name + " into " + current_branch_name + ".";
        Commit new_commit = new MergeCommit(current_commit, message, current_commit_name, given_commit_name);

        ArrayList<String> checker = new ArrayList<>();
        for (File f: _cwd.listFiles()) {
            if (!f.isHidden()) {
                checker.add(f.getName());
            }
        }
        for (String f : refcurr.keySet()) {
            merge_cases(f, new_commit, refcurr, refgiven, refsplit);
            checker.remove(f); refgiven.remove(f); refsplit.remove(f);
        }
        for (String f : refgiven.keySet()) {
            if (refsplit.containsKey(f)) {
                if (refsplit.get(f).compareTo(refgiven.get(f)) != 0) {
                    merge_conflict(null, refgiven.get(f), f, new_commit);
                }
            } else {
                new_commit.getRefs().put(f, refgiven.get(f));
            }
            refsplit.remove(f);
        }
        if (!checker.isEmpty()) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        }

        String new_commit_name = Utils.sha1(Utils.serialize(new_commit));
        File new_commit_file = Utils.join(_commits, new_commit_name);
        try {
            new_commit_file.createNewFile();
        } catch (IOException exp) {
            throw new IllegalArgumentException();
        }
        Utils.writeObject(new_commit_file, new_commit);
        Utils.writeContents(new File(current_branch), new_commit_name);
        for (File f: _cwd.listFiles()) {
            if (!refsplit.containsKey(f.getName()) && !f.isHidden()) {
                f.delete();
            }
        }
        for (String s : new_commit.getRefs().keySet()) {
            File k = Utils.join(_cwd, s);
            if (!k.exists()) {
                try {
                    k.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(k,
                    Blob.getBlob(new_commit.getRefs().get(s)).getContent());
        }
    }

    /** Checks for certain errors that might arise while merging.
     * @param given_branch_file The Branch to be merged with.
     * @param current_commit_file The current commit.
     * @param given_commit_file The commit to be merged with.*/
    public void merge_error_check(File given_branch_file,
                                  File current_commit_file,
                                  File given_commit_file) {
        StagedObject toBeAdded = Utils.readObject(_add, StagedObject.class);
        StagedObject toBeRemoved = Utils.readObject(_remove, StagedObject.class);
        if (!given_branch_file.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (!toBeAdded.getStagedItems().isEmpty()) {
            System.out.println("You have uncommitted changes.");
        } else if (!toBeRemoved.getStagedItems().isEmpty()) {
            System.out.println("You have uncommitted changes.");
        } else if (current_commit_file.equals(given_commit_file)) {
            System.out.println("Cannot merge a branch with itself.");
        }
    }


    /** String for saving parent commit.
     * @param f name of the file in the current commit
     * @param newC the new_commit which will be made after the merge is completed.
     * @param refcurr The hashmap of files:blobs in the current commit.
     * @param refsplit The hashmap of files:blobs in the commit at split point.
     * @param refgiven The hashmap of files:blobs in the latest commit of the given branch. */
    public void merge_cases(String f, Commit newC,
                        HashMap<String, String> refcurr,
                        HashMap<String, String> refgiven,
                        HashMap<String, String>  refsplit) {

        if (!refsplit.containsKey(f)) {
            if (refgiven.containsKey(f)
                    && refcurr.get(f).compareTo(refgiven.get(f)) != 0) {
                merge_conflict(refcurr.get(f), refgiven.get(f), f, newC);
            }
        } else {
            if (refgiven.containsKey(f)
                    && refsplit.get(f).compareTo(refcurr.get(f)) == 0
                    && refgiven.get(f).compareTo(refcurr.get(f)) != 0) {
                newC.getRefs().put(f, refgiven.get(f));
            }
            if (!refgiven.containsKey(f)
                    && refcurr.get(f).compareTo(refsplit.get(f)) == 0) {
                newC.getRefs().remove(f);
            }
            if (refgiven.containsKey(f)
                    && refsplit.get(f).compareTo(refcurr.get(f)) != 0
                    && refcurr.get(f).compareTo(refgiven.get(f)) != 0
                    && refsplit.get(f).compareTo(refgiven.get(f)) != 0) {
                merge_conflict(refcurr.get(f), refgiven.get(f), f, newC);
            }
            if (!refgiven.containsKey(f)
                    && refcurr.get(f).compareTo(refsplit.get(f)) != 0) {
                merge_conflict(refcurr.get(f), null, f, newC);
            }
        }
    }

    /** Handles merge conflicts when they occur by creating a new type of blob
     * It merges the two given files, the one being merged into at the top and saves it as a new blob.
     * @param current_blob the current blob to which the other one will be merged into.
     * @param given_blob the given blob which is to be merged into the current blob.
     * @param file_name Name of the file associated with that blob.
     * @param new_commit The new commit in which the new blob will be saved.
     */
    public void merge_conflict ( String current_blob, String given_blob,
                                 String file_name, Commit new_commit) {
        System.out.println("Encountered a merge conflict.");
        Blob a = null;
        Blob b = null;
        if (current_blob != null) {
            a = Blob.getBlob(current_blob);
        }
        if (given_blob != null) {
            b = Blob.getBlob(given_blob);
        }
        Blob c = new Blob(a, b);
        String new_blob_name = Utils.sha1(Utils.serialize(c));
        File new_blob_file = Utils.join(_blobs, new_blob_name);
        if (!new_blob_file.exists()) {
            try {
                new_blob_file.createNewFile();
            } catch (IOException exp) {
                throw new IllegalArgumentException();
            }
            Utils.writeObject(new_blob_file, c);
        }
        new_commit.getRefs().put(file_name, new_blob_name);
    }



}

