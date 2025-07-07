package kiwi.breen.unused.deps;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Fixture
{
    public static void main(String[] args)
    {
        final Logger logger = getLogger(Fixture.class);
        logger.info("hello {}", String.join(" ", args));
    }
}