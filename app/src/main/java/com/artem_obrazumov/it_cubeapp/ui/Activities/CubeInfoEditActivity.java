package com.artem_obrazumov.it_cubeapp.ui.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.Adapters.AddedImagesAdapter;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.Tasks;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityCubeInfoEditBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CubeInfoEditActivity extends AppCompatActivity {

    // Константы
    private static final int GET_NEW_IMAGE = 1;   // Получение фото из галлереи
    private static final int GET_PERMISSION_TO_WRITE = 2; // Получение разрешения на запись в хранилище

    // Данные куба
    private String cubeID;
    private ITCubeModel cube;

    // Binding
    private ActivityCubeInfoEditBinding binding;

    // Окно для уведомления о загрузках
    private ProgressDialog progressDialog;

    // Адаптер для добавленных изображений
    private AddedImagesAdapter addedImagesAdapter;
    // Доп. переменная, которая хранит индекс элемента в адаптере, пока мы работаем с изображением
    private int currentCuttingPos = 0;

    // База данных
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    // Списки с загруженными фотографиями
    private ArrayList<Uri> imagesURIs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCubeInfoEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Настройка верхней панели
        ActionBar action_bar = getSupportActionBar();
        action_bar.setTitle(R.string.edit_cube_info);
        action_bar.setDisplayHomeAsUpEnabled(true);

        // Инициализация элементов
        progressDialog = new ProgressDialog(this);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Получение ID Куба
        Intent intent = getIntent();
        cubeID = intent.getStringExtra("cubeID");
        if (cubeID == null) {
            // Не удалось получить ID куба
            Toast.makeText(getApplicationContext(), getString(R.string.cubes_load_error), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadCubeInfo();
        }

        // Слушатель для кнопки
        binding.addImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
            }
        });

        // Инициализация RecyclerView
        initializeRecyclerView();

        // Запрос доступа в хранилище устройства
        ActivityCompat.requestPermissions(CubeInfoEditActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GET_PERMISSION_TO_WRITE);
    }

    // Загрузка данных куба
    private void loadCubeInfo() {
        ITCubeModel.getCubeQuery(cubeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    cube = ds.getValue(ITCubeModel.class);
                    binding.inputAddress.setText(cube.getAddress());
                    binding.inputDescription.setText(cube.getDescription());
                    loadEditableImages(cube.getPhotosURLs());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Инициализация списка изображений
    private void initializeRecyclerView() {
        ArrayList<Uri> array = new ArrayList<>();
        addedImagesAdapter = new AddedImagesAdapter(array);
        addedImagesAdapter.setOnClickListener(new AddedImagesAdapter.OnAddedImageClickListener() {
            @Override
            public void onCutBtnClick(View v, int pos) {
                // Кнопка обрезания изображения
                currentCuttingPos = pos;
                Uri imageUri = addedImagesAdapter.getDataSet().get(pos);
                CropImage.activity(imageUri)
                        .start(CubeInfoEditActivity.this);
            }

            @Override
            public void onImageClick(View v, String url) {
                // Кнопка просмотра изображения
                // Переходим на другую активность и передаем ссылку на фото
                Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
                intent.putExtra("imageURL", url);
                startActivity(intent);
            }
        });

        binding.addedImages.setLayoutManager(new LinearLayoutManager(this));
        binding.addedImages.setNestedScrollingEnabled(false);
        binding.addedImages.setAdapter(addedImagesAdapter);
    }

    // Метод для временного сохранения файлов в память
    private void loadEditableImages(ArrayList<String> imagesURLs) {
        imagesURIs = new ArrayList<>();
        progressDialog.setMessage(getString(R.string.loading_images));

        try {
            if (imagesURLs.size() > 0) {
                progressDialog.show();
            }

            for (int i = 0; i < imagesURLs.size(); i++) {
                String src = imagesURLs.get(i);
                final String[] path = new String[1];
                final Bitmap[] bitmap = new Bitmap[1];
                int imagesLeft = imagesURLs.size() - i;
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                bitmap[0] = new Tasks.ImageBitmapLoader(src).doInBackground();
                                path[0] = insertImageInGallery(bitmap[0]);
                                if (path[0].equals("")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(CubeInfoEditActivity.this, getString(R.string.allow_files_acces), Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                }
                                imagesURIs.add(getUriFromFilePath(path[0]));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addedImagesAdapter.setDataSet(imagesURIs);
                                        if (imagesLeft <= 1) {
                                            progressDialog.hide();
                                        }
                                    }
                                });
                            }
                        }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.hide();
        }
    }

    // Метод для сохранения изображения в галерею
    private String insertImageInGallery(Bitmap image) {
        String imageID = UUID.randomUUID().toString();
        String tempFilesPath = Environment.getExternalStorageDirectory() + "/ITCubeTempFiles";
        File tempFilesDir = new File(tempFilesPath);

        if (!tempFilesDir.exists()) {
            // Если папки временных файлов не существует, то создаем ее
            new File(Environment.getExternalStorageDirectory().getPath() + "/ITCubeTempFiles/")
                    .mkdirs();
        }

        // Получаем путь к файлу
        String path = tempFilesPath + "/" + imageID + ".jpg";
        File imageFile = new File(path);

        // Пытаемся сохранить файл
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Метод для получения объекта Uri для файла
    private Uri getUriFromFilePath(String path) {
        Uri result = Uri.parse(path);
        if (result.getScheme() == null){
            result = Uri.fromFile(new File(path));
        }
        return result;
    }

    // Метод добавления изображений в список
    private void addImage() {
        // Создаем интент для получения изображения из галлереи
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GET_NEW_IMAGE);
    }

    // Метод для сжатия изображения при загрузке
    private byte[] compressedURI(Uri currentURI) {
        byte[] data = new byte[0];
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), currentURI);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            data = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.image_load_error), Toast.LENGTH_SHORT).show();
        }
        return data;
    }

    // Метод для удаления временных файлов, если они есть
    private void deleteTemporaryFiles(ArrayList<Uri> imagesURIs) {
        for (int i = 0; i < imagesURIs.size(); i++) {
            String path = imagesURIs.get(i).getPath();
            File imageFile = new File(path);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    // Метод для прикрепления изображений к кубу
    private void setCubeImagesLinks(ArrayList<String> uploadedImagesURLs, String id) {
        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("photosURLs", uploadedImagesURLs);
        database.getReference("IT_Cubes").child(id)
                .updateChildren(updatedData);
    }

    // Установка нового изображения после его обрезания
    private void updateAdapterImage(Uri croppedImageURI) {
        addedImagesAdapter.getDataSet().set(currentCuttingPos, croppedImageURI);
        addedImagesAdapter.notifyDataSetChanged();
    }

    // Метод для загрузки изображений в посты
    private void UploadAddedImages(ArrayList<Uri> uploadedImagesURIs, String id) {
        ArrayList<String> uploadedImagesURLs = new ArrayList<>();
        progressDialog.setMessage(getString(R.string.loading_images));

        if (uploadedImagesURIs.size() > 0) {
            progressDialog.show();
            for (int i = 0; i < uploadedImagesURIs.size(); i++) {
                String imageID = UUID.randomUUID().toString();
                Uri currentURI = uploadedImagesURIs.get(i);
                String location = String.format("cube_images/%s/%s.jpeg", id, imageID);
                StorageReference reference = storage.getReference(location);

                int index = i;
                reference.putBytes(compressedURI(currentURI)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Для КАЖДОГО изображения сохраняем ссылку в массив и прикрепляем его к посту
                                uploadedImagesURLs.add(uri.toString());
                                setCubeImagesLinks(uploadedImagesURLs, id);
                                if (index >= uploadedImagesURIs.size() - 1) {
                                    if (imagesURIs != null) {
                                        deleteTemporaryFiles(imagesURIs);
                                    }
                                    progressDialog.hide();
                                    onBackPressed();
                                }
                            }
                        });
                    }
                });
            }
        } else {
            onBackPressed();
        }
    }

    // Сохранение информации о кубе
    private void saveCubeInfo() {
        // Получение полей
        String address = binding.inputAddress.getText().toString().trim();
        String description = binding.inputDescription.getText().toString().trim();

        if (validData(address, description)) {
            // Редактируем модель куба
            cube.setAddress(address);
            cube.setDescription(description);
            DatabaseReference reference = database.getReference("IT_Cubes").child(cubeID);

            // Если загружено более 9 фотографий, то стираем их
            while (addedImagesAdapter.getDataSet().size() > 9) {
                addedImagesAdapter.getDataSet().remove(9);
            }
            // Загружаем фотографии
            UploadAddedImages(addedImagesAdapter.getDataSet(), cube.getID());

            // Добавляем пост в БД
            reference.setValue(cube);
        }
    }

    // Валидация данных
    private boolean validData(String address, String description) {
        if (address.length() < 10) {
            // Слишком короткий адрес
            binding.inputAddress.setError(getString(R.string.short_address));
            binding.inputAddress.requestFocus();
            return false;
        }
        if (description.length() < 20) {
            // Слишком короткое описание
            binding.inputDescription.setError(getString(R.string.short_description));
            binding.inputDescription.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_NEW_IMAGE && resultCode == RESULT_OK) {
            // Получение изображения с галлереи
            if (data != null) {
                // Закидываем изображение в список
                addedImagesAdapter.addElement(data.getData());
            } else {
                Toast.makeText(getApplicationContext(), R.string.image_load_error, Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Получение обрезанного изображения
                Uri croppedImageURI = CropImage.getActivityResult(data).getUri();
                updateAdapterImage(croppedImageURI);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Устанавливаем меню для активности
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit:
                // Кнопка "сохранить"
                saveCubeInfo();
                break;
            default:
                // Кнопка "назад"
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}