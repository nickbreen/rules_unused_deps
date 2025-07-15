package example;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class E2ESmoke
{
    public static void main(String[] args)
    {
        final Logger logger = getLogger(E2ESmoke.class);
        logger.info("hello {}", String.join(" ", args));
    }
}