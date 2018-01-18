package cruft.wtf.gimlet.conf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class QueryTest {

    @Test
    public void copyConstructor() {
        Query query = new Query();
        query.setName("Parent query");
        query.setDescription("Parent description");
        query.setContent("Parent content");
        {
            Query child = new Query();
            child.setName("Child 1");
            child.setDescription("Child 1 description");
            child.setContent("Child 1 content");
            query.addSubQuery(child);
            {
                Query grandchild = new Query();
                grandchild.setName("Grandchild query");
                grandchild.setDescription("Grandchild description");
                grandchild.setContent("Grandchild content");
                child.addSubQuery(grandchild);
            }
        }
        {
            Query child = new Query();
            child.setName("Child 2");
            child.setDescription("Child 2 description");
            child.setContent("Child 2 content");
            query.addSubQuery(child);
        }

        assertValues(query);

        Query deepCopy = new Query(query);
        assertValues(deepCopy);

        assertDifference(query, deepCopy);
    }

    private void assertValues(final Query query) {
        assertEquals(2, query.getSubQueries().size());
        assertEquals(1, query.getSubQueries().get(0).getSubQueries().size());
        assertEquals("Grandchild query", query.getSubQueries().get(0).getSubQueries().get(0).getName());
        assertEquals("Child 1", query.getSubQueries().get(0).getName());
        assertEquals("Child 1 description", query.getSubQueries().get(0).getDescription());
        assertEquals(0, query.getSubQueries().get(1).getSubQueries().size());
    }

    private void assertDifference(final Query original, final Query copy) {
        assertNotEquals(original.hashCode(), copy.hashCode());
        assertNotEquals(original.getSubQueries().get(0).hashCode(), copy.getSubQueries().get(0).hashCode());
        assertNotEquals(original.getSubQueries().get(1).hashCode(), copy.getSubQueries().get(1).hashCode());
    }
}