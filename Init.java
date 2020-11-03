package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Init extends Main {


    public static File cwd;
    public static File gitboi;
    public static File headbranch;
    public static File commits;
    public static File branchbois;
    public static File dubdubdub;
    public static File stagingarea;
    public static File tvremote;
    public static HashMap<String, String> branchheads;

    public static StagingArea currentArea;

    public static void init() throws IOException {
        Init.setUp();
        Commit first = new Commit();
//        String commitname = Utils.sha1(first);
        File initialcommit = new File(commits, first.name);
        initialcommit.createNewFile();
        Utils.writeObject(initialcommit, first);
        Init.branchheads = new HashMap<>();
        Init.branchheads.put("master", first.name);
        Utils.writeObject(branchbois, branchheads);
    }

    public static void setUp() throws IOException {
        cwd = new File(".");
        cwd.mkdir();
        gitboi = new File(cwd, ".gitlet");
        if (gitboi.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        gitboi.mkdir();
        headbranch = new File(gitboi, "HEAD");
        headbranch.createNewFile();
        commits = new File(gitboi, "COMMITS");
        commits.mkdir();
        branchbois = new File(gitboi, "BRANCHES");
        branchbois.createNewFile();
        dubdubdub = new File(gitboi, "BLOBS");
        dubdubdub.mkdir();
        stagingarea = new File(gitboi, "STAGING_AREA");
        stagingarea.createNewFile();
        currentArea = new StagingArea();
        Utils.writeObject(stagingarea, currentArea);
        tvremote = new File(gitboi, "REMOTES");
        tvremote.mkdir();
    }
}
