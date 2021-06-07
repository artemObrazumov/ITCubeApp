package com.artem_obrazumov.it_cubeapp.ui.Activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityImageViewerBinding;
import com.bumptech.glide.Glide;

// Активность для просмотра изображений
public class ImageViewerActivity extends AppCompatActivity {

    // Binding
    private ActivityImageViewerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        //Установка изображения
        SetImageFromIntent();
    }

    // Метод для установки изображения из интента
    private void SetImageFromIntent() {
        try {
            // Загружаем изображение
            String url = getIntent().getExtras().getString("imageURL");
            Glide.with(ImageViewerActivity.this)
                    .load(url)
                    .into(binding.viewingImage);

        } catch (Exception e) {
            // Ошибка при загрузке изображения
            Toast.makeText(this, getString(R.string.image_load_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}