package example;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class E2ESmokeTest
{
    @Test
    public void shouldHello()
    {
        final E2ESmoke object = new E2ESmoke();
        assertNotNull("a fake test", object);
        assertTrue("a fake test", true);
    }
}