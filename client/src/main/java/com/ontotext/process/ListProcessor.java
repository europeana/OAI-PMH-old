package com.ontotext.process;

/**
 * Created by Simo on 14-2-27.
 */
public interface ListProcessor<T> {

    /**
     * Called at the start of processing 1 list of records (i.e. a page)
     * @param recordsList
     */
    public void processListBegin(T recordsList);

    /**
     * Called when processing 1 list of records (i.e. a page) has finished
     * @param recordsList
     */
    public void processListEnd(T recordsList);

    /**
     * Called when processing all lists (pages) are done.
     */
    public void processListFinish();

    /**
     * Called when an exception occurred while processing lists (pages)
     * @param e
     */
    public void processListError(Exception e);
}
