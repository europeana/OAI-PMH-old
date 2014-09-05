package com.ontotext.process.list;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.RecordProcessor;
import com.ontotext.process.record.RecordProcessorHub;
import se.kb.oai.pmh.RecordsList;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Simo on 14-2-27.
 */
public class ListProcessorHub implements ListProcessor {
    private static final int MAX_PROCESSORS = 100;
    private final List<ListProcessor> listProcessors;
    private final List<RecordProcessor> recordProcessors;
    static private Log log = LogFactory.getLog(ListProcessor.class);

    public ListProcessorHub(List<ListProcessor> listProcessors, List<RecordProcessor> recordProcessors) {
        this.listProcessors = listProcessors;
        this.recordProcessors = recordProcessors;
        listProcessors.add(new IterateRecords(new RecordProcessorHub(recordProcessors)));
    }

    public void processListBegin(RecordsList recordsList) {
        for (ListProcessor processor : listProcessors) {
            processor.processListBegin(recordsList);
        }
    }

    public void processListEnd(RecordsList recordsList) {
        for (ListProcessor processor : listProcessors) {
            processor.processListEnd(recordsList);
        }
    }

    public void processListFinish() {
        for (int i = listProcessors.size(); --i >= 0; ) {
            ListProcessor processor = listProcessors.get(i);
            processor.processListFinish();
        }
    }

    public void processListError(Exception e) {
        for (ListProcessor processor : listProcessors) {
            processor.processListError(e);
        }
    }

    public void addProcessor (ListProcessor processor) {
        listProcessors.add(processor);
    }

    public void addProcessor(RecordProcessor processor) {
        recordProcessors.add(processor);
    }

    public static ListProcessorHub load(Properties properties) {
        ArrayList<ListProcessor> listProcessors = new ArrayList<ListProcessor>();
        ArrayList<RecordProcessor> recordProcessors = new ArrayList<RecordProcessor>();

        for (int i = 1; i <= MAX_PROCESSORS; ++i) {
            String className = properties.getProperty("processor." + i);
            if (className == null) {
                break;
            }

            try {
                Class<?> processorClass = Class.forName(className);
                Constructor<?> constructor = processorClass.getConstructor(new Class[]{Properties.class});
                Object processor = constructor.newInstance(properties);
                if (processor instanceof RecordProcessor) {
                    log.info("Add RecordProcessor: " + className);
                    recordProcessors.add((RecordProcessor) processor);
                }

                if (processor instanceof ListProcessor) {
                    listProcessors.add((ListProcessor) processor);
                    log.info("Add ListProcessor: " + className);
                }
            } catch (Exception e) {
                log.error(e);
            }

        }

        return new ListProcessorHub(listProcessors, recordProcessors);
    }
}
