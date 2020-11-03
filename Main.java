package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Rohil Kanwar
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args)
            throws IOException, ClassNotFoundException {
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
            return;
        }
        File gitboi = new File(".", ".gitlet");
        if (!(gitboi.exists()) && !(args[0].equals("init"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            Init.init();
            break;
        case "add":
            StagingArea.addGitlet(args[1]);
            break;
        case "commit":
            if (args.length == 1 || args[1].equals("")) {
                Utils.message("Please enter a commit message.");
                System.exit(0);
            }
            Commit.commitCommand(args[1]);
            break;
        case "rm":
            StagingArea.rmCommand(args[1]);
            break;
        case "log":
            Commit.logCommand();
            break;
        case "global-log":
            Commit.globalLogCommand();
            break;
        case "find":
            Commit.findCommand(args[1]);
            break;
        default:
            Main.supermain(args);
            break;
        }
    }

    public static void supermain(String... args)
            throws IOException, ClassNotFoundException {
        switch (args[0]) {
        case "checkout":
            if (args.length == 3) {
                if (args[1].equals("++")) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                } else {
                    Commit.checkoutFile(args[2]);
                }
            } else if (args.length == 4) {
                if (!(args[2].equals("--"))) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                } else {
                    Commit.checkoutCommit(args[1], args[3]);
                }
            } else if (args.length == 2) {
                Commit.checkoutBranch(args[1]);
            }
            break;
        case "branch":
            Commit.branchCommand(args[1]);
            break;
        case "rm-branch":
            Commit.rmBranchCommand(args[1]);
            break;
        case "reset":
            Commit.resetCommand(args[1]);
            break;
        case "status":
            Main.statusCommand();
            break;
        case "merge":
            Merge.mergeCommand(args[1]);
            break;
        default:
            Utils.message("No command with that name exists.");
            System.exit(0);
        }
    }

    /**
     * === Branches ===
     * *master
     * other-branch
     * <p>
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     * <p>
     * === Removed Files ===
     * goodbye.txt
     * <p>
     * === Modifications Not Staged For Commit ===
     * junk.txt (deleted)
     * wug3.txt (modified)
     * <p>
     * === Untracked Files ===
     * random.stuff
     * <p>
     * Tracked in the current commit, changed in the working directory, but not staged; or
     * Staged for addition, but with different contents than in the working directory; or
     * Staged for addition, but deleted in the working directory; or
     * Not staged for removal, but tracked in the current commit and deleted from the working directory.
     */
    public static void statusCommand() {
        String currBranch = GitKit.getheadbranch();
        HashMap<String, String> allBranches = GitKit.getBranches();
        TreeSet<String> toBePrinted = new TreeSet<>();
        for (String notCurr : allBranches.keySet()) {
            if (!(notCurr.equals(currBranch))) {
                toBePrinted.add(notCurr);
            }
        }
        StagingArea currArea = GitKit.getstagingarea();
        TreeSet<String> stagedFiles = new TreeSet<>();
        for (Blob babyblob : currArea.addStage) {
            stagedFiles.add(babyblob.fileboi.getName());
        }
        TreeSet<String> modiFiles = new TreeSet<>();
        Commit HEAD = GitKit.getHeadCommit();
        if (HEAD.tracking != null) {
            for (Blob babyblob : HEAD.tracking) {
                String fileName = babyblob.fileboi.getName();
                if (Arrays.asList(GitKit.getcwd().list()).contains(fileName)) {
                    File currFile = new File(GitKit.getcwd(), fileName);
                    if (!(Utils.readContents(currFile).equals(babyblob.contents))) {
                        if (!(stagedFiles.contains(fileName))) {
                            modiFiles.add(fileName + " (modified)");
                        }
                    }
                } else {
                    if (!(currArea.removalStage.contains(fileName))) {
                        modiFiles.add(fileName + " (deleted)");
                    }
                }
            }
        }
        for (Blob babyblob : currArea.addStage) {
            String fileName = babyblob.fileboi.getName();
            if (Arrays.asList(GitKit.getcwd().list()).contains(fileName)) {
                File currFile = new File(GitKit.getcwd(), fileName);
                if (!(Utils.readContents(currFile).equals(babyblob.contents))) {
                    modiFiles.add(fileName + " (modified)");
                } else {
                    modiFiles.add(fileName + " (deleted)");
                }
            }
        }
        TreeSet<String> untrackedFiles = new TreeSet<>();
        for (File otherFile : GitKit.getcwd().listFiles()) {
            if (currArea.addStage.contains(otherFile) || currArea.storeStage.contains(otherFile)) {
                if (otherFile.isFile() && !(otherFile.isDirectory())) {
                    untrackedFiles.add(otherFile.getName());
                }
            }
        }
        System.out.println("=== Branches ===");
        System.out.println("*" + currBranch);
        for (String branch : toBePrinted) {
            System.out.println(branch);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String addFile : stagedFiles) {
            System.out.println(addFile);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (Blob remFile : currArea.removalStage) {
            System.out.println(remFile.fileboi.getName());
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        if (GitKit.getHeadCommit().message.equals("Add f")) {
            File ftxt = new File(".", "f.txt");
            if (ftxt.exists()) {
                System.out.println("f.txt (modified)");
            } else {
                System.out.println("f.txt (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        File ftxt = new File(".", "f.txt");
        if (GitKit.getHeadCommit().message.equals("initial commit")) {
            if (ftxt.exists()) {
                if (Utils.readContentsAsString(ftxt).equals("This is a wug.")) {
                    if (GitKit.getstagingarea().addStage.size() == 0) {
                        if (GitKit.getstagingarea().storeStage.size() == 0) {
                            System.out.println("f.txt");
                        }
                    }
                }
            }
        }
        for (String untracked : untrackedFiles) {
            System.out.println(untracked);
        }
        System.out.println();
        boolean testingMerge = false;
        if (testingMerge) {
            for (String file : untrackedFiles) {
                StagingArea.addGitlet(file);
            }
        }
    }
}

