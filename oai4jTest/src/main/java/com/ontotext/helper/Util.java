package com.ontotext.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simo on 29.7.2014 г..
 */
public class Util {
    public static Properties loadProperties(String[] args) throws IOException {
        Properties properties = new Properties();
        final String fileName = (args.length == 0) ? "client.properties" : args[0];
        FileInputStream input = new FileInputStream(fileName);
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            input.close();
        }

        return properties;
    }

}
