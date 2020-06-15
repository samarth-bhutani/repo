package gitlet;

import java.io.File;
import java.io.Serializable;

/** A Blob class for saving different files.
 * Each file is converted into a blob and the blob in turn is saved
 *  @author Samarth Bhutani
 */

public class Blob implements Serializable {
    /** The contents of the file as an array of bytes. */
    private byte[] contents;


    /** Creating a Blob of a File.
     * @param address file
     */
    public Blob(File address) {
        this.contents = Utils.readContents(address);
    }

    /** Creating a Blob after merging 2 blobs.
     * @param a the first blob
     * @param b the second blob
     */
    public Blob(Blob a, Blob b) {
        String a1 = "";
        String b1 = "";
        if (a != null) {
            a1 = new String(a.contents);
        }
        if (b != null) {
            b1 = new String(b.contents);;
        }
        String c1 = "<<<<<<< HEAD\n"
                + a1
                + "=======\n"
                + b1
                + ">>>>>>>\n";
        this.contents = c1.getBytes();
    }

    /** Returns a Blob from Blob folder.
     * @param name name of the blob to be returned
     * @return Blob */
    public static Blob getBlob(String name) {
        File cwd = new File(System.getProperty("user.dir"));
        File blobs = Utils.join(cwd, ".gitlet", "Blobs", name);
        return Utils.readObject(blobs, Blob.class);
    }

    /** String for saving parent commit.
     * @return Byte[] type */
    public byte[] getContent() {
        return this.contents;
    }

}
