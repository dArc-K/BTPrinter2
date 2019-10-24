package com.example.btprinter2.utils;

import java.nio.ByteBuffer;
public class ByteHelper {
    //Prepare data for printer representation
    public static byte commandByteRepresentation(final int command) {

        final byte[] bytes = ByteBuffer
                .allocate(4)
                .putInt(command)
                .array();

        return bytes[3];
    }

}