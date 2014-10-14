package com.ontotext.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Simo on 13.10.2014 г..
 */
public class ByteArrayOutputStream2 extends ByteArrayOutputStream {
    public ByteArrayOutputStream2(int size) {
        super(size);
    }

    public ByteArrayInputStream toInputStream() {
        return new ByteArrayInputStream(buf, 0, count );
    }
}
