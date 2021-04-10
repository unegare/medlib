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

public class DoctorAdapter extends
    RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView infoTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.doctor_name);
            infoTextView = (TextView) itemView.findViewById(R.id.doctor_lastInfo);
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

        TextView tvName = holder.nameTextView;
        tvName.setText(doc.getProfileName());

        TextView tvInfo = holder.infoTextView;
        tvInfo.setText("none");
    }

    @Override
    public int getItemCount() {
        Log.i("DOCAD::getItemCount", Integer.toString(mDoctors.size()));
        return mDoctors.size();
    }
}
