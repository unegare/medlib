package com.example.medlib;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.IntConsumer;

public class DoctorAdapter extends
    RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView infoTextView;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.doctor_name);
            infoTextView = (TextView) itemView.findViewById(R.id.doctor_lastInfo);
            view = itemView;
        }
    }

    private List<DoctorItem> mDoctors;

    public DoctorAdapter(List<DoctorItem> doctors) {
        mDoctors = doctors;
    }

    @Override
    public DoctorAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);

        View doctorView = inflater.inflate(R.layout.doctor_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(doctorView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DoctorAdapter.@NotNull ViewHolder holder, int position) {
        Log.i("DOCAD::onBindViewHolder", "position=" + Integer.toString(position));
        DoctorItem doc = mDoctors.get(position);

        holder.nameTextView.setText(doc.getProfileName());

        holder.infoTextView.setText("none");

        int profile_id = doc.getProfileID();
        IntConsumer ic = doc.getOnClickHandler();
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("DoctorItem", "onClick");
                ic.accept(profile_id);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.i("DOCAD::getItemCount", Integer.toString(mDoctors.size()));
        return mDoctors.size();
    }

    public void setDoctors(List<DoctorItem> docs) {
        mDoctors = docs;
    }
}
