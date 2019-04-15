package cruft.wtf.gimlet.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LimitedSizePriorityQueueTest {

    @Test
    public void all() {
        LimitedSizePriorityQueue<String> s = new LimitedSizePriorityQueue<>(5);
        s.push("first");
        s.push("second");
        s.push("third");
        s.push("fourth");
        s.push("fifth");
        s.push("sixth");
        s.push("seventh");
        s.push("eighth");
        s.push("ninth");

        s.push("eighth"); // this should now be the first again.

        assertEquals(5, s.size());

        assertEquals("eighth", s.pop());
        assertEquals("ninth", s.pop());
        assertEquals("seventh", s.pop());
        assertEquals("sixth", s.pop());
        assertEquals("fifth", s.pop());

        assertEquals(0, s.size());
    }
}