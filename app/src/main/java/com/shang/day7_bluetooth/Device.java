package com.shang.day7_bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



/**
 * Created by shang on 2015/4/22.
 */
public class Device extends BroadcastReceiver {
      private Handler handler;

    public Device(Handler handler)
    {
        this.handler=handler;

    }




    @Override


    public void onReceive(Context context, Intent intent) {
        Log.v("开始搜索", intent.toString());
        //获取额外的数据
        BluetoothDevice extra = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.v("名字", extra.getName()+"");
        Log.v("地址", extra.getAddress());
      //  Message msg=Message.obtain();
        //msg.obj=intent.getExtras();
        //handler.sendMessage(msg);
        Message msg=handler.obtainMessage(0);
        msg.setData(intent.getExtras());
        msg.sendToTarget();


    }
}
