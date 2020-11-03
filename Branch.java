package gitlet;

public class Branch {

    String name;
    /**SHA-1 ID of the Head Commit of this Branch*/
    String HEAD;

    Branch(String name, String HEAD) {
        this.name = name;
        this.HEAD = HEAD;
    }
}

