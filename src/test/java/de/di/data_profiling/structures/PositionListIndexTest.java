package de.di.data_profiling.structures;

import org.junit.Test;

import static org.junit.Assert.*;

public class PositionListIndexTest {

    @Test
    public void testIntersectionCorrectness() {
        PositionListIndex pli1 = new PositionListIndex(new AttributeList(new int[]{0}), new String[]{"a", "a", "a", "b", "b", "a"});
        PositionListIndex pli2 = new PositionListIndex(new AttributeList(new int[]{1}), new String[]{"a", "a", "b", "b", "b", "b"});
        PositionListIndex pli3 = new PositionListIndex(new AttributeList(new int[]{2}), new String[]{"a", "a", "b", "c", "c", "b"});
        PositionListIndex pli4 = new PositionListIndex(new AttributeList(new int[]{3}), new String[]{"a", "b", "c", "d", "e", "f"});
        PositionListIndex pli5 = new PositionListIndex(new AttributeList(new int[]{4}), new String[]{"a", "c", "a", "b", "a", "c"});
        PositionListIndex pli6 = new PositionListIndex(new AttributeList(new int[]{4}), new String[]{"a", "c", "a", "b", "b", "c"});
        PositionListIndex pli7 = new PositionListIndex(new AttributeList(new int[]{4}), new String[]{"a", "b", "c", "d", "d", "e"});

        assertEquals(pli1.getClusters(), pli1.intersect(pli1).getClusters());
        assertEquals(pli3.getClusters(), pli1.intersect(pli2).getClusters());
        assertEquals(pli3.getClusters(), pli2.intersect(pli1).getClusters());
        assertEquals(pli4.getClusters(), pli1.intersect(pli2).intersect(pli5).getClusters());
        assertEquals(pli7.getClusters(), pli1.intersect(pli6).intersect(pli3).getClusters());
    }

    @Test
    public void testNullValueNotClustered() {
        PositionListIndex pli1 = new PositionListIndex(new AttributeList(new int[]{0}), new String[]{"a","a","b","b","c"});
        PositionListIndex pli2 = new PositionListIndex(new AttributeList(new int[]{0}), new String[]{"a","a","a","b","b"});
        PositionListIndex pli3 = new PositionListIndex(new AttributeList(new int[]{0}), new String[]{"c","d","a","a","a"});

        PositionListIndex pli12 = pli1.intersect(pli2);
        PositionListIndex pli13 = pli1.intersect(pli3);PositionListIndex pli123 = pli12.intersect(pli13);

        assertFalse(pli12.isUnique());
        assertFalse(pli13.isUnique());
        assertTrue(pli123.isUnique());
    }
}
