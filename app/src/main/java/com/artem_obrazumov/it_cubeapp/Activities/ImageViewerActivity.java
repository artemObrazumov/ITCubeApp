package com.artem_obrazumov.it_cubeapp.Activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;

// Активность для просмотра изображений
public class ImageViewerActivity extends AppCompatActivity {

    // View-элементы
    private ImageView viewing_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // Инициализация элементов
        viewing_image = findViewById(R.id.viewing_image);
        
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
                    .into(viewing_image);

        } catch (Exception e) {
            // Ошибка при загрузке изображения
            Toast.makeText(this, getString(R.string.image_load_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}