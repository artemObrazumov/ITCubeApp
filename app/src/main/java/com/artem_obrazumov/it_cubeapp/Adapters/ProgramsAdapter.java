package com.artem_obrazumov.it_cubeapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.ProgramModel;
import com.artem_obrazumov.it_cubeapp.R;

import java.util.ArrayList;

public class ProgramsAdapter extends RecyclerView.Adapter<ProgramsAdapter.ViewHolder> {

    // Информация в адаптере
    private ArrayList<ProgramModel> dataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final EditText input_title, input_description;

        public ViewHolder(View view) {
            super(view);
            input_title = view.findViewById(R.id.input_title);
            input_description = view.findViewById(R.id.input_description);
        }

        public EditText getInputTitle() {
            return input_title;
        }

        public EditText getInputDescription() {
            return input_description;
        }
    }

    public ProgramsAdapter(ArrayList<ProgramModel> dataSet) {
        this.dataSet = dataSet;
    }

    public ProgramsAdapter() {}

    // Добавление программы обучения
    public void addItem() {
        dataSet.add(new ProgramModel());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.program_row, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

    }

    @Override
    public int getItemCount() {
        try {
            return dataSet.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
