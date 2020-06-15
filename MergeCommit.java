package gitlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/** A commit made by a merge.
 *  @author Samarth Bhutani
 */
public class MergeCommit extends Commit {

    /** String for saving the second parent of the commit. */
    private String parent2;

    /** Constructor for a Merge Commit.
     * @param old the previous commit
     * @param message the message associated to this commit
     * @param parent the first parent of the commit.
     * @param parent_2 the second parent of the commit. */
    public MergeCommit(Commit old, String message,
                       String parent, String parent_2) {
        super(old, message, parent);
        this.parent2 = parent_2;
    }

    /** Returns the second parent of the commit.
     * @return String with the name of the second parent. */
    public String getParent2() {
        return this.parent2;
    }


    @Override
    public String toString() {
        DateFormat form = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return "Merge: " + this.getParent().substring(0, 7)
                + " "
                + this.getParent2().substring(0, 7)
                + "\n Date: "
                + form.format(this.getTimestamp())
                + "\n"
                + this.getMessage();
    }
}




