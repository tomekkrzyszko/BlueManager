package pl.tomekkrzyszko.bluemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceHolder>{

    private List<BlueDevice> devices;

    private Context mContext;
    private OnDeviceClickListener onDeviceClickListener;
    private String connectedDeviceMAC = null;

    DevicesAdapter(Context context, Map<String, BlueDevice> devices, OnDeviceClickListener onDeviceClickListener) {
        this.mContext = context;
        this.onDeviceClickListener = onDeviceClickListener;
        updateDeviceList(devices,null);
    }

    void updateDeviceList(Map<String, BlueDevice> devices, BlueDevice connectedDeviceMAC){
        this.devices = new ArrayList<>(devices.values());
        if(connectedDeviceMAC!=null) {
            this.connectedDeviceMAC = connectedDeviceMAC.getAddress();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View listView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_list_item, viewGroup, false);
        return new DeviceHolder(listView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        holder.id.setText(String.valueOf(position));
        holder.name.setText(devices.get(position).getName());
        holder.address.setText(String.format(mContext.getString(R.string.device_address),devices.get(position).getAddress()));
        holder.itemView.setOnClickListener(view -> onDeviceClickListener.onDeviceClicked(devices.get(position)));
        if(connectedDeviceMAC!=null){
            if(connectedDeviceMAC.equals(devices.get(position).getAddress())){
                holder.deviceContainer.setBackgroundColor(mContext.getResources().getColor(R.color.green));
            }else{
                holder.deviceContainer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != devices ? devices.size() : 0);
    }

    public class DeviceHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.device_id) TextView id;
        @BindView(R.id.device_name) TextView name;
        @BindView(R.id.device_address) TextView address;
        @BindView(R.id.device_container) LinearLayout deviceContainer;

        DeviceHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }
}
