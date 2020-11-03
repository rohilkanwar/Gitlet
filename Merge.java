package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Merge {
    /**
     * The Merge command is split into the following parts:
     *
     * A. Finding the best split point
     * B. Split it into two cases from there:
     *  B.1. Files present in the Split Point
     *  B.2. Files absent in the Split Point
     *
     */
    static boolean encounteredConflict;

    public static void mergeCommand(String branchname) throws IOException, ClassNotFoundException {
        Merge.checkErrorCases(branchname);
        Commit splitPoint = Merge.findSplitPoint(branchname);
        String printMsg = "";
        if (splitPoint.name.equals(GitKit.getBranches().get(branchname))) {
            printMsg = "Given branch is an ancestor of the current branch.";
            System.out.println(printMsg);
            return;
        }
        if (!(splitPoint.name.equals(GitKit.getHeadCommit().name))) {
            Commit.checkoutBranch(branchname);
            printMsg = "Current branch fast-forwarded.";
            System.out.println(printMsg);
            return;
        }
        StagingArea currArea = GitKit.getstagingarea();
        Commit HEAD = GitKit.getHeadCommit();
        Commit branchHead = GitKit.getCommit(GitKit.getBranches().get(branchname));
        HashSet<File> splitFiles = trackedFiles(splitPoint);
        for (Blob tracked : splitPoint.tracking) {
            // make one Blob per file in the commit tracking arrayList
            if (splitFiles.contains(tracked.fileboi)) {
                if (!(getFileBlob(tracked.fileboi, branchHead).contents.equals(getFileBlob(tracked.fileboi, splitPoint)))) {
                    if (getFileBlob(tracked.fileboi, HEAD).contents.equals(getFileBlob(tracked.fileboi, splitPoint))) {
                        Commit.checkoutCommit(branchHead.name, tracked.fileboi.getName());
                        currArea.addStage.add(tracked);
                    }
                }
            }
        }

        for (File branchFile : trackedFiles(branchHead)) {
            if (!(trackedFiles(HEAD).contains(branchFile))) {
                if (!(trackedFiles(splitPoint).contains(branchFile))) {
                    Commit.checkoutFile(branchFile.getName());
                    currArea.addStage.add(getFileBlob(branchFile, branchHead));
                }
            }
        }

        for (File splitFile : splitFiles) {
            if (getFileBlob(splitFile, splitPoint).contents.equals(getFileBlob(splitFile, HEAD).contents)) {
                if (!(trackedFiles(branchHead).contains(splitFile))) {
                    currArea.removalStage.add(getFileBlob(splitFile, splitPoint));
                }
            }
        }

        checkConflict(HEAD, branchHead, splitPoint, splitFiles);
        checkConflict(branchHead, HEAD, splitPoint, splitFiles);

        String commitMsg = "Merged " + branchname + " into " + GitKit.getheadbranch();
        Commit.commitCommand(commitMsg, GitKit.getBranches().get(branchname));
        if (encounteredConflict) {
            System.out.println("Encountered a merge conflict.");
        }

        Utils.writeObject(GitKit.stagingarea, currArea);
    }

    public static HashSet<File> trackedFiles(Commit community) {
        HashSet<File> tracked = new HashSet<>();
        for (Blob eachFile : community.tracking) {
            tracked.add(eachFile.fileboi);
        }
        return tracked;
    }

    public static Blob getFileBlob(File givenFile, Commit community) {
        for (Blob babyblob : community.tracking) {
            if (babyblob.fileboi==givenFile) {
                return babyblob;
            }
        }
        Utils.message("This function doesn't work correctly.");
        System.exit(0);
        return null;
    }

    public static Commit findSplitPoint(String branchname) {
        Commit HEAD = GitKit.getHeadCommit();
        Commit otherHEAD = GitKit.getCommit(GitKit.getBranches().get(branchname));
        ArrayList<Commit> currCommits = new ArrayList<>();
        ArrayList<Commit> otherCommits = new ArrayList<>();
        Commit splitPoint = HEAD;
        Commit headPointer = HEAD;
        while (headPointer.mergeParent==null && headPointer.parent!=null) {
            currCommits.add(headPointer);
            headPointer = GitKit.getCommit(headPointer.parent);
        }
        if (headPointer.parent!=null) {
            currCommits.add(GitKit.getCommit(headPointer.mergeParent));
        }
        Commit otherPointer = otherHEAD;
        while (otherPointer.parent!=null) {
            if (currCommits.contains(otherPointer)) {
                splitPoint = otherPointer;
            }
            otherPointer = GitKit.getCommit(otherPointer.parent);
        }
//        if (otherPointer.parent!=null) {
//            if (currCommits.contains(otherPointer.mergeParent)) {
//                splitPoint = GitKit.getCommit(otherPointer.mergeParent);
//            }
//        }
        return splitPoint;
    }

    public static void checkErrorCases(String branchname) {
//        GitKit.checkUntracked();
        StagingArea currArea = GitKit.getstagingarea();
        if (currArea.addStage.size()!=0 || currArea.removalStage.size()!=0) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        if (!(GitKit.getBranches().keySet().contains(branchname))) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (GitKit.getheadbranch().equals(branchname)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    public static void checkConflict(Commit currBranch, Commit givenBranch, Commit splitPoint, HashSet<File> splitFiles) {
        for (File currFile : trackedFiles(currBranch)) {
            boolean changedContents = false;
            boolean oneChangedOtherDeleted = false;
            boolean newDiffFiles = false;
            if (splitFiles.contains(currFile)) {
                if (!(getFileBlob(currFile, currBranch).contents.equals(getFileBlob(currFile,splitPoint)))) {
                    if (trackedFiles(givenBranch).contains(currFile)) {
                        if (!(getFileBlob(currFile, currBranch).contents.equals(getFileBlob(currFile, givenBranch)))) {
                            changedContents = true;
                        }
                    }
                    else {
                        oneChangedOtherDeleted = true;
                    }
                }
            }
            else {
                if (trackedFiles(currBranch).contains(currFile) && trackedFiles(givenBranch).contains(currFile)) {
                    if (!(getFileBlob(currFile, currBranch).contents.equals(getFileBlob(currFile, givenBranch)))) {
                        newDiffFiles = true;
                    }
                }
            }
            if (changedContents || oneChangedOtherDeleted || newDiffFiles) {
                encounteredConflict = true;
                getFileBlob(currFile, currBranch).contents = Utils.serialize("<<<<<<<< HEAD\n" +
                        "contents of file in current branch=======\n" +
                        "contents of file in given branch>>>>>>>");
//                check if oneChangedOrDeleted to see if contents of file in given branch are supposed to be replaced with an empty string
            }
        }
    }
}
