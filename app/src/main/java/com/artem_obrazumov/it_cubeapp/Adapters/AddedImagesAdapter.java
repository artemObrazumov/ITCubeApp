package com.artem_obrazumov.it_cubeapp.Adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AddedImagesAdapter extends RecyclerView.Adapter<AddedImagesAdapter.ViewHolder> {

    // Информация в адаптере
    private ArrayList<Uri> dataSet = new ArrayList<>();

    // Интерфейс для обработки нажатий
    private OnAddedImageClickListener onAddedImageClickListener;

    // Конструкторы для адаптера
    public AddedImagesAdapter(ArrayList<Uri> dataSet) {
        this.dataSet = dataSet;
    }

    public AddedImagesAdapter() {}

    // Метод для обновления списка ссылок с изображениями
    public void setDataSet(ArrayList<Uri> newDataSet) {
        this.dataSet = newDataSet;
        notifyDataSetChanged();
    }

    // Метод для установки слушателей нажатий
    public void setOnClickListener(OnAddedImageClickListener onAddedImageClickListener) {
        this.onAddedImageClickListener = onAddedImageClickListener;
    }

    // Метод для вставки нового элемента
    public void addElement(Uri url) {
        this.dataSet.add(url);
        notifyItemInserted(dataSet.size()-1);
    }

    // Класс ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        // View-элементы
        private final ImageView image;
        private final ImageView cut_button, delete_button;

        Uri temporaryUri;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.preview_image);
            cut_button = view.findViewById(R.id.cut_button);
            delete_button = view.findViewById(R.id.delete_button);

            // Обработчики нажатий на кнопки
            // Кнопка обрезания фотки
            cut_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = dataSet.indexOf(temporaryUri);
                    onAddedImageClickListener.onCutBtnClick(v, pos);
                }
            });

            // Кнопка удаления фотки
            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = dataSet.indexOf(temporaryUri);
                    removeItem(pos);
                }
            });

            // Обработка нажатия на изображение
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String imageUrl = temporaryUri.toString();
                    onAddedImageClickListener.onImageClick(v, imageUrl);
                }
            });
        }

        // Геттеры
        public ImageView getImageView() {
            return image;
        }

        // Сеттер для ссылки на изображение
        public void setUrl(Uri newUri) {
            this.temporaryUri = newUri;
            getImageView().setImageURI(newUri);
        }
    }

    private void removeItem(int position) {
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.added_images_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Устанавливаем ссылку на изображение
        Uri imageURL = dataSet.get(position);
        holder.setUrl(imageURL);

        // Загрузка изображения
        try {
            Glide.with(holder.getImageView()).load(imageURL).placeholder(R.drawable.default_user_profile_icon).into(holder.getImageView());
        } catch (Exception e) {
            // Если не удалось загрузить изображение с интернета, ничего не делаем
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    // Получение списка
    public ArrayList<Uri> getDataSet() {
        return this.dataSet;
    }

    public interface OnAddedImageClickListener {
        void onCutBtnClick(View v, int pos);
        void onImageClick(View v, String url);
    }
}

