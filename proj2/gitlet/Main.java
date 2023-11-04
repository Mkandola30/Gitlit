package gitlet;

import java.io.File;
import java.util.Date;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        //  TODO: deal with multiple repos
        String repoFilePath = Repository.GITLET_DIR + "/repo.txt";
        File repositoryFile = new File(repoFilePath);

        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (repositoryFile.exists()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    break;
                }
                Repository r = new Repository();
                if (!Repository.GITLET_DIR.exists()) {
                    Repository.GITLET_DIR.mkdir();
                }
                Utils.writeObject(repositoryFile, r);
                break;
            case "add":
                Repository repo = Utils.readObject(repositoryFile, Repository.class);
                //System.out.println("add has a mostRecentCommit " + (repo.mostRecentCommit != null));
                repo.add(args[1]);
                Utils.writeObject(repositoryFile,repo);
                break;
            case "commit":
                repo = Utils.readObject(repositoryFile, Repository.class);
                repo.commit((args)[1]);
                Utils.writeObject(repositoryFile,repo);
                break;

            case "log":
                repo = Utils.readObject(repositoryFile, Repository.class);
                repo.printLog();
                break;
            case "restore":
                repo = Utils.readObject(repositoryFile, Repository.class);
                if (args.length == 4) {
                    String commitIdArg = args[1];
                    String fileToRestoreArg = args[3];
                    repo.restore(commitIdArg, fileToRestoreArg);
                } else if (args.length == 3) {
                    String fileToRestoreArg = args[2];
                    repo.restore(fileToRestoreArg);
                } else {
                    System.out.println("Invalid arguments for restore");
                }

                Utils.writeObject(repositoryFile,repo);
                break;
            case "rm":
                //System.out.println("remove??? i hardly know her move!!!");
                repo = Utils.readObject(repositoryFile, Repository.class);
                if (args.length == 2) {
                    repo.rm(args[1]);
                    Utils.writeObject(repositoryFile, repo);
                } else {
                    System.out.println("Invalid arguments for remove.");
                }
                break;
            //NEW
            case "global-log":
                repo = Utils.readObject(repositoryFile, Repository.class);
                repo.printGlobalLog();
                break;
            case "find":
                repo = Utils.readObject(repositoryFile, Repository.class);
                if (args[0].equals("find")) {
                    String commitMessage = args[1];
                    repo.find(commitMessage);
                    Utils.writeObject(repositoryFile, repo);
                }
                break;
            case "status":
                repo = Utils.readObject(repositoryFile, Repository.class);
                repo.printStatus();
                break;
            case "branch":
                repo = Utils.readObject(repositoryFile, Repository.class);
                if(args.length == 2) {
                    String branchName = args[1];
                    repo.branch(branchName);
                    Utils.writeObject(repositoryFile, repo);
                } else {
                    System.out.println("Invalid");
                }
                break;
            case "switch":
                repo = Utils.readObject(repositoryFile, Repository.class);
                if (args.length == 2) {
                    repo.switchBranch(args[1]);
                    Utils.writeObject(repositoryFile, repo);
                } else {
                    System.out.println("Invalid");
                }
                break;
            case "rm-branch":
                repo = Utils.readObject(repositoryFile, Repository.class);
                if (args.length == 2) {
                    String branchName = args[1];
                    repo.rmBranch(branchName);
                    Utils.writeObject(repositoryFile, repo);
                } else {
                    System.out.println("remove");
                }
                break;
            case "reset":
                if (args.length != 2) {
                    System.out.println("commit id");
                    break;
                }
                String commitId = args[1];
                repo = Utils.readObject(repositoryFile, Repository.class);
                repo.reset(commitId);
                Utils.writeObject(repositoryFile, repo);
                break;
            case "merge":
                System.out.println("mergey mcmergeface");
                break;
            case "":
                System.out.println("hm? can you repeat that?");
                break;
        }
    }

}
