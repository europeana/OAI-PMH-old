package com.ontotext.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Simo on 29.7.2014 г..
 */
public class Util {
    public static Properties loadProperties() throws IOException {
        InputStream input = Util.class.getResourceAsStream("/client.properties");
        if (input == null) {
            throw new FileNotFoundException("client.properties");
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
