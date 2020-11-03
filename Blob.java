package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    File fileboi;
    byte[] contents;

    Blob(byte[] contents, File fileboi) {
        this.contents = contents;
        this.fileboi = fileboi;
    }

}
