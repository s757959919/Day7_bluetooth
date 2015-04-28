package com.shang.day7_bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

/**
 * Created by shang on 2015/4/22.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
  private List<BluetoothDevice> beans;
    private Context context;
    private View.OnClickListener listener;

    public DeviceAdapter( List<BluetoothDevice> beans, Context context,View.OnClickListener listener  ) {
        this.beans=beans;
        this.context=context;
        this.listener=listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View item=  LayoutInflater.from(context) .inflate(R.layout.item,null);
        item.setOnClickListener(listener);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {



        BluetoothDevice device =beans.get(i);

        if(device.getBondState()==BluetoothDevice.BOND_BONDED)
        {
            viewHolder.name.setTextColor(Color.RED);
        }
        else //if(device.getBondState()==BluetoothDevice.BOND_NONE)
        {
            viewHolder.name.setTextColor(Color.BLACK);
        }
        viewHolder.name.setText(device.getName()+":");
        viewHolder.address.setText(device.getAddress());



    }
     public BluetoothDevice getItem(int position)
     {
         return beans.get(position);
     }

    @Override
    public int getItemCount() {
        return beans.size();
    }
   public void addAll(Collection<? extends BluetoothDevice>list)
   {
       {
           int size = beans.size();
           beans.addAll(list);
           notifyItemRangeInserted(size, list.size());
       }
   }

    public void add(BluetoothDevice device)
    {
        if(!beans.contains(device))
        {
            beans.add(0,device);
            notifyItemInserted(0);
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
  {
      private TextView name;
      private TextView  address;
      public ViewHolder(View itemView) {
          super(itemView);
    name = (TextView) itemView.findViewById(R.id.name);
    address= (TextView) itemView.findViewById(R.id.address);




      }
  }





}
