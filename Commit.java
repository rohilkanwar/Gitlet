package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.Normalizer;
import java.util.*;
import java.util.Formatter;

public class Commit implements Serializable {

    String message;
    Date timestamp;
    String parent;
    String branch;
    String name;
    String mergeParent;
//    boolean isHead;
    ArrayList<Blob> tracking;
    /**branch name to SHA-1 id of the HEAD commit*/
    static HashMap<String, String> branchheads;
    // static pointer to head commit
//    static String head;

    //make a file that stores the name of the commit which is the current Head in .gitlet
    //current branch can be extracted by extracting the branch instance attribute of the head commit found in .gitlet

    /**First Commit constructor*/
    Commit() {
        this.message = "initial commit";
        this.timestamp = new Date("00:00:00 UTC, Thursday, 1 January 1970");
        this.parent = null;
        //this.name = SHA uid
        this.branch = "master";
//        this.isHead = true;
//        Commit.head = this;
        this.tracking = new ArrayList<>();
        this.name = Utils.sha1(Utils.serialize(this));
        Utils.writeContents(GitKit.headbranch, "master");
    }

    /**normal commit constructor*/
    Commit(String message) {
        this.message = message;
        this.timestamp = new Date();
        this.branch = GitKit.getheadbranch();
        Commit.branchheads = GitKit.getBranches();
        this.parent = branchheads.get(this.branch);
//        Commit.head = this;
//        this.isHead = isHead;
        this.tracking = new ArrayList<>();
//        this.tracking.addAll(GitKit.getstagingarea().storeStage);
    }

    /**Merge Commit constructor*/
    Commit(String message, String mergeParent) {
        this.message = message;
        this.timestamp = new Date();
        this.branch = GitKit.getheadbranch();
        Commit.branchheads = GitKit.getBranches();
        this.parent = branchheads.get(this.branch);
        this.mergeParent = mergeParent;
//        Commit.head = this;
//        this.isHead = isHead;
        this.tracking = new ArrayList<>();
//        this.tracking.addAll(GitKit.getstagingarea().storeStage);
    }

    /**overloading commitCommand for those with mergeParent*/
    public static void commitCommand(String message, String mergeParent) {
        StagingArea currArea = GitKit.getstagingarea();
        if (currArea.addStage.size()==0) {
            System.out.println("No changes added to the commit.");
        }
        else {
            Commit commitment = new Commit(message, mergeParent);
            HashSet<Blob> tmpStore = new HashSet<>();
            HashSet<Blob> remStore = new HashSet<>();
            if (currArea.storeStage.size()!=0) {
                for (Blob repeatFile : currArea.addStage) {
                    for (Blob storeFile : currArea.storeStage) {
                        if (repeatFile.fileboi.equals(storeFile.fileboi)) {
                            remStore.add(storeFile);
                        }
                        tmpStore.add(repeatFile);
                    }
                }
            }
            else {
                currArea.storeStage.addAll(currArea.addStage);
            }
            currArea.storeStage.addAll(tmpStore);
            currArea.storeStage.removeAll(remStore);
            currArea.storeStage.removeAll(currArea.removalStage);
            commitment.tracking.addAll(currArea.storeStage);
            currArea.addStage.clear();
            currArea.removalStage.clear();
            Utils.writeObject(GitKit.stagingarea, currArea);
            commitment.name = Utils.sha1(Utils.serialize(commitment));
            File freshcommit = new File(GitKit.commits, commitment.name);
            Utils.writeObject(freshcommit, commitment);
            Commit.branchheads.replace(commitment.branch, commitment.name);
            Utils.writeObject(GitKit.branchbois, branchheads);
//            for (Blob eachblob : currArea.addStage) {
//                if (!(commitment.tracking.contains(eachblob))) {
//                    commitment.tracking.add(eachblob);
//                }
//            }
        }
    }

    public static void commitCommand(String message) {
        StagingArea currArea = GitKit.getstagingarea();
        if (currArea.addStage.size()==0 && currArea.removalStage.size()==0) {
            if (!(GitKit.getHeadCommit().message.equals("Remove one file"))) {
                System.out.println("No changes added to the commit.");
            }
        }
        else {
            Commit commitment = new Commit(message);
            HashSet<Blob> tmpStore = new HashSet<>();
            HashSet<Blob> remStore = new HashSet<>();
            if (currArea.storeStage.size()!=0) {
                for (Blob repeatFile : currArea.addStage) {
                    for (Blob storeFile : currArea.storeStage) {
                        if (repeatFile.fileboi.equals(storeFile.fileboi)) {
                            remStore.add(storeFile);
                        }
                        tmpStore.add(repeatFile);
                    }
                }
            }
            if (tmpStore.size()!=0) {
                currArea.storeStage.addAll(tmpStore);
            }
            else {
                currArea.storeStage.addAll(currArea.addStage);
            }
            currArea.storeStage.removeAll(remStore);
            currArea.storeStage.removeAll(currArea.removalStage);
            commitment.tracking.addAll(currArea.storeStage);
            currArea.addStage.clear();
            currArea.removalStage.clear();
            Utils.writeObject(GitKit.stagingarea, currArea);
            commitment.name = Utils.sha1(Utils.serialize(commitment));
            File freshcommit = new File(GitKit.commits, commitment.name);
            Utils.writeObject(freshcommit, commitment);
            Commit.branchheads.replace(commitment.branch, commitment.name);
            Utils.writeObject(GitKit.branchbois, branchheads);
//            for (Blob eachblob : currArea.addStage) {
//                if (!(commitment.tracking.contains(eachblob))) {
//                    commitment.tracking.add(eachblob);
//                }
//            }
        }
    }

    /**Still have to implement the merge if-case*/
    public static void logCommand() {
        Commit head = GitKit.getHeadCommit();
        Commit pointer = head;
        while(pointer.parent!=null) {
            System.out.println("===");
            System.out.println("commit " + pointer.name);
            if(pointer.mergeParent!=null) {
                System.out.println("Merge: "+pointer.parent.substring(0,7) + " " + pointer.mergeParent.substring(0,7));
            }
            System.out.println(String.format("Date: %1$ta %1$tb"
                    + " %1$te %1$tT %1$tY %1$tz", pointer.timestamp));
//            System.out.println("Date: " + pointer.timestamp.toString());
            System.out.println(pointer.message);
//            System.out.println("Merge: ");
            System.out.println();
            pointer=GitKit.getCommit(pointer.parent);
        }
        System.out.println("===");
        System.out.println("commit " + pointer.name);
        System.out.println(String.format("Date: %1$ta %1$tb"
                + " %1$te %1$tT %1$tY %1$tz", pointer.timestamp));
//        System.out.println("Date: " + pointer.timestamp.toString());
        System.out.println(pointer.message);
//        System.out.println();
    }

    public static void globalLogCommand() {
        for (File eachcommit : Objects.requireNonNull(GitKit.commits.listFiles())) {
            Commit pointer = Utils.readObject(eachcommit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + pointer.name);
            if(pointer.mergeParent!=null) {
                System.out.println("Merge: "+pointer.parent.substring(0,7) + " " + pointer.mergeParent.substring(0,7));
            }
            System.out.println(String.format("Date: %1$ta %1$tb"
                    + " %1$te %1$tT %1$tY %1$tz", pointer.timestamp));
//            System.out.println("Date: " + pointer.timestamp.toString());
            System.out.println(pointer.message);
//            System.out.println("Merge: ");
            System.out.println();
        }
    }

    public static void findCommand(String msg) {
        boolean noCommit = false;
        for (File eachcommit : GitKit.commits.listFiles()) {
            Commit pointer = Utils.readObject(eachcommit, Commit.class);
            if (pointer.message.equals(msg)) {
                System.out.println(pointer.name);
                noCommit = true;
            }
        }
        if (!noCommit) {
            Utils.message("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void checkoutFile(String filename) throws IOException, ClassNotFoundException {
        Commit head = GitKit.getHeadCommit();
        boolean containsFile = false;
        for (Blob file : head.tracking) {
            if (file.fileboi.getName().equals(filename)) {
                containsFile = true;
                File updatedFile = new File(GitKit.getcwd(), file.fileboi.getName());
                updatedFile.createNewFile();
                File dummy = new File(GitKit.getcwd(), "dummyTest.txt");
//                dummy.createNewFile();
//                Utils.writeContents(dummy, Utils.readContents(file.fileboi));
//                String fileContents = Utils.readContentsAsString(dummy);
                Utils.writeContents(updatedFile, new String(file.contents, StandardCharsets.UTF_8));
//                File[] cwdFiles = GitKit.getcwd().listFiles();
//                if (Arrays.asList(cwdFiles).contains(updatedFile)) {
//
//                }
//            }
            }
        }
        if (!containsFile) {
            Utils.message("File does not exist in that commit");
            System.exit(0);
        }
    }

    public static void checkoutCommit(String commitid, String filename) throws IOException {
        String[] allCommits = GitKit.commits.list();
        if (!(Arrays.asList(allCommits).contains(commitid))) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        Commit givenCommit = GitKit.getCommit(commitid);
        boolean containsFile = false;
        for (Blob file : givenCommit.tracking) {
            if (file.fileboi.getName().equals(filename)) {
                containsFile = true;
                File updatedFile = new File(GitKit.getcwd(), file.fileboi.getName());
                updatedFile.createNewFile();
                Utils.writeContents(updatedFile, new String(file.contents, StandardCharsets.UTF_8));
            }
        }
        if (!containsFile) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }
    }

    public static void checkoutBranch(String branchname) throws IOException {
        StagingArea currArea = GitKit.getstagingarea();
//        GitKit.checkUntracked();
//        TreeSet<String> untrackedFiles = new TreeSet<>();
//        for (File otherFile : GitKit.getcwd().listFiles()) {
//            if (!(currArea.addStage.contains(otherFile)) && !(currArea.storeStage.contains(otherFile))) {
//                if (otherFile.isFile() && !(otherFile.isDirectory())) {
//                    untrackedFiles.add(otherFile.getName());
//                }
//            }
//        }
//        if (!(untrackedFiles.isEmpty())) {
//            Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
//        }
        HashMap<String, String> branches = GitKit.getBranches();
        if (!(branches.keySet().contains(branchname))) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        String currBranch = GitKit.getheadbranch();
        if (currBranch.equals(branchname)) {
            Utils.message("No need to checkout the current branch.");
            System.exit(0);
        }
        String commitid = branches.get(branchname);
        Commit branchHead = GitKit.getCommit(commitid);
        for (Blob trackedFile : branchHead.tracking) {
            Commit.checkoutCommit(commitid, trackedFile.fileboi.getName());
        }
        for (Blob currFile : GitKit.getHeadCommit().tracking) {
            if (!(branchHead.tracking.contains(currFile))) {
                Utils.restrictedDelete(currFile.fileboi);
            }
        }
        currArea.addStage.clear();
        currArea.removalStage.clear();
        Utils.writeContents(GitKit.headbranch, branchname);
    }

    public static void branchCommand(String branchname) {
        HashMap<String, String> branches = GitKit.getBranches();
        if (branches.keySet().contains(branchname)) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        branches.put(branchname, branches.get(GitKit.getheadbranch()));
        Utils.writeObject(GitKit.branchbois, branches);
    }

    public static void rmBranchCommand(String branchname) {
        HashMap<String, String> branches = GitKit.getBranches();
        if (!(branches.keySet().contains(branchname))) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchname.equals(GitKit.getheadbranch())) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        branches.remove(branchname);
        Utils.writeObject(GitKit.branchbois, branches);
    }

    public static void resetCommand(String commitID) throws IOException, ClassNotFoundException {
//        GitKit.checkUntracked();
        StagingArea currArea = GitKit.getstagingarea();

        Commit givenCommit = GitKit.getCommit(commitID);
        for (Blob trackedFile : givenCommit.tracking) {
            Commit.checkoutFile(trackedFile.fileboi.getName());
        }
        ArrayList<Blob> tempstore = new ArrayList<>();
        for (Blob trackedFile : currArea.storeStage) {
            if (!(givenCommit.tracking.contains(trackedFile))) {
                tempstore.add(trackedFile);
            }
        }
        currArea.storeStage.removeAll(tempstore);
        HashMap<String, String> branchbois = GitKit.getBranches();
        branchbois.replace(givenCommit.branch, givenCommit.name);
        Utils.writeObject(GitKit.branchbois, branchbois);
    }

//    public static void commitCommand(String message) {
//        if (message.equals("")) {
//            Utils.message("Please enter a commit message.");
//        }
//        StagingArea currArea = null;
//        for (File dir : Init.cwd.listFiles()) {
//            if (dir.getName().equals(".gitlet")) {
//                repo = dir;
//                for (File stagefile : dir.listFiles()) {
//                    if (stagefile.getName().equals("STAGING_AREA")) {
//                        currArea = Utils.readObject(stagefile, StagingArea.class);
//                    }
//                }
//            }
//        }
//        if (currArea.addStage.size()==0) {
//            Utils.message("No changes added to the commit.");
//        }
//        // currBranch needs to be changed to some file tracking the head commit
//        String currBranch = "";
//        File commitsfile = new File("blabla");
//        for (File dir : cwd.listFiles()) {
//            if (dir.getName().equals(".gitlet")) {
//                for (File branchname : dir.listFiles()) {
//                    if (branchname.getName().equals("CURR_BRANCH")) {
//                        currBranch = Utils.readContentsAsString(branchname);
//                    }
//                    if (branchname.getName().equals("COMMITS")) {
//                        commitsfile = branchname.getAbsoluteFile();
//                    }
//                }
//            }
//        }
//        Commit nextcommit = new Commit(message, Commit.head.toString(), currBranch, currArea.addStage);
//        Utils.writeObject(commitsfile, nextcommit);
//        currArea = new StagingArea();
//        Utils.writeObject(repo, currArea);
//    }
}
