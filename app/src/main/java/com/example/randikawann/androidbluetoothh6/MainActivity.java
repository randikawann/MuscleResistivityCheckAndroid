package com.example.randikawann.androidbluetoothh6;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.io.FileOutputStream;
import java.io.FileWriter;


public class MainActivity extends AppCompatActivity {




    //    private final String DEVICE_NAME="MyBTBee";
    private final String DEVICE_ADDRESS="20:15:07:27:46:85";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//Serial Port Service ID
    private static final String TAG = "MyActivity";
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    Button startButton, sendButton,clearButton,stopButton;
    TextView textView;
    EditText editText;
    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    String string;
    private static final String FILE_NAME = "example.txt";
    String name1;
    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        clearButton = (Button) findViewById(R.id.buttonClear);
        stopButton = (Button) findViewById(R.id.buttonStop);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        graph = (GraphView) findViewById(R.id.graph);
        setUiEnabled(false);

    }

    public void setUiEnabled(boolean bool)
    {
        startButton.setEnabled(!bool);
        sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        textView.setEnabled(bool);

    }

    public boolean BTinit()
    {

        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
            Toast.makeText(getApplicationContext(),"Bluetooth adapter not anabled",Toast.LENGTH_SHORT).show();
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        Toast.makeText(getApplicationContext(),"find bounded device",Toast.LENGTH_SHORT).show();
        if(bondedDevices.isEmpty())
        {
            Log.i(TAG, "bound is empty");
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {

            //Log.i(TAG, "bound device have");
            for (BluetoothDevice iterator : bondedDevices)
            {
                //Log.i(TAG, "bounded devices for");
                //if(iterator.getAddress().equals(DEVICE_ADDRESS))
                //{
                    Log.i(TAG, "found device");
                    device=iterator;
                    found=true;
                    break;
                //}


                //Log.i(TAG, "get Address "+iterator.getAddress().toString());
                //Log.i(TAG, DEVICE_ADDRESS.toString());

            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        //Log.i(TAG, "BTconnect begining");

        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    public void onClickStart(View view) {
        if(BTinit())
        {
            if(BTconnect())
            {
                Log.i(TAG, "If BTconnect");
                setUiEnabled(true);
                deviceConnected=true;
                beginListenForData();
                textView.append("\nConnection Opened!\n");
            }

        }
    }

    void beginListenForData()
    {

        Log.i(TAG, "Begining listner data");
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];

        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            final byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            string = new String(rawBytes, "UTF-8");

                            handler.post(new Runnable() {
                                public void run() {
                                    textView.setText(string);
                                    //Log.i(TAG, "***************************" + string);
                                    Log.i(TAG, "***************************" + rawBytes);
                                //add line graph
                                    makeGraph(string);

                                    //store value from file
                                    save();

                                }


                            });

                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
    // make graph
    public void makeGraph(String string){
        int x=0;
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(x++, Integer.parseInt(this.string))

        });
        graph.addSeries(series);
    }
    //Save input value
    public void save() {
        String text = string;
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(text.getBytes());

            //Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME,
              //      Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //Read saved files
    public void load() {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            editText.setText(sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onClickSend(View view) {
        String string = editText.getText().toString();
        string.concat("\n");
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.append("\nSent Data:"+string+"\n");

    }

    public void onClickStop(View view) throws IOException {
        Log.i(TAG, "File stop");
        //load();
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        setUiEnabled(false);
        deviceConnected=false;
        textView.append("\nConnection Closed!\n");
    }

    public void onClickClear(View view) {
        Log.i(TAG, "File view");
        load();
    }

    }


