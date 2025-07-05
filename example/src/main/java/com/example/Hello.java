package com.example;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Hello
{
    public static void main(String[] args)
    {
        final Logger logger = getLogger(Hello.class);
        logger.info("hello {}", String.join(" ", args));
    }
}