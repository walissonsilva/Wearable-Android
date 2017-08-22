package com.example.walisson.wearable_bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;

    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;

    ConnectedThread connectedThread;

    android.os.Handler mHandler;

    StringBuilder dadosBluetooth = new StringBuilder();

    String jsonFreq = "https://api.thingspeak.com/update?api_key=NWXY4YCIOOSUS6XV&field1=";
    String jsonTemp = "https://api.thingspeak.com/update?api_key=NWXY4YCIOOSUS6XV&field2=";

    Button searchDevice;
    EditText info_freq, info_temp;

    boolean conexao = false;

    private static String MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startActivity(abreLogin);

        //ws.setJavaScriptEnabled(true);
        //ws.setSupportZoom(false);

        //wv.loadUrl("http://www.google.com.br");

        searchDevice = (Button) findViewById(R.id.search_device);
        info_freq = (EditText) findViewById(R.id.freq);
        info_temp = (EditText) findViewById(R.id.temp);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui Bluetooth.", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth, SOLICITA_ATIVACAO);
        }

        searchDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (conexao){
                    // desconectar
                    try {
                        meuSocket.close();
                        conexao = false;
                        searchDevice.setText("Procurar Dispositivos");
                        Toast.makeText(getApplicationContext(), "Bluetooth desconectado!", Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Erro ao desconectar!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // conectar
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXAO);
                }
            }
        });

        mHandler = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ){
                    //Toast.makeText(getApplicationContext(), "Recebi!", Toast.LENGTH_LONG).show();
                    String recebido = (String) msg.obj;
                    dadosBluetooth.append(recebido);

                    int finalFreq = dadosBluetooth.indexOf("f");
                    int finalTemp = dadosBluetooth.indexOf("t");

                    WebView wv = (WebView) findViewById(R.id.web_view);
                    WebSettings ws = wv.getSettings();

                    if (finalFreq > 0){
                        String freqCompleto = dadosBluetooth.substring(0, finalFreq);
                        int tamInformacao = freqCompleto.length();

                        if (dadosBluetooth.charAt(0) == 'F'){
                            String freqFinal = dadosBluetooth.substring(1, tamInformacao);
                            info_freq.setText(freqFinal + " BPM");
                            wv.loadUrl(jsonFreq + freqFinal);
                            //Toast.makeText(getApplicationContext(), dadosFinal, Toast.LENGTH_LONG).show();
                        }

                        dadosBluetooth.delete(0, dadosBluetooth.length());
                    } else if (finalTemp > 0){
                        String tempCompleto = dadosBluetooth.substring(0, finalTemp);
                        int tamInformacao = tempCompleto.length();

                        if (dadosBluetooth.charAt(0) == 'T'){
                            String tempFinal = dadosBluetooth.substring(1, tamInformacao);
                            info_temp.setText(tempFinal + " °C");
                            wv.loadUrl(jsonTemp + tempFinal);
                            //Toast.makeText(getApplicationContext(), dadosFinal, Toast.LENGTH_LONG).show();
                        }

                        dadosBluetooth.delete(0, dadosBluetooth.length());
                    }

                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (SOLICITA_ATIVACAO):
                if (resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(), "O Bluetooth foi ativado!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth NÃO foi ativado! Seu aplicativo foi encerrado.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case (SOLICITA_CONEXAO):
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);

                    meuDevice = mBluetoothAdapter.getRemoteDevice(MAC);

                    try {
                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Erro ao tentar o UUID!", Toast.LENGTH_LONG).show();
                    }
                    try {
                        //meuSocket =(BluetoothSocket) meuDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(meuDevice, 1);
                        BluetoothAdapter.getDefaultAdapter().isEnabled();
                        meuSocket.connect();

                        searchDevice.setText("Desconectar");
                        conexao = true;

                        connectedThread = new ConnectedThread(meuSocket);
                        connectedThread.start();

                        Toast.makeText(getApplicationContext(), "Conexão realizada com sucesso!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Erro no connect!", Toast.LENGTH_LONG).show();
                        try {
                            meuSocket =(BluetoothSocket) meuDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(meuDevice, 1);
                            BluetoothAdapter.getDefaultAdapter().isEnabled();
                            meuSocket.connect();

                            searchDevice.setText("Desconectar");
                            conexao = true;
                            connectedThread = new ConnectedThread(meuSocket);
                            connectedThread.start();
                            Toast.makeText(getApplicationContext(), "Conexão realizada com sucesso!", Toast.LENGTH_LONG).show();
                        } catch (Exception e1) {
                            Toast.makeText(getApplicationContext(), "Erro no final!", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Não foi possível realizar a conexão!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }



    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    String dadosBt = new String(buffer, 0, bytes);



                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, dadosBt).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                meuSocket.close();
            } catch (IOException e) { }
        }
    }
}
