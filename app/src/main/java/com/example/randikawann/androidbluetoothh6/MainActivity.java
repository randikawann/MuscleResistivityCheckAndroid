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
import java.lang.reflect.Array;
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
    int valueAll;
    int x=0;
    private static final String FILE_NAME = "example.txt";
    String name1;
    GraphView graph;
    int arr[] ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        clearButton = (Button) findViewById(R.id.buttonClear);
        stopButton = (Button) findViewById(R.id.buttonStop);
        //editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        graph = (GraphView) findViewById(R.id.graph);
        setUiEnabled(false);
        arr=new int[100];
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
//                            valueAll = new Integer(rawBytes,);
                            try{
                                valueAll=Integer.valueOf(string);
                            }catch (Exception e){}

                            Log.i(TAG, "***************************" + valueAll);
                            handler.post(new Runnable() {
                                public void run() {
                                    textView.setText(string);


                                   // Log.i(TAG, "***************************" + rawBytes);

                                //add line graph

                                    //int x=0;
                                    try{
                                    arr[x]=valueAll;
                                    x++;
                                    }catch(Exception e){}
                                }


                            });
//                            Toast.makeText(getApplicationContext(),"go to make graph",Toast.LENGTH_SHORT).show();
//                            makeGraph(arr);
                            //store value from file
                            //save();


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
    public void makeGraph(int[] arr){
        try{
            graph.getViewport().setScrollable(true); // enables horizontal scrolling
            graph.getViewport().setScrollableY(true); // enables vertical scrolling
            graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
            graph.getViewport().setScalableY(true); //

        }catch (Exception e){}
        for (int i=0;i<arr.length;i++){
            new DataPoint(i,arr[i]);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, arr[1]),
                new DataPoint(2, arr[2]),
                new DataPoint(3, arr[3]),
                new DataPoint(4, arr[4]),
                new DataPoint(5, arr[5]),

                new DataPoint(6, arr[6]),
                new DataPoint(7, arr[7]),
                new DataPoint(8, arr[8]),
                new DataPoint(9, arr[9]),
                new DataPoint(10, arr[10]),

                new DataPoint(11, arr[11]),
                new DataPoint(12, arr[12]),
                new DataPoint(13, arr[13]),
                new DataPoint(14, arr[14]),
                new DataPoint(15, arr[15]),

                new DataPoint(16, arr[16]),
                new DataPoint(17, arr[17]),
                new DataPoint(18, arr[18]),
                new DataPoint(19, arr[19]),
                new DataPoint(20, arr[20]),

                new DataPoint(21, arr[21]),
                new DataPoint(22, arr[22]),
                new DataPoint(23, arr[23]),
                new DataPoint(24, arr[24]),
                new DataPoint(25, arr[25]),

                new DataPoint(26, arr[26]),
                new DataPoint(27, arr[27]),
                new DataPoint(28, arr[28]),
                new DataPoint(29, arr[29]),
                new DataPoint(30, arr[30]),

                new DataPoint(31, arr[31]),
                new DataPoint(32, arr[32]),
                new DataPoint(33, arr[33]),
                new DataPoint(34, arr[34]),
                new DataPoint(35, arr[35]),

                new DataPoint(36, arr[36]),
                new DataPoint(37, arr[37]),
                new DataPoint(38, arr[38]),
                new DataPoint(39, arr[39]),
                new DataPoint(40, arr[40]),

                new DataPoint(41, arr[41]),
                new DataPoint(42, arr[42]),
                new DataPoint(43, arr[43]),
                new DataPoint(44, arr[44]),
                new DataPoint(45, arr[45]),

                new DataPoint(46, arr[46]),
                new DataPoint(47, arr[47]),
                new DataPoint(48, arr[48]),
                new DataPoint(49, arr[49]),
                new DataPoint(50, arr[50]),

                new DataPoint(51, arr[51]),
                new DataPoint(52, arr[52]),
                new DataPoint(53, arr[53]),
                new DataPoint(54, arr[54]),
                new DataPoint(55, arr[55]),

                new DataPoint(56, arr[56]),
                new DataPoint(57, arr[57]),
                new DataPoint(58, arr[58]),
                new DataPoint(59, arr[59]),
                new DataPoint(60, arr[60]),

                new DataPoint(61, arr[61]),
                new DataPoint(62, arr[62]),
                new DataPoint(63, arr[63]),
                new DataPoint(64, arr[64]),
                new DataPoint(65, arr[65]),

                new DataPoint(66, arr[66]),
                new DataPoint(67, arr[67]),
                new DataPoint(68, arr[68]),
                new DataPoint(69, arr[69]),
                new DataPoint(70, arr[70]),

                new DataPoint(71, arr[71]),
                new DataPoint(72, arr[72]),
                new DataPoint(73, arr[73]),
                new DataPoint(74, arr[74]),
                new DataPoint(75, arr[75]),

                new DataPoint(76, arr[76]),
                new DataPoint(77, arr[77]),
                new DataPoint(78, arr[78]),
                new DataPoint(79, arr[79]),
                new DataPoint(80, arr[80]),

                new DataPoint(81, arr[81]),
                new DataPoint(82, arr[82]),
                new DataPoint(83, arr[83]),
                new DataPoint(84, arr[84]),
                new DataPoint(85, arr[85]),

                new DataPoint(86, arr[86]),
                new DataPoint(87, arr[87]),
                new DataPoint(88, arr[88]),
                new DataPoint(89, arr[89]),
                new DataPoint(90, arr[90]),

                new DataPoint(91, arr[91]),
                new DataPoint(92, arr[92]),
                new DataPoint(93, arr[93]),
                new DataPoint(94, arr[94]),
                new DataPoint(95, arr[95]),

                new DataPoint(96, arr[96]),
                new DataPoint(97, arr[97]),
                new DataPoint(98, arr[98]),
                new DataPoint(99, arr[99]),
//                new DataPoint(100, arr[100])

        });

        graph.addSeries(series);
        textView.setText(arr[1]+" , "+arr[2]+" , "+arr[3]+" , "+arr[4]+" , "+arr[5]+" , "+arr[6]+" , "+arr[7]+" , "+arr[8]+" , "+arr[9]+" , "+arr[10]+" , "+
                arr[11]+" , "+arr[12]+" , "+arr[13]+" , "+arr[14]+" , "+arr[15]+" , "+arr[16]+" , "+arr[17]+" , "+arr[18]+" , "+arr[19]+" , "+arr[20]+" , "+
                arr[21]+" , "+arr[22]+" , "+arr[23]+" , "+arr[24]+" , "+arr[25]+" , "+arr[26]+" , "+arr[27]+" , "+arr[28]+" , "+arr[29]+" , "+arr[30]+" , "+
                arr[31]+" , "+arr[32]+" , "+arr[33]+" , "+arr[34]+" , "+arr[35]+" , "+arr[36]+" , "+arr[37]+" , "+arr[38]+" , "+arr[39]+" , "+arr[40]+" , "+
                arr[41]+" , "+arr[42]+" , "+arr[43]+" , "+arr[44]+" , "+arr[45]+" , "+arr[46]+" , "+arr[47]+" , "+arr[48]+" , "+arr[49]+" , "+arr[50]+" , "+
                arr[51]+" , "+arr[52]+" , "+arr[53]+" , "+arr[54]+" , "+arr[55]+" , "+arr[56]+" , "+arr[57]+" , "+arr[58]+" , "+arr[59]+" , "+arr[60]+" , "+
                arr[61]+" , "+arr[62]+" , "+arr[63]+" , "+arr[64]+" , "+arr[65]+" , "+arr[66]+" , "+arr[67]+" , "+arr[68]+" , "+arr[69]+" , "+arr[70]+" , "+
                arr[71]+" , "+arr[72]+" , "+arr[73]+" , "+arr[74]+" , "+arr[75]+" , "+arr[76]+" , "+arr[77]+" , "+arr[78]+" , "+arr[79]+" , "+arr[80]+" , "+
                arr[81]+" , "+arr[82]+" , "+arr[83]+" , "+arr[84]+" , "+arr[85]+" , "+arr[86]+" , "+arr[87]+" , "+arr[88]+" , "+arr[89]+" , "+arr[90]
        );
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
//        String string = editText.getText().toString();
//        string.concat("\n");
//        try {
//            outputStream.write(string.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        textView.append("\nSent Data:"+string+"\n");

    }

    public void onClickStop(View view) throws IOException {
        textView.setText(" ");
        Toast.makeText(getApplicationContext(),"go to make graph",Toast.LENGTH_SHORT).show();
        makeGraph(arr);
        //displayValue(arr);
        Log.i(TAG, "File stop");
        //load();
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        setUiEnabled(false);
        deviceConnected=false;
        //textView.append("\nConnection Closed!\n");
    }

    public void onClickClear(View view) {
        Log.i(TAG, "File view");
        load();
    }

    public void displayValue(int[] arr){

        textView.setText(arr.toString());
//        for(int i=0; i<arr.length;i++){
//            textView.setText(arr[i]);
//        }
    }

    }


