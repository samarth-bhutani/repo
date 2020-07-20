package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/** A StagedObject class for saving objects for addition and removal in a Hashmap.
 *  @author Samarth Bhutani
 */
public class StagedObject implements Serializable {

    /** Hashmap for saving blobs to file name.
     * File name : blob_name
     * blob_name is the sha1 name after serialization*/
    private HashMap<String, String> stagedItems = new HashMap<>();

    /** Stage an object for adding onto the next commit.
     * Creates a blob out of the file to be added and adds it to stagedItems if possible.
     * Special case: if the previous commit already has the same version of the file,
     *  then remove the file from staging area if possible
     * @param file_name name of the file to be added*/
    public void stageObject(String file_name) {

        File cwd = new File(System.getProperty("user.dir"));
        File file_toBeStaged = Utils.join(cwd, file_name);
        Blob blob_new = new Blob(file_toBeStaged);
        String blob_name = Utils.sha1(Utils.serialize(blob_new));

        File head = Utils.join(cwd, ".gitlet", "HEAD");
        File current_branch = new File(Utils.readContentsAsString(head));
        String commit_name = Utils.readContentsAsString(current_branch);
        File commit_file = Utils.join(cwd, ".gitlet", "Commits", commit_name);
        Commit commit_new = Utils.readObject(commit_file, Commit.class);
        if (commit_new.getRefs().get(file_name) != null
                && commit_new.getRefs().get(file_name).equals(blob_name)) {
            this.stagedItems.remove(file_name);
        } else {
            File blob_file = Utils.join(cwd, ".gitlet", "Blobs", blob_name);
            if (!blob_file.exists()) {
                try {
                    blob_file.createNewFile();
                } catch (IOException exp) {
                    throw new IllegalArgumentException();
                }
                Utils.writeObject(blob_file, blob_new);
            }
            this.stagedItems.put(file_name, blob_name);
        }
    }

    /** Stages a file to be removed from the next commit.
     * uses the same stagedItem data structure but in the following format:
     * File name : ""
     * Can be improved in terms of storage space by converting to an arraylist for removals.
     * @param name name of the file to be removed. */
    public void removeObject(String name) {
        this.stagedItems.put(name, "");
    }

    /** Returns Staged Items for removal or additon.
     * @return Hashmap in the form
     *  "file name": "" for removals
     *  "file name": "blob name" for additions*/
    public HashMap<String, String> getStagedItems() {
        return this.stagedItems;
    }
}
