package process;

import se.kb.oai.pmh.RecordsList;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Simo on 14-2-24.
 */
public class QueueReader implements Runnable {
    private final BlockingQueue<RecordsList> queue;
    private final ListProcessor processor;

    public QueueReader(BlockingQueue<RecordsList> queue, ListProcessor processor) {
        this.queue = queue;
        this.processor = processor;
    }

    public void run() {
        try {
            RecordsList list;
            do {
                list = queue.take();
                if (shouldStop(list)) {
                    processor.processListFinish();
                    break;
                }
                processor.processListBegin(list);
            } while (true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldStop(RecordsList list) {
        return list.size() == 0;
    }
}
