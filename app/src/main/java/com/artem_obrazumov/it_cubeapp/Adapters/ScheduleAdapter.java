package com.artem_obrazumov.it_cubeapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.ProgramModel;
import com.artem_obrazumov.it_cubeapp.Models.ScheduleModel;
import com.artem_obrazumov.it_cubeapp.R;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    // Информация в адаптере
    private ArrayList<ScheduleModel> dataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox monday, tuesday, wednesday, thursday, friday, saturday;
        private final Spinner time_select;

        public ViewHolder(View view) {
            super(view);
            monday = view.findViewById(R.id.monday);
            tuesday = view.findViewById(R.id.tuesday);
            wednesday = view.findViewById(R.id.wednesday);
            thursday = view.findViewById(R.id.thursday);
            friday = view.findViewById(R.id.friday);
            saturday = view.findViewById(R.id.saturday);

            time_select = view.findViewById(R.id.time_select);
        }

        // Геттеры для дней недели
        public CheckBox getMonday() {
            return monday;
        }

        public CheckBox getTuesday() {
            return tuesday;
        }

        public CheckBox getWednesday() {
            return wednesday;
        }

        public CheckBox getThursday() {
            return thursday;
        }

        public CheckBox getFriday() {
            return friday;
        }

        public CheckBox getSaturday() {
            return saturday;
        }

        // Геттер для спиннера со временем
        public Spinner getTimeSelect() {
            return time_select;
        }
    }

    public ScheduleAdapter(ArrayList<ScheduleModel> dataSet) {
        this.dataSet = dataSet;
    }

    public ScheduleAdapter() {
        this.dataSet = new ArrayList<>();
    }

    // Добавление расписания
    public void addItem() {
        dataSet.add(new ScheduleModel());
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.schedule_row, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
