package com.ontotext.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by Simo on 29.7.2014 г..
 */
public class Util {
    private static final Log log = LogFactory.getLog(Util.class);

    public static Properties loadProperties() throws IOException {

        String configFileName = System.getProperty("config", "client.properties");
        File configFile = new File(configFileName);
        InputStream input;
        if (configFile.isFile()) {
            log.debug("Load config file: " + configFileName);
            input = new FileInputStream(configFile);
        } else {
            log.info("No file specified in 'config' system property. Loading default...");
            input = Util.class.getResourceAsStream("/client.properties");
            if (input == null) {
                throw new FileNotFoundException(configFileName);
            }
        }

        Properties properties = new Properties();
        try {
            properties.load(input);
        } finally {
            input.close();
        }

        return properties;
    }

}
