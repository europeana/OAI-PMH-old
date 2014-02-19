package process;

/**
 * Created by Simo on 14-1-30.
 */
public interface Processor<T> {
    void process(T t);
    Object total();
}
