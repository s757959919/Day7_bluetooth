package com.shang.day7_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static String uuid = "4e3a500b-1ba9-4c3f-a5fe-76cb46608b5f";
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView recycle;
    private Device receiver;
    private DeviceAdapter adapter;
    private Map<BluetoothDevice, BluetoothSocket> socketMap = new HashMap<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    BluetoothDevice device = msg.getData().getParcelable(BluetoothDevice.EXTRA_DEVICE);
                    adapter.add(device);
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recycle = (RecyclerView) findViewById(R.id.recycle);
        recycle.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(new ArrayList<BluetoothDevice>(), this, this);
        recycle.setAdapter(adapter);

        //注册广播：
        Device receiver = new Device(handler);
        //找到蓝牙设备会接收到一个广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //蓝牙设备开启状态
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            discovery();
        }
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "没有蓝牙模块", Toast.LENGTH_LONG).show();
            finish();
        }
        if (!bluetoothAdapter.isEnabled())  //返回值true开启 ， false没有开启
        {
            //开启蓝牙设备：
            //方法一：调用方法开启
            //   bluetoothAdapter.enable();
            //方法二：  通过意图：
            Intent intent = new Intent();
            intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
        } else {
            //获取信息：
            adapter.addAll(bluetoothAdapter.getBondedDevices());

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_LONG).show();
            discovery();
        } else {

            Toast.makeText(this, "开启失败", Toast.LENGTH_LONG).show();

        }
    }

    private BluetoothServerSocket server;

    public void discovery() {
        //开始扫描
        bluetoothAdapter.startDiscovery();

        //socket链接是一个耗时操作。。  需要开启子线程  socket链接是长连接。  HttpClient 时短连接
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    server = bluetoothAdapter.listenUsingRfcommWithServiceRecord("ssl", UUID.fromString(uuid));
                    BluetoothSocket socket;
                    while ((socket = server.accept()) != null) {
                        //获取设备名字
                        BluetoothDevice remoteDevice = socket.getRemoteDevice();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(BluetoothDevice.EXTRA_DEVICE, remoteDevice);
                        Message msg = handler.obtainMessage(0);
                        msg.setData(bundle);
                        msg.sendToTarget();
                        socketMap.put(remoteDevice, socket);
                        new ReadThread(socket, handler).start();
                         /* Log.d("添加成功","-----------------");
                          Log.d("BluetoothSocket",remoteDevice.getName());
                          //获取内容
                          DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                          Log.d("BluetoothSocket",dataInputStream.readUTF());*/
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Set<Map.Entry<BluetoothDevice, BluetoothSocket>> entries = socketMap.entrySet();
        for (Map.Entry<BluetoothDevice, BluetoothSocket> entry : entries) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onClick(View v) {
        int position = recycle.getChildPosition(v);
        final BluetoothDevice item = adapter.getItem(position);
  /*      //蓝牙所包含的服务
    ParcelUuid[] uuids=item.getUuids();
        if(uuids!=null)
        {
            for(ParcelUuid uuid:uuids)
            {
                Log.v("服务： +",uuid.toString());
            }
        }*/
        //耗时操作
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    BluetoothSocket bluetoothSocket = socketMap.get(item);
                    if (bluetoothSocket == null) {
                        bluetoothSocket = item.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
                        //发起一个连接
                        bluetoothSocket.connect();
                        new ReadThread(bluetoothSocket, handler).start();
                        socketMap.put(item, bluetoothSocket);
                    }
                    DataOutputStream dataOutputStream = new DataOutputStream(bluetoothSocket.getOutputStream());
                    dataOutputStream.writeUTF("发送测试");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }
}
