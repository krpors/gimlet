package cruft.wtf.gimlet.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * This class limits the size of the elements by evicting the last element if the size exceeds {@link #maxSize}.
 * If an element is added which already exists (it does this by invoking the equals method), it is first removed
 * from the list, and then pushed on the deque so it comes first.
 * <p>
 * The class is backed by a {@link Deque}, and provides some delegate methods to interact with it.
 *
 * @param <E> The element type.
 */
public class LimitedSizePriorityQueue<E> {

    private Deque<E> backingQueue = new ArrayDeque<>();

    private final int maxSize;

    public LimitedSizePriorityQueue(int maxSize) {

        this.maxSize = maxSize;
    }

    public void addLast(E e) {
        backingQueue.addLast(e);
    }

    /**
     * Push an object on the stack. If the element already exists, the element
     * is first removed from the stack, and re-added to the top, so it gets priority.
     *
     * @param e
     */
    public void push(E e) {
        // Remove e if it already is present in the deque.
        backingQueue.removeIf(e1 -> e1.equals(e));

        // Re-add it again at the start of the queue.
        backingQueue.push(e);

        // If the size exceeds, remove the last in priority.
        if (backingQueue.size() > maxSize) {
            backingQueue.removeLast();
        }
    }

    public int size() {
        return backingQueue.size();
    }

    public E pop() {
        return backingQueue.pop();
    }

    public void forEach(Consumer<? super E> action) {
        backingQueue.forEach(action);
    }
}
