package com.example.btprinter2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.btprinter2.utils.PrintCMD;
import com.example.btprinter2.utils.PrinterCommands;
import com.example.btprinter2.utils.QRCodeHelper;
import com.example.btprinter2.utils.Utils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;

public class MainActivity extends AppCompatActivity implements Runnable {
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    Button mScan, mPrint, mDisc, mBtnCheckWB;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    TextView mTVDescription;
    EditText mETWaybillId, mETReceiver, mETSender;
    RadioButton mRB58mm, mRB80mm;
    ImageView mIVQRCode;

    @Override
    public void onCreate(Bundle mSavedInstanceState) {
        super.onCreate(mSavedInstanceState);
        setContentView(R.layout.activity_main);

        mTVDescription = findViewById(R.id.tvDescription);
        mETWaybillId = findViewById(R.id.etWaybillId);
        mETReceiver = findViewById(R.id.etReceiver);
        mETSender = findViewById(R.id.etSender);
        mRB58mm = findViewById(R.id.rb58mm);
        mRB80mm = findViewById(R.id.rb80mm);
        mIVQRCode = findViewById(R.id.ivQRCode);

        mBtnCheckWB = findViewById(R.id.btnCheckWB);
        mBtnCheckWB.setOnClickListener(new View.OnClickListener() {
                public void onClick(View vw) {
                    //showQRCode showqrcd = new showQRCode();
                    //showqrcd.execute(mETWaybillId.getText().toString());
                    //mIVQRCode.setImageBitmap(showqrcd.getBitmap());
                    try {
                        Bitmap bitmap = QRCodeHelper.encodeAsBitmap(mETWaybillId.getText().toString(), BarcodeFormat.QR_CODE, 300, 300);
                        if (bitmap != null) {
                            mIVQRCode.setImageBitmap(bitmap);
                        } else {
                            mTVDescription.setText("Bitmap null");
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Exe ", e);
                    }
                }
        });

        mScan = findViewById(R.id.Scan);
        mScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(MainActivity.this, "Message1", Toast.LENGTH_SHORT).show();
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                    } else {
                        ListPairedDevices();
                        Intent connectIntent = new Intent(MainActivity.this,
                                DeviceListActivity.class);
                        startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                    }
                }
            }
        });

        mPrint = (Button) findViewById(R.id.mPrint);
        mPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            OutputStream os = mBluetoothSocket.getOutputStream();

                            //--> DAR
                            String content = "";

                            //content = String.format("%1$-10s \n", mETWaybillId.getText().toString());
                            //os.write(content.getBytes());
/*
                            //  MOVE TO PrintAsBarcode()
                            os.write(printBill());
                            BILL = "123321";
                            byte[] contents = BILL.getBytes();
                            // include the content length after the mode selector (0x49) = 73 = CODE128
                            byte[] formats  = {(byte) 0x1d, (byte) 0x6b, (byte) 0x49, (byte)contents.length};

                            byte[] bytes    = new byte[formats.length + contents.length];

                            System.arraycopy(formats, 0, bytes, 0, formats.length);
                            System.arraycopy(contents, 0, bytes, formats.length, contents.length);

                            // add a terminating NULL
                            //bytes[formats.length + contents.length] = (byte) 0x00;

                            os.write(bytes);
*/

                            // for 58mm, printable barcode length is 8chars
                            boolean isPrintBarcode = false;
                            if (mRB80mm.isSelected()) {
                                isPrintBarcode = true;
                            } else {
                                if (mETWaybillId.getText().length() <= 8) {
                                    isPrintBarcode = true;
                                }
                            }
                            if (isPrintBarcode) {
                                os.write(PrintCMD.setBarcodeHeight(110));
                                //os.write(PrintCMD.printAsBarcode("121121"));
                                os.write(PrintCMD.printAsBarcode(mETWaybillId.getText().toString()));
                            } else {
                                /* for printing as PDF417
                                os.write(PrintCMD.printAsPDF417(mETWaybillId.getText().toString()));
                                os.write(PrinterCommands.PRINT_PDF417);
                                */

                                Bitmap bitmap = QRCodeHelper.encodeAsBitmap(mETWaybillId.getText().toString(), BarcodeFormat.QR_CODE, 200, 200);
                                if (bitmap!=null) {
                                    //os.write(PrinterCommands.SELECT_BIT_IMAGE_MODE);
                                    byte[] command = Utils.decodeBitmap(bitmap);
                                    os.write(command);
                                    os.write(PrintCMD.printAsBarcode2D(mETWaybillId.getText().toString()));
                                    os.write(PrinterCommands.FEED_LINE);
                                }
                            }
                            //content = String.format("\n %1$s", mETWaybillId.getText().toString());
                            content = String.format("%1$s", mETSender.getText().toString());
                            content = content + String.format("\n%1$s", mETReceiver.getText().toString());
                            content = content + "\n\n --- BTPrinter 2 --\n";
                            os.write(content.getBytes());
                            //<-- DAR

                            //os.write(BILL.getBytes());

                            //This is printer specific code you can comment ==== > Start
/*
                            // Setting height
                            int gs = 29;
                            os.write(intToByteArray(gs));
                            int h = 104;
                            os.write(intToByteArray(h));
                            int n = 162;
                            os.write(intToByteArray(n));

                            // Setting Width
                            int gs_width = 29;
                            os.write(intToByteArray(gs_width));
                            int w = 119;
                            os.write(intToByteArray(w));
                            int n_width = 2;
                            os.write(intToByteArray(n_width));
*/
                            os.flush();
                        } catch (Exception e) {
                            Log.e("MainActivity", "Exe ", e);
                        }
                    }
                };
                t.start();
            }
        });

        mDisc = (Button) findViewById(R.id.dis);
        mDisc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.disable();
            }
        });

    }// onCreate

    private class showQRCode extends AsyncTask<String, Void, Bitmap> {

        private Bitmap mBitmap = null;

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                mBitmap = QRCodeHelper.encodeAsBitmap(strings[0], BarcodeFormat.QR_CODE, 300, 300);
                //Bitmap bitmap = QRCodeHelper.encodeAsBitmap(strings[0], BarcodeFormat.QR_CODE, 300, 300);
                /*if (bitmap != null) {
                    mIVQRCode.setImageBitmap(bitmap);
                } else {
                    mTVDescription.setText("Bitmap null");
                }*/
            } catch (Exception e) {
                Log.e("MainActivity", "Exe ", e);
            }
            return mBitmap;
        }

        public Bitmap getBitmap()
        {
            return mBitmap;
        }
    }

    private byte[] printBill()
    {
        String BILL = "";

        BILL = "                   XXXX MART    \n"
                + "                   XX.AA.BB.CC.     \n " +
                "                 NO 25 ABC ABCDE    \n" +
                "                  XXXXX YYYYYY      \n" +
                "                   MMM 590019091      \n";
        BILL = BILL + "-----------------------------------------------\n";

        BILL = BILL + String.format("%1$-10s %2$10s %3$13s %4$10s", "Item", "Qty", "Rate", "Totel");
        BILL = BILL + "\n";
        BILL = BILL
                + "-----------------------------------------------";
        BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-001", "5", "10", "50.00");
        BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-002", "10", "5", "50.00");
        BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-003", "20", "10", "200.00");
        BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-004", "50", "10", "500.00");

        BILL = BILL
                + "\n-----------------------------------------------";
        BILL = BILL + "\n\n ";

        BILL = BILL + "                   Total Qty:" + "      " + "85" + "\n";
        BILL = BILL + "                   Total Value:" + "     " + "700.00" + "\n";

        BILL = BILL
                + "-----------------------------------------------\n";
        BILL = BILL + "\n\n ";

        return BILL.getBytes();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);

        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v(TAG, "Coming incoming address " + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mBluetoothConnectProgressDialog = ProgressDialog.show(this,
                            "Connecting...", mBluetoothDevice.getName() + " : "
                                    + mBluetoothDevice.getAddress(), true, false);
                    Thread mBlutoothConnectThread = new Thread(this);
                    mBlutoothConnectThread.start();
                    // pairToDevice(mBluetoothDevice); This method is replaced by
                    // progress dialog with thread
                }
                break;

            case REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(MainActivity.this, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.v(TAG, "PairedDevices: " + mDevice.getName() + "  "
                        + mDevice.getAddress());
            }
        }
    }

    public void run() {
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(MainActivity.this, "DeviceConnected", Toast.LENGTH_SHORT).show();
        }
    };

    public byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();
        String description = "";
        for (int k = 0; k < b.length; k++) {
            description = "Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]) + "\n";
            System.out.println(description);
        }
        Toast.makeText(MainActivity.this, description, Toast.LENGTH_SHORT).show();
        return b[3];
    }

    public byte[] sel(int val) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putInt(val);
        buffer.flip();
        return buffer.array();
    }

}
