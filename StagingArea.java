package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class StagingArea implements Serializable {

    HashSet<Blob> addStage;
    HashSet<Blob> removalStage;
    HashSet<Blob> storeStage;
    HashSet<String> removed;
    public static StagingArea currentArea;

    StagingArea() {
        addStage = new HashSet<>();
        removalStage = new HashSet<>();
        storeStage = new HashSet<>();
        removed = new HashSet<>();
    }

    public void addCommand(Blob toBeAdded) {
        if (!(toBeAdded.fileboi.exists())) {
            Utils.message("File does not exist.");
            System.exit(0);
        }
        ArrayList<Blob> tempRem = new ArrayList<>();
        for (Blob rmBlob : removalStage) {
            if (Arrays.equals(rmBlob.contents, toBeAdded.contents) && rmBlob.fileboi.getName().equals(toBeAdded.fileboi.getName())) {
                tempRem.add(toBeAdded);
                break;
            }
        }
        removalStage.removeAll(tempRem);
        for (Blob addBlob : addStage) {
            if (Arrays.equals(addBlob.contents, toBeAdded.contents) && addBlob.fileboi.equals(toBeAdded.fileboi)) {
                return;
            }
        }

        for (String rmFile : removed) {
            if (toBeAdded.fileboi.getName().equals(rmFile)) {
                removed.remove(rmFile);
                return;
            }
        }
        for (Blob storeBlob : storeStage) {
            if (Arrays.equals(storeBlob.contents, toBeAdded.contents) && storeBlob.fileboi.equals(toBeAdded.fileboi)) {
                if (!(addStage.contains(toBeAdded))) {
                    return;
                }
            }
        }
        addStage.add(toBeAdded);
//        for (Blob already : addStage) {
//            if (already.fileboi.getName().equals(toBeAdded.fileboi.getName())) {
//                if (already.contents.equals(toBeAdded.contents)) {
//                    if (addStage.contains(toBeAdded)) {
//                        addStage.remove(toBeAdded);
//                    }
//                    return;
//                }
//            }
//        }
//        storeStage.add(toBeAdded);
    }

    public static void rmCommand(String toBeRemoved) {
        currentArea = GitKit.getstagingarea();
        File delFile = new File(".",toBeRemoved);
        if (!(delFile.exists()) && Merge.trackedFiles(GitKit.getHeadCommit()).contains(delFile)) {
//            Blob babyblob = new Blob(Utils.readContents(delFile), delFile);
            for (Blob eachfile : GitKit.getHeadCommit().tracking) {
                if (eachfile.fileboi.getName().equals(toBeRemoved)) {
                    currentArea.removalStage.add(eachfile);
                }
            }
//            Utils.restrictedDelete(babyblob.fileboi);
//            currentArea.removed.add(babyblob.fileboi.getName());
            Utils.writeObject(GitKit.stagingarea, currentArea);
        }
        for (File eachfile : GitKit.getcwd().listFiles()) {
            if (eachfile.getName().equals(toBeRemoved)) {
                Blob babyblob = new Blob(Utils.readContents(eachfile), eachfile);
//                Blob notbabyblob = new Blob(Utils.readContents(eachfile), eachfile);
//                boolean iwanttoknow = babyblob==notbabyblob;
                boolean containsFile = false;
                if (Merge.trackedFiles(GitKit.getHeadCommit()).contains(eachfile)) {
                    currentArea.removalStage.add(babyblob);
                    Utils.restrictedDelete(babyblob.fileboi);
                    currentArea.removed.add(babyblob.fileboi.getName());
                    containsFile=true;
                    Utils.writeObject(GitKit.stagingarea, currentArea);
                }
                for (Blob oneBlob : currentArea.addStage) {
                    if (Arrays.equals(oneBlob.contents,babyblob.contents) && oneBlob.fileboi.equals(babyblob.fileboi)) {
                        currentArea.addStage.remove(oneBlob);
                        containsFile = true;
                        Utils.writeObject(GitKit.stagingarea, currentArea);
                        return;
                    }
                }
//                if (currentArea.addStage.contains(babyblob)) {
//                    currentArea.addStage.remove(babyblob);
//                }
                if (!containsFile) {
                    Utils.message("No reason to remove the file.");
                    System.exit(0);
                }
            }
        }
    }

    public static void addGitlet(String filename) {
        currentArea = GitKit.getstagingarea();
        boolean fileExists = false;
        for (File eachfile : GitKit.getcwd().listFiles()) {
            if (eachfile.getName().equals(filename)) {
                Blob babyblob = new Blob(Utils.readContents(eachfile), eachfile);
                String str = new String(babyblob.contents, StandardCharsets.UTF_8);
                currentArea.addCommand(babyblob);
                fileExists = true;
                Utils.writeObject(GitKit.stagingarea, currentArea);
//                break;
            }
        }
        if (!fileExists) {
            Utils.message("File does not exist.");
            System.exit(0);
        }
//        byte[] contentbytes = Utils.readContents(fileboi);
//        Blob blob = new Blob(contentbytes, fileboi);
    }
}
