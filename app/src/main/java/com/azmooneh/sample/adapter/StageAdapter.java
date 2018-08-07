package com.azmooneh.sample.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azmooneh.sample.R;
import com.azmooneh.sample.struct.StructStep;

import java.util.ArrayList;

/**
 * Created by iSheykhi on 09/11/2017.
 */

public class StageAdapter extends RecyclerView.Adapter<StageAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(StructStep item);
    }

    private Context                context;
    private ArrayList<StructStep> values;
    private OnItemClickListener    listener;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }

    public void add(int position, StructStep item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    public StageAdapter(ArrayList<StructStep> myDataSet, OnItemClickListener listener) {
        values = myDataSet;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        View v = inflater.inflate(R.layout.item_list_stage, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final StructStep StructStep = values.get(position);
        holder.tvTitle.setText(StructStep.title);
        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(StructStep);
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

}