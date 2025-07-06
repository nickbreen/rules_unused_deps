package kiwi.breen.unused.deps;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FixtureTest
{
    @Test
    public void shouldHello()
    {
        final Fixture object = new Fixture();
        assertNotNull("a fake test", object);
        assertTrue("a fake test", true);
    }
}