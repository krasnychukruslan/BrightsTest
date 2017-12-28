package com.creatilas.brightstest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by rusci on 29-Dec-17.
 */

public class DateStepAdapter extends RecyclerView.Adapter<DateStepAdapter.ViewHolder> {

    private List<ModelDateStep> list;

    DateStepAdapter(List<ModelDateStep> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_step, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (list.get(position).getCurrentDate().equals(getDay())) {
            holder.step.setText("Your last days progress");
            holder.meter.setVisibility(View.GONE);
            holder.date.setVisibility(View.GONE);
        } else {
            holder.date.setText(list.get(position).getCurrentDate());
            holder.step.setText(list.get(position).getSteps());
            holder.meter.setText(String.valueOf(Integer.parseInt(list.get(position).getSteps()) / 2));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String getDay() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView step;
        TextView meter;

        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.itemDate);
            step = itemView.findViewById(R.id.itemStep);
            meter = itemView.findViewById(R.id.itemMeter);
        }
    }
}
