package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timestamp;

    // Something that keeps track of what files
    // this commit is tracking
    public HashMap<String, byte[]> trackedFileContents;
    String parentId;
    // Some other stuff ?

    /* TODO: fill in the rest of this class. */
    public Commit (String message, String parentId, Date timestamp,
                   HashMap<String,byte[]> trackedFileContents) {
        this.message = message;
        this.parentId = parentId;
        this.timestamp = timestamp;
        this.trackedFileContents = trackedFileContents;
    }
    public String getMessage() {
        return this.message;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getParentId() {
        return this.parentId;
    }

    public HashMap<String, byte[]> getTrackedFileContents() {
        return trackedFileContents;
    }

    public void setTrackedFileContents(HashMap<String, byte[]> trackedFileContents) {
        this.trackedFileContents = trackedFileContents;
    }



}
