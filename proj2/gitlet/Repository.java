package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository extends SimpleDateFormat implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public Commit mostRecentCommit;
    public HashMap<String, byte[]> stagingArea;
    public HashMap<String, byte[]> removalArea; //NEW
    public File saveStage;
    public File COMMIT_DIR = join(GITLET_DIR,".commits");
    public String headId;
    //keeps track of all the branches in the repository
    private HashMap<String, String> branches;
    //keeps track of currently checked out branches
    private String currentBranch;
    private String workingDir;

    public Repository() {
        this.currentBranch = "main";//manpreet
        //check if gitlet already exists
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            COMMIT_DIR.mkdir();
        }
        init();
    }


    public void init() {
        this.branches = new HashMap<>(); //manpreet
        // Check if there are any files in the staging area, and if so, clear it

        // TODO: fixme

        //create a initial commit and serialize it and get the sha1 and then create a file with it
        this.mostRecentCommit = new Commit("initial commit", null, new Date(0), new HashMap<>());
        byte[] serialized_initialCommit = Utils.serialize(this.mostRecentCommit);
        String initialCommit_ID = Utils.sha1(serialized_initialCommit);

        File initialCommit_File = Utils.join(COMMIT_DIR, initialCommit_ID);
        Utils.writeContents(initialCommit_File, serialized_initialCommit);
        headId = initialCommit_ID;

        // create main branch
        File master = Utils.join(GITLET_DIR, "main");
        Utils.writeContents(master, initialCommit_ID);

        //keep track of staging area
        saveStage = Utils.join(GITLET_DIR, "staging");
        stagingArea = new HashMap<>();
        removalArea = new HashMap<>();
        Utils.writeContents(saveStage, Utils.serialize(stagingArea));
        //there shouldn't be anything in the staging area to save from
        //initial commit, right? so add should see if a saveStage file
        //already exists and if not, creates a new one?
    }

    public void add(String fileToAdd) {
        // adding already added = overwriting previous entry with new contents
        // if current working version is identical to version in current commit,
        // do not add + remove from staging area if it is already there

        boolean hasChanged = true;
        // create new path for the file being added to staging area
        File newPath = Utils.join(CWD, fileToAdd);

        // if the path doesn't exist, print out error message
        if (!newPath.exists()) {
            System.out.println("File does not exist."); //CHANGE: to match spec
            return;
        }

        // checks to see if the file was in the most recent commit
        // if so, checks to see if it has changed
        if (mostRecentCommit.getTrackedFileContents().containsKey(fileToAdd)) {
            //hasChanged = Utils.readContents(newPath) != mostRecentCommit.getTrackedFileContents().get(fileToAdd);
            byte[] mostRecentContents = mostRecentCommit.getTrackedFileContents().get(fileToAdd); //manpreet
            hasChanged = mostRecentContents == null || !Arrays.equals(Utils.readContents(newPath), mostRecentContents); // manpreet

        }

        // if there has been a change in the file to be added, changes the
        // corresponding value in the hashmap to the new contents of the file
        // AKA overrides original content in file
        if (hasChanged) {
            byte[] changedContents = Utils.readContents(newPath);
            stagingArea.put(fileToAdd, changedContents);
        } else {
            stagingArea.remove(fileToAdd); //removes file from staging area if no change
        }
        //possibly serialize staging area to some txt file? tbd
    }

    public void commit(String message) {
        /*default: a commit has the same file contents as parent
        - files staged for addition and removal are the updates to the commit
        - staging area is cleared after commit
        - new commit becomes 'current commit' with head pointer now pointing at it

        //each commit is identified by sha-1 id which includes the file references
        //to its files, parent references, log message, and commit time*/

        //if no message inputted, prints error message
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stagingArea.isEmpty() && removalArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        // Create a new commit with the last commit as parent
        Commit newCommit = new Commit(message, headId, new Date(), new HashMap<>(mostRecentCommit.getTrackedFileContents()));

        // Add all the files from the staging area to the new commit
        newCommit.setTrackedFileContents(new HashMap<>(stagingArea));

        // Remove files from the removal area from the new commit
        for (String fileName : removalArea.keySet()) {
            newCommit.getTrackedFileContents().remove(fileName);
        }

        // Save the new commit
        byte[] newCommitSerialized = Utils.serialize(newCommit);
        String newCommitId = Utils.sha1(newCommitSerialized);
        File newCommitFile = Utils.join(COMMIT_DIR, newCommitId);
        Utils.writeContents(newCommitFile, newCommitSerialized);

        // Update the head pointer and mostRecentCommit
        headId = newCommitId;
        mostRecentCommit = newCommit;

        // Clear the staging area and removal area
        stagingArea.clear();
        removalArea.clear();
    }

    public void printLog() { //FIXME: for merge commits !!!
        Commit commitToPrint = mostRecentCommit;
        String commitToPrintId = headId;

        while (commitToPrintId != null) {
            printLogFormatter(commitToPrintId, commitToPrint);
            commitToPrintId = commitToPrint.getParentId();

            if (commitToPrintId != null) {
                File nextCommitFilePath = Utils.join(COMMIT_DIR, commitToPrintId);
                commitToPrint = Utils.readObject(nextCommitFilePath, Commit.class);
            }
        }
    }

    // Formats output for specified commit
    public void printLogFormatter(String commitToPrintId, Commit commitToPrint) {
        SimpleDateFormat friend = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        System.out.println("===");
        System.out.println("commit " + commitToPrintId);
        Date time = commitToPrint.getTimestamp();
        System.out.println("Date: " + friend.format(time));
        System.out.println(commitToPrint.getMessage());
        System.out.println();
    }

    public void restore(String fileName) {
        /* Takes the version of the file as it exists in the head commit
         * and puts it in the working directory, overwriting the version
         * of the file that’s already there if there is one.
         * The new version of the file is not staged. */

        // calls other restore method with headId as first argument
        //System.out.println("why why");
        //System.out.println(headId);
        restore(headId, fileName);

    }
    public void restore(String commitId, String fileName) {
        /* Takes the version of the file as it exists in the commit
         * with the given id, and puts it in the working directory,
         * overwriting the version of the file that’s already there
         * if there is one. The new version of the file is not staged.
         */
        if (fileName == null || fileName.isEmpty() || fileName.startsWith("--")) {
            System.out.println("Incorrect operands.");
            return;
        }

        File commitToRestorePath = Utils.join(COMMIT_DIR, commitId);
        //if (commitToRestorePath.isFile()) {
        if (commitToRestorePath.exists()) {//manpreet
            Commit commitToRestore = Utils.readObject(commitToRestorePath, Commit.class);
            HashMap<String, byte[]> commitToRestoreContents = commitToRestore.getTrackedFileContents(); //how commit keeps track of files + content
            byte[] fileToRestoreContents = commitToRestoreContents.get(fileName); //getting files contents
            File currentFileToReplacePath = Utils.join(CWD,fileName); //file to restore in current working directory
            if (fileToRestoreContents == null) {
                System.out.println("File does not exist in that commit.");
            } else {
                Utils.writeContents(currentFileToReplacePath, fileToRestoreContents); //overwrites contents in CWD
            }
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    public void rm(String fileName) {
        // check if the files are in the current commit


        /* Unstage the file if it is currently staged for
         * addition. If the file is tracked in the current
         * commit, stage it for removal and remove the file
         * from the working directory if the user has not
         * already done so (do not remove it unless it is
         * tracked in the current commit).
         *
         * If the file is neither staged nor tracked by the
         * head commit, print the error message
         * "No reason to remove the file."
         *  */

        // Check if the file is staged for addition
        // Check if the file is staged for addition
        // Check if the file is staged for addition
        if (stagingArea.containsKey(fileName)) {
            stagingArea.remove(fileName);
            Utils.writeContents(saveStage, Utils.serialize(stagingArea));
        } else if (mostRecentCommit.getTrackedFileContents().containsKey(fileName)) {
            // Add the file to the removal area
            removalArea.put(fileName, mostRecentCommit.getTrackedFileContents().get(fileName));
            // Remove the file from the working directory
            Utils.join(CWD, fileName).delete();
            // Remove the file from the current commit's tracked files
            mostRecentCommit.getTrackedFileContents().remove(fileName);
            // Save the updated staging area
            Utils.writeContents(saveStage, Utils.serialize(stagingArea));
            Utils.writeContents(Utils.join(GITLET_DIR, "removal"), Utils.serialize(removalArea));
            // Save the updated mostRecentCommit
            File commitFile = Utils.join(COMMIT_DIR, headId);
            Utils.writeObject(commitFile, mostRecentCommit);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public void printStatus() {
        System.out.println("=== Branches ===");
        //TODO: add branch information here
        System.out.println("*main\n"); //FIXME
        System.out.println("=== Staged Files ===");
        //String[] sortedFileNames = stagingArea.keySet().toArray();


        //NEW Convert the keys in the staging area to a list
        List<String> stagedFiles = new ArrayList<>(stagingArea.keySet());
        // Sort the list of filenames
        Collections.sort(stagedFiles);
        // Print the sorted filenames
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println("\n=== Removed Files ===");

        // Convert the keys in the removal area to a list
        List<String> removedFiles = new ArrayList<>(removalArea.keySet());
        // Sort the list of filenames
        Collections.sort(removedFiles);
        // Print the sorted filenames
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }



        for (String fileName: removalArea.keySet()) {
            System.out.println(fileName);
        }
        //for (String fileName: stagingArea.keySet()) {
            //System.out.println(fileName);
        //}
        //System.out.println("\n=== Removed Files ===");
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

    public void printGlobalLog() {
        /* Like log, except displays information about all commits
        ever made. The order of the commits does not matter.
        Hint: there is a useful method in gitlet.Utils that will
        help you iterate over files within a directory.
        * */
        // makes list of all commit file names in .commits folder
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT_DIR);

        //iterates all the commits in the file and prints them
        //out accordingly
        for (String s: allCommits) {
            String commitToPrintId = s;
            File commitToPrintFile = Utils.join(COMMIT_DIR, commitToPrintId);
            Commit commie = Utils.readObject(commitToPrintFile, Commit.class);
            printLogFormatter(commitToPrintId, commie);
        }
    }
    public void find(String commitMessage) {
        //Store the ids of commits that have give commit message
        List<String> commits = new ArrayList<>();
        //acces all the files and read the stored object and deserialize it as commit object
        for(File file : COMMIT_DIR.listFiles()) {
            Commit c = Utils.readObject(file, Commit.class);
            //checks if its equal to any other commit message and then add
            if (c.getMessage().equals(commitMessage)) {
                commits.add(file.getName());
            }
        }
        //check if its empty if not then go through all the commits and print the id
        if (commits.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String id : commits) {
                System.out.println(id);
            }
        }
    }

    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            // Create a new branch and point it to the current head commit
            branches.put(branchName, headId);
            // Set the initial branch as the current branch if no branches exist yet
            if (currentBranch == null) {
                currentBranch = branchName;
            }
            // Save the updated branches
            saveBranches();
        }
    }
    public Commit getCommitById(String id) {
        File commitFile = new File(COMMIT_DIR, id);
        if (commitFile.exists()) {
            return Utils.readObject(commitFile, Commit.class);
        }
        return null;
    }
    public void switchBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }

        //if (branchName.equals(currentBranch)) {
            //System.out.println("No need to switch to the current branch");
            //return;
        //}

        // Get the target branch's head commit
        String targetBranchHeadId = branches.get(branchName);
        Commit targetBranchHead = getCommitById(targetBranchHeadId);

        // Check if any untracked files would be overwritten by the switch
        for (String fileName : CWD.list()) {
            if (!mostRecentCommit.getTrackedFileContents().containsKey(fileName) && !stagingArea.containsKey(fileName) && targetBranchHead.getTrackedFileContents().containsKey(fileName)) {
                System.out.println("untracked file");
                return;
            }
        }

        // Remove files from the working directory that are not part of the target branch head commit
        for (String fileName : CWD.list()) {
            if (!targetBranchHead.getTrackedFileContents().containsKey(fileName) && !stagingArea.containsKey(fileName)) {
                // Delete the file from the working directory
                Utils.restrictedDelete(Utils.join(CWD, fileName));
            }
        }

        // Update the working directory with files from the head of the target branch
        for (Map.Entry<String, byte[]> entry : targetBranchHead.getTrackedFileContents().entrySet()) {
            String fileName = entry.getKey();
            byte[] fileContents = entry.getValue();
            Utils.writeContents(Utils.join(CWD, fileName), fileContents);
        }

        // Clear the staging area
        stagingArea.clear();

        // Update the current branch and head commit
        currentBranch = branchName;
        headId = targetBranchHeadId;
    }

    public void rmBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (currentBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else if (!stagingArea.isEmpty()) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.remove(branchName);
            // Save the updated branches
            saveBranches();
        }
    }

    private void saveBranches() {
        File file = new File(GITLET_DIR, "branches");
        Utils.writeObject(file, branches);
    }

    public void reset(String commitID) {
        Commit commitToReset = getCommitById(commitID);
        // Check if commitID exists
        if (commitToReset == null) {
            System.out.println("Commit ID doesn't exist");
            return;
        }
        // Check for untracked files
        for (String file : CWD.list()) {
            if (!commitToReset.getTrackedFileContents().containsKey(file) && !stagingArea.containsKey(file) && mostRecentCommit.getTrackedFileContents().containsKey(file)) {
                System.out.println("Untracked files exist. Reset aborted");
                return;
            }
        }
        // Update the working directory
        for (File file : CWD.listFiles()) {
            if (!commitToReset.getTrackedFileContents().containsKey(file.getName())) {
                file.delete();
            }
        }
        for (Map.Entry<String, byte[]> entry : commitToReset.getTrackedFileContents().entrySet()) {
            String file = entry.getKey();
            byte[] contents = entry.getValue();
            Utils.writeContents(new File(CWD, file), contents);
        }
        // Clear the staging area
        stagingArea.clear();
        // Update the head commit if the current branch is the main branch
        if (currentBranch.equals("main")) {
            headId = commitID;
        }
        // Update the branch's pointer to the new commit
        branches.put(currentBranch, commitID);
        saveBranches();
        mostRecentCommit = commitToReset; //manpreet
    }

}
