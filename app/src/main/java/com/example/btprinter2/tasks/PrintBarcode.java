package com.example.btprinter2.tasks;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import com.example.btprinter2.utils.ByteHelper;

public class PrintBarcode extends Thread {

    private final BluetoothSocket mSocket;
    private final OutputStream mOutputStream;
    private final String mBarcodeValue;

    public PrintBarcode(BluetoothSocket socket, String barcodeValue) {
        mSocket = socket;
        mBarcodeValue = barcodeValue;
        OutputStream temporaryOutput = null;
        try {
            temporaryOutput = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutputStream = temporaryOutput;
    }

    private void sendCommand(final int command){

        try {
            mOutputStream.write(ByteHelper.commandByteRepresentation(command));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendGSCommand() {
        final int gsCommand = 29;

        try {
            mOutputStream.write(ByteHelper.commandByteRepresentation(gsCommand));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendPrintBarcodeCommand() {
        final int barcodeLength = mBarcodeValue.length();

        try {
            mOutputStream.write(ByteHelper.commandByteRepresentation(107));  // = k = 0x6b
            mOutputStream.write(ByteHelper.commandByteRepresentation(73));  //= I = 0x49
            mOutputStream.write(ByteHelper.commandByteRepresentation(barcodeLength));

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mBarcodeValue.length(); i++) {

            try {
                mOutputStream.write((mBarcodeValue.charAt(i) + "").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        //Setting tab don't need a GS command
        //sendCommand(mBarcode.getHorizontalTab());

        //sendGSCommand();
        //sendCommand(mBarcode.getHeightCommandCode());
        //sendCommand(mBarcode.getHeight());

        //sendGSCommand();
        //sendCommand(mBarcode.getWidthCommandCode());
        //sendCommand(mBarcode.getWidth());

        sendGSCommand();
        sendPrintBarcodeCommand();
    }
}
