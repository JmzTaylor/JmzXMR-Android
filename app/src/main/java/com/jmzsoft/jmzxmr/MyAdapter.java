package com.jmzsoft.jmzxmr;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Rig> rigList;

    private static class VIEW_TYPES {
        public static final int Header = 0;
        public static final int Normal = 1;
        public static final int Footer = 2;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, difficulty, hashrate, version, uptime, totalHash, totalHashes, validShares, totalPaid, amountDue;
        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            difficulty = view.findViewById(R.id.difficulty);
            hashrate = view.findViewById(R.id.hashrate);
            version = view.findViewById(R.id.version);
            uptime = view.findViewById(R.id.uptime);
            totalHash = view.findViewById(R.id.totalHash);
            totalHashes = view.findViewById(R.id.totalHashes);
            validShares = view.findViewById(R.id.validShares);
            totalPaid = view.findViewById(R.id.totalPaid);
            amountDue = view.findViewById(R.id.amountDue);
        }
    }


    public MyAdapter(ArrayList<Rig> rigList) {
        this.rigList = rigList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowView;
        switch (viewType) {
            case VIEW_TYPES.Header:
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_header, parent, false);
                break;
            case VIEW_TYPES.Footer:
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pool_row, parent, false);
                break;
            case VIEW_TYPES.Normal:
            default:
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rig_row, parent, false);
                break;
        }
        return new MyViewHolder (rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Rig rig = rigList.get(position);
        switch (viewType) {
            case VIEW_TYPES.Header:
                holder.totalHash.setText(rig.getCurrentHash());
                break;
            case VIEW_TYPES.Footer:
                Pool pool = rig.getPool();
                holder.totalHashes.setText(pool.getTotalHashes());
                holder.validShares.setText(pool.getValidShares());
                holder.totalPaid.setText(pool.getAmtPaid());
                holder.amountDue.setText(pool.getAmtDue());
                break;
            case VIEW_TYPES.Normal:
            default:
                holder.title.setText(rig.getRigName());
                holder.difficulty.setText(rig.getDiffCurrent());
                holder.hashrate.setText(rig.getCurrentHash());
                holder.version.setText(rig.getMinerVersion());
                holder.uptime.setText(rig.getUptime());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return rigList.size();
    }

    @Override
    public int getItemViewType(int position) {

        switch (rigList.get(position).getPosition()) {
            case 0:
                return VIEW_TYPES.Header;
            case 2:
                return VIEW_TYPES.Footer;
            case 1:
            default:
                return VIEW_TYPES.Normal;
        }
    }
}