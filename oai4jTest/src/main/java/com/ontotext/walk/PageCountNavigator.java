package walk;

import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-1-30.
 */
public class PageCountNavigator implements Navigator<RecordsList> {
    int counter = 0;
    final int maxCount;
    public PageCountNavigator(int count) {
        this.maxCount = count;
    }

    public void check(RecordsList recordsList) {
        if (counter != maxCount) {
            ++counter;
        }
    }

    public boolean shouldStop() {
        return counter == maxCount;
    }
}
