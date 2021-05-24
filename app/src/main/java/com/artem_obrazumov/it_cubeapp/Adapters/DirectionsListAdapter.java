package com.artem_obrazumov.it_cubeapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.R;

import java.util.ArrayList;

public class DirectionsListAdapter extends RecyclerView.Adapter<DirectionsListAdapter.ViewHolder> {

    // Константы
    public static final int MODE_ACTIVE = 0;
    public static final int MODE_AVAILABLE = 1;

    // Информация в адаптере
    private ArrayList<DirectionModel> dataSet;
    private int mode = MODE_ACTIVE;

    // Слушатель для обработки нажатий
    private OnDirectionClickListener onDirectionClickListener;

    // Конструкторы
    public DirectionsListAdapter() {}

    public DirectionsListAdapter(ArrayList<DirectionModel> dataSet) {
        this.dataSet = dataSet;
    }

    public DirectionsListAdapter(ArrayList<DirectionModel> dataSet, int mode) {
        this.dataSet = dataSet;
        this.mode = mode;
    }

    // Обновление информации в адаптере
    public void setDataSet(ArrayList<DirectionModel> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    // Обновление слушателя нажатий
    public void setOnDirectionClickListener(OnDirectionClickListener onDirectionClickListener) {
        this.onDirectionClickListener = onDirectionClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, description;
        private final Button subscribe_button;

        public ViewHolder(@NonNull View view) {
            super(view);

            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            subscribe_button = view.findViewById(R.id.subscribe_button);
            try {
                subscribe_button.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            onDirectionClickListener.onDirectionSubscribeButtonClicked(
                                                                    dataSet.get(getAdapterPosition()).getId()
                                                            );
                                                        }
                                                    }
                );
            } catch (Exception ignored) {}
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getDescription() {
            return description;
        }

        public Button getSubscribe_button() {
            return subscribe_button;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        // Выбор разметки в зависимости от типа адаптера
        if (mode == MODE_ACTIVE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.active_course_row, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.available_course_row, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Установка значений направления
        DirectionModel direction = dataSet.get(position);
        holder.getTitle().setText(direction.getTitle());
        holder.getDescription().setText(direction.getDescription());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public interface OnDirectionClickListener {
        void onDirectionSubscribeButtonClicked(String directionID);
    }
}
