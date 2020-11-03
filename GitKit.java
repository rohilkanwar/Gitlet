package gitlet;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class GitKit {

    public static File cwd = new File(".");
    public static File gitboi = new File(cwd,".gitlet");
    public static File headbranch = new File(gitboi, "HEAD");
    public static File stagingarea = new File(gitboi, "STAGING_AREA");
    public static File commits = new File(gitboi, "COMMITS");;
    public static File branchbois = new File(gitboi, "BRANCHES");
    public static File dubdubdub = new File(gitboi, "BLOBS");
    public static File tvremote = new File(gitboi, "REMOTES");

//    ClassLoader classboi = new ClassLoader() {
//
//        public Class<?> loadClass(String name) throws ClassNotFoundException {
//            return classboi.loadClass(name);
//        }
//    };
//
//    Class<?> init = classboi.loadClass("gitlet.Init");
//
//    public GitKit() throws ClassNotFoundException {
//    }

    public static File getcwd() {
        return new File(".");
    }

    public static byte[] getgitboi() {
        return Utils.readContents(GitKit.gitboi);
    }

    public static StagingArea getstagingarea() {
        return Utils.readObject(GitKit.stagingarea, StagingArea.class);
    }

    public static String getheadbranch() {
        return Utils.readContentsAsString(GitKit.headbranch);
    }

    public static HashMap<String, String> getBranches() {
        return Utils.readObject(GitKit.branchbois, HashMap.class);
    }

    public static Commit getHeadCommit() {
        HashMap<String, String> allBranches = GitKit.getBranches();
        String HEAD = allBranches.get(GitKit.getheadbranch());
        for (File eachcommit : GitKit.commits.listFiles()) {
            if (eachcommit.getName().equals(HEAD)) {
                Commit headcommit = Utils.readObject(eachcommit, Commit.class);
                return headcommit;
            }
        }
        return null;
    }

    public static Commit getCommit(String commitID) {
        boolean noSuchCommit = true;
        for (File eachcommit : GitKit.commits.listFiles()) {
            if (eachcommit.getName().equals(commitID) || eachcommit.getName().substring(0,6).equals(commitID)) {
                noSuchCommit = false;
                Commit headcommit = Utils.readObject(eachcommit, Commit.class);
                return headcommit;
            }
        }
        if (noSuchCommit) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return null;
    }

    public static void checkUntracked() {
        String branchname = GitKit.getheadbranch();
        StagingArea currArea = GitKit.getstagingarea();
        TreeSet<String> untrackedFiles = new TreeSet<>();
        Commit branchhead = GitKit.getCommit(GitKit.getBranches().get(branchname));
        for (File untracked : GitKit.getcwd().listFiles()) {
            if (untracked.isFile()) {
                if (!(Merge.trackedFiles(branchhead).contains(untracked))) {
                    untrackedFiles.add(untracked.getName());
                }
            }
        }
//        for (Blob otherFile : branchhead.tracking) {
//            if (!(currArea.addStage.contains(otherFile)) && !(currArea.storeStage.contains(otherFile))) {
//                if (otherFile.fileboi.isFile() && !(otherFile.fileboi.isDirectory())) {
//                    untrackedFiles.add(otherFile.fileboi.getName());
//                }
//            }
//        }
        if (!(untrackedFiles.isEmpty())) {
            Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    public static Object getObject(byte[] byteArr) throws IOException, ClassNotFoundException {
        ByteArrayInputStream baistream = new ByteArrayInputStream(byteArr);
        ObjectInput input = new ObjectInputStream(baistream);
        return input.readObject();
    }
}
