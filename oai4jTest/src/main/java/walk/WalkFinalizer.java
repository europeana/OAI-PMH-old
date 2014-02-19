package walk;

import org.apache.commons.lang3.time.StopWatch;

import java.io.PrintStream;
import java.util.Date;

/**
 * Created by Simo on 14-2-12.
 */
public class WalkFinalizer implements Runnable {
    private final PrintStream out;
    private final ListRecordsWalker walker;

    public WalkFinalizer(PrintStream out, ListRecordsWalker walker) {
        this.out = out;
        this.walker = walker;
    }

    public void run() {
        StopWatch sw = new StopWatch();
        try {
            out.println(Thread.currentThread().getName());
            out.println(new Date());
            sw.start();
            walker.run();
            walker.recordProcessor.total();
            walker.listProcessor.total();
            sw.stop();
            out.println(sw);
        } finally {
            out.close();
        }

    }
}
