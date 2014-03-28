package process;

import org.apache.commons.logging.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by Simo on 14-2-27.
 */
public class OutHolder {
    public PrintStream out;
    public final Log log;

    public OutHolder(String fileName, Log log) {
        this.log = log;
        if (fileName != null) {
            try {
                out = new PrintStream(new FileOutputStream(fileName));
            } catch (FileNotFoundException e) {
                log.error(e);
            }
        }

        if (out == null) {
            out = System.out;
        }
    }

    public void close() {
        if (out != System.out) {
            out.close();
        }
    }
}
