package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

/** A commit class for saving different commits.
 * A commit consists of a message, date, parent and hashmap with references to files:blobs
 *  @author Samarth Bhutani
 */
public class Commit implements Serializable {

    /** The message associated with the commit. */
    private String message;
    /** The time and date at which the commit was made. */
    private Date timestamp;
    /** The sha1 name of the parent commit. */
    private String parent;
    /** A Hashmap which contains the file name : blob name */
    private HashMap<String, String> refs;

    /** Creates a commit for the first time without a parent .
     * @param message1 message associated with the commit.
     * @param parent1 name of the parent of that commit*/
    public Commit(String message1, String parent1) {
        this.message = message1;
        this.parent = parent1;
        if (this.parent == null) {
            this.timestamp = new Date(0);
        }
        this.refs = new HashMap<>();
    }

    /** Creates a new commit from a previous commit.
     * @param old the old commit which is generally copied and then edited.
     * @param message1 the message associated with this new commit.
     * @param parent1 sha1 name of the parent of this new commit*/
    public Commit(Commit old, String message1, String parent1) {
        this.message = message1;
        this.parent = parent1;
        if (old.refs == null) {
            this.refs = new HashMap<>();
        } else {
            this.refs = new HashMap<>();
            this.refs.putAll(old.refs);
        }
        this.timestamp = new Date();

    }

    /** Finds the splitting point when given the name of the current commit and another commit in separate branches.
     * @param curr name of the current commit or commit1.
     * @param given name of the given commit or commit2.
     * @return String name of commit where the two commits from different branches split off*/
    public static String findSplit(String curr, String given) {

        File cwd = new File(System.getProperty("user.dir"));
        ArrayList<String> tab = new ArrayList<>();
        tab = fillTab(tab, given);
        if (tab.contains(curr)) {
            System.out.println("Current branch fast-forwarded.");
        }
        File commitOldAddress;
        Commit ans;
        do {
            commitOldAddress = Utils.join(cwd, ".gitlet", "Commits", curr);
            ans = Utils.readObject(commitOldAddress, Commit.class);
            if (tab.get(0).compareTo(curr) == 0) {
                String k = "Given branch is an ancestor of the current branch.";
                System.out.println(k);
            }
            if (tab.contains(curr)) {
                return curr;
            }
            curr = ans.getParent();
            if (ans instanceof MergeCommit) {
                if (tab.contains(((MergeCommit) ans).getParent2())) {
                    return ((MergeCommit) ans).getParent2();
                }
            }
        } while (curr != null);
        return null;
    }

    /** Helper function for findSplit which finds all the previous commits
     *  which can be compared to in this case to find the split point.
     *  Also splits off if the merge is merge commit and multiple parents have to be added
     * @param tab an empty arraylist which is slowly filled with the parent commits of the given commit
     * @param given given commit name
     * @return Arraylist all the previous parents from the given commit including merge parents and their ancestors*/
    public static ArrayList<String> fillTab(ArrayList<String> tab,
                                             String given) {
        File cwd = new File(System.getProperty("user.dir"));
        File commitOldAddress;
        Commit ans;
        do {
            commitOldAddress = Utils.join(cwd, ".gitlet", "Commits", given);
            ans = Utils.readObject(commitOldAddress, Commit.class);
            if (!tab.contains(given)) {
                tab.add(given);
            }
            given = ans.getParent();
            if (ans instanceof MergeCommit) {
                fillTab(tab, ((MergeCommit) ans).getParent2());
            }
        } while (given != null);
        return tab;
    }

    /** String for saving parent commit.
     * @return String type*/
    public String getMessage() {
        return this.message;
    }
    /** String for saving parent commit.
     * @return String type*/
    public Date getTimestamp() {
        return this.timestamp;
    }
    /** String for saving parent commit.
     * @return String type*/
    public String getParent() {
        return this.parent;
    }
    /** String for saving parent commit.
     * @return String type*/
    public HashMap<String, String> getRefs() {
        return refs;
    }

    @Override
    public String toString() {
        DateFormat form = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return "Date: "
                + form.format(this.getTimestamp())
                + '\n'
                + this.getMessage();
    }

}
