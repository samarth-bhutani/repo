package gitlet;

/** Driver class for Gitlet, the tiny version-control system similar to GIT
 * used for saving and merging files when working with others
 *  @author Samarth Bhutani
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     * init : initiates a GIT repository in the location where it is called
     * add [file name] : adds file to the staging area
     * commit [file name]: saved all the files in the staging area and removes the ones which are staged to be removed
     * log : returns the log of the current branch
     * rm [file name] : removes the given file
     * global-log : return the list of all commits ever made
     * find ["message"] : returns the commit with the given message
     * status : returns the current status of the GIT repository
     * branch [branch name] : creates a new branch with the given name
     * rm-branch [branch name] : removes the given branch
     * reset [commit id] : resets back to the given commit id.
     * merge [branch name] : merges the content in the current branch with the given branch
     * checkout -- [file name] : checkout the version of the given file in the latest commit
     * checkout [commit id] -- [file name] : checkout the version of the given file in the given commit
     * chekcout [branch name] : checkout the entire content of the latest commit in the given branch.
     */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init")) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            Repo.init();
        } else if (args[0].equals("add")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.add(args[1]);
        } else if (args[0].equals("commit")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.commit(args[1]);
        } else if (args[0].equals("log")) {
            Repo ans = new Repo();
            ans.error_check(1, args.length);
            ans.log();
        } else if (args[0].equals("rm")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.rm(args[1]);
        } else if (args[0].equals("global-log")) {
            Repo ans = new Repo();
            ans.error_check(1, args.length);
            ans.global_log();
        } else if (args[0].equals("find")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.find(args[1]);
        } else if (args[0].equals("status")) {
            Repo ans = new Repo();
            ans.error_check(1, args.length);
            ans.status();
        } else if (args[0].equals("branch")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.rmbranch(args[1]);
        } else if (args[0].equals("reset")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.reset(args[1]);
        } else if (args[0].equals("merge")) {
            Repo ans = new Repo();
            ans.error_check(2, args.length);
            ans.merge(args[1]);
        } else if (args[0].equals("checkout")) {
            if (args.length > 4 || args.length < 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            Repo ans = new Repo();
            if (args.length == 2) {
                ans.error_check(2, args.length);
                ans.branch_checkout(args);
            } else {
                ans.file_checkout(args);
            }
        } else {
            System.out.println("No command with that name exists.");
            return;
        }
    }



}
