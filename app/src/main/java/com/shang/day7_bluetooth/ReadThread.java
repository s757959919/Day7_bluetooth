package com.shang.day7_bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by shang on 2015/4/22.
 */
public class ReadThread extends Thread {
    private BluetoothSocket socket;
    private Handler handler;


    public ReadThread(BluetoothSocket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        BluetoothDevice device = socket.getRemoteDevice();
        String utf;

        try {
           DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
         while ((utf=dataInputStream.readUTF())!=null)
         {
               Message message=handler.obtainMessage(0);
               message.obj=device.getName()+""+utf;
               message.sendToTarget();
         }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
