package com.example.btprinter2.utils;

public class PrintCMD {

    public static byte[] setBarcodeHeight(int pIntHeight)
    {
        //GS h n
        //final byte contents = (byte) 0x78;
        final byte contents = ByteHelper.commandByteRepresentation(pIntHeight);
        final byte[] formats  = {(byte) 0x1d, (byte) 0x68, contents};

        return formats;
    }

    public static byte[] printAsBarcode(String pStrValue)
    {
        //BILL = "123321";
        final byte[] contents = pStrValue.getBytes();
        // include the content length after the mode selector (0x49) = 73 = CODE128
        final byte[] formats  = {(byte) 0x1d, (byte) 0x6b, (byte) 0x49, (byte)contents.length};

        byte[] bytes    = new byte[formats.length + contents.length];

        System.arraycopy(formats, 0, bytes, 0, formats.length);
        System.arraycopy(contents, 0, bytes, formats.length, contents.length);

        // add a terminating NULL
        //bytes[formats.length + contents.length] = (byte) 0x00;
        return bytes;
    }

    public static byte[] printAsPDF417(String pStrValue)
    {
        final byte[] contents = pStrValue.getBytes();
        final byte length = (byte) contents.length;    //ByteHelper.commandByteRepresentation(10);
        final byte height = ByteHelper.commandByteRepresentation(0);
        final byte[] formats  = {(byte) 0x1b, (byte) 0x1d, (byte) 0x78, (byte) 0x44, length, height};

        byte[] bytes    = new byte[formats.length + contents.length];

        System.arraycopy(formats, 0, bytes, 0, formats.length);
        System.arraycopy(contents, 0, bytes, formats.length, contents.length);

        // add a terminating NULL
        //bytes[formats.length + contents.length] = (byte) 0x00;
        return bytes;
    }


    public static byte[] printAsBarcode2D(String pStrValue)
    {
        final byte[] contents = pStrValue.getBytes();
        final byte length = (byte) contents.length;
        final byte height = ByteHelper.commandByteRepresentation(0);
        final byte[] formats  = { PrinterCommands.GS, (byte) 0x28, (byte) 0x4c, length, height, ByteHelper.commandByteRepresentation(48), ByteHelper.commandByteRepresentation(50)};

        byte[] bytes    = new byte[formats.length + contents.length];

        System.arraycopy(formats, 0, bytes, 0, formats.length);
        System.arraycopy(contents, 0, bytes, formats.length, contents.length);

        return bytes;
    }
}
