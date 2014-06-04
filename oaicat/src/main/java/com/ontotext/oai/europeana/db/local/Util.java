package com.ontotext.oai.europeana.db.local;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Simo on 14-1-16.
 */
public class Util {
    public static void saveFileNoThrow(File file, String data) {
        if (file == null || data == null) {
            return;
        }

        try {
            FileUtils.writeStringToFile(file, data, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadFileNoThrow(File file) {
        if (file != null && file.exists()) {
            try {
                return FileUtils.readFileToString(file, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
