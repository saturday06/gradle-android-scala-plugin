package jp.leafytree.android.hello;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import scala.collection.concurrent.TrieMap;

public class HelloJavaTest {

    @Test
    public void simpleAssertion() {
        assertTrue(true);
    }

    @Test
    public void simpleActivityAssertion() {
        assertEquals(3, new HelloJava().increment(2));
    }
}
