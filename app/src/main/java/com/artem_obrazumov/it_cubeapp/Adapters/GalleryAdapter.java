package com.artem_obrazumov.it_cubeapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    // Константы
    public static final int MODE_FULL = 1;        // Полные изображения
    public static final int MODE_CROPPED = 2;     // Обрезанные изображения
    public static final int MODE_HORIZONTAL = 3;  // Изображения в ряд

    // Информация в адаптере
    private ArrayList<String> dataSet = new ArrayList<>();
    private int ViewType = MODE_FULL;

    // Интерфейсы для обработки нажатий
    private OnImageClickListener onImageClickListener;

    // Конструкторы для адаптера
    public GalleryAdapter(ArrayList<String> dataSet) {
        this.dataSet = dataSet;
    }

    public GalleryAdapter(int ViewMode) {
        this.ViewType = ViewMode;
    }

    public GalleryAdapter(ArrayList<String> dataSet, int ViewMode) {
        this.dataSet = dataSet;
        this.ViewType = ViewMode;
    }

    public GalleryAdapter() {}

    // Метод для обновления списка новостей
    public void setDataSet(ArrayList<String> newDataSet) {
        this.dataSet = newDataSet;
        notifyDataSetChanged();
    }

    // Метод для обновления обработчика нажатий
    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    // Класс ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // View-элементы
        private final ImageView image;

        String url;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            image = view.findViewById(R.id.image);
        }

        // Геттеры
        public ImageView getImageView() {
            return image;
        }

        // Реализация интерфейса
        @Override
        public void onClick(View v) {
            onImageClickListener.onClick(v, url);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.ViewType;
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MODE_FULL) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.image_row, parent, false);
        } else if (ViewType == MODE_CROPPED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cropped_image_row, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.image_horizontal_row, parent, false);
        }

        return new GalleryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ViewHolder holder, int position) {
        String imageURL = dataSet.get(position);
        holder.url = imageURL;

        // Загрузка изображения
        try {
            if (getItemViewType(position) == MODE_CROPPED) {
                Glide.with(holder.getImageView())
                        .load(imageURL)
                        .apply(new RequestOptions().override(900, 900))
                        .centerCrop()
                        .placeholder(R.color.placeholder_color)
                        .into(holder.getImageView());
            } else {
                Glide.with(holder.getImageView()).load(imageURL).placeholder(R.color.placeholder_color).into(holder.getImageView());
            }
        } catch (Exception e) {
            // Если не удалось загрузить изображение с интернета, ничего не делаем
        }
    }

    @Override
    public int getItemCount() {
        try {
            return dataSet.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public interface OnImageClickListener {
        void onClick(View v, String url);
    }
}
