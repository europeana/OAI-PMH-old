package process.list;

import se.kb.oai.pmh.RecordsList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by Simo on 14-2-11.
 */
public class TimeMeasureProcessor extends TraceListProcessor {
    long lastTime = System.currentTimeMillis();
    BufferedWriter writer;
    private int counter = 0;
    private final int flushRate = 100;

    public TimeMeasureProcessor(PrintStream out) {
        super(out);
        writer = new BufferedWriter(new PrintWriter(new PrintStream(out)));
    }

    public void process(RecordsList recordsList) {
        super.process(recordsList);
        long time = System.currentTimeMillis();
        long diff = time - lastTime;
        lastTime = time;
        if (writer != null) {
            try {
                writer.append(Long.toString(diff));
                writer.newLine();
                if (++counter % flushRate == 0) {
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Object total() {
        super.total();
        if (writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
