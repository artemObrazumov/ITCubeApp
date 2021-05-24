package com.artem_obrazumov.it_cubeapp.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Adapters.AddedImagesAdapter;
import com.artem_obrazumov.it_cubeapp.Models.ContestPostModel;
import com.artem_obrazumov.it_cubeapp.Models.PostModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.Tasks;
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

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class NewPostActivity extends AppCompatActivity {

    // Константы
    private static final int GET_NEW_IMAGE = 1;   // Получение фото из галлереи

    public static final int CREATE_NEW_POST = 0;  // Создание нового поста
    public static final int EDIT_POST = 1;        // Редактирование поста

    private static final int GET_PERMISSION_TO_WRITE = 2; // Получение разрешения на запись в хранилище

    // Режим использования активности
    private int mode;
    private String existingPostID;
    private int postType;
    private int previousPostPublishType = PostModel.STATE_DRAFT;

    // View - элементы
    private TextView open_until;
    private EditText input_title, input_description, input_link;
    private RecyclerView added_images;
    private Button add_images_button;
    private CheckBox publish_checkbox;

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

    // Окно для уведомления пользователя загрузке
    private ProgressDialog progressDialog;

    // Объекты для получения даты окончания мероприятий
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Date openUntilTime;

    // данные пользователя
    private UserModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        setupMode();

        // Получение верхней панели
        ActionBar action_bar = getSupportActionBar();
        // Изменение названия верхней панели
        if (mode == CREATE_NEW_POST) {
            action_bar.setTitle(R.string.add_post);
        } else if (mode == EDIT_POST) {
            action_bar.setTitle(R.string.edit_post);
        }
        // Включение кнопки "назад" на верхней панели
        action_bar.setDisplayHomeAsUpEnabled(true);

        // Инициализация элементов
        open_until = findViewById(R.id.open_until);
        input_title = findViewById(R.id.input_title);
        input_description = findViewById(R.id.input_description);
        input_description.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input_link = findViewById(R.id.input_link);
        publish_checkbox = findViewById(R.id.publish_checkbox);
        added_images = findViewById(R.id.added_images);
        add_images_button = findViewById(R.id.add_images_button);

        progressDialog = new ProgressDialog(this);

        // Слушатель для кнопки
        add_images_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
            }
        });

        // Получение даты окончания мероприятий
        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 0);
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                openUntilTime = calendar.getTime();
                new TimePickerDialog(NewPostActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                openUntilTime = calendar.getTime();
                                displayDateAndTime();
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), true)
                        .show();
            }
        };
        open_until.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запускаем оукно для выбора даты рождения
                new DatePickerDialog(
                        NewPostActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        // Инициализация базы данных
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        loadUserData();

        // Инициализация RecyclerView
        initializeRecyclerView();
    }

    // Метод для загрузки данных пользователя
    private void loadUserData() {
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();

        UserModel.getUserQuery(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            user = ds.getValue(UserModel.class);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();
                    }
                }
        );
    }

    // Выбор режима использования активности
    private void setupMode() {
        Intent intent = getIntent();
        postType = intent.getIntExtra("postType", 0);
        if (postType == PostModel.POST_TYPE_NEWS) {
            findViewById(R.id.input_link_layout).setVisibility(View.GONE);
            findViewById(R.id.open_until).setVisibility(View.GONE);
        }
        mode = intent.getIntExtra("mode", CREATE_NEW_POST);
        if (mode == EDIT_POST) {
            existingPostID = intent.getStringExtra("existingPostID");
            /*
                Запрашиваем доступ на запись файлов (для редактирования изображений)
                Если доступ будет разрешен, то заполняем все поля в методе onRequestPermissionsResult()
            */
            ActivityCompat.requestPermissions(NewPostActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},GET_PERMISSION_TO_WRITE);
        }
    }

    // Заполнение полей данными существующего поста
    private void setupFields() {
        if (postType == PostModel.POST_TYPE_NEWS) {
            PostModel.getNewsPostQuery(existingPostID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                PostModel post = ds.getValue(PostModel.class);
                                input_title.setText(post.getTitle());
                                input_description.setText(post.getDescription());
                                previousPostPublishType = post.getPublishType();
                                if (post.getPublishType() == PostModel.STATE_PUBLISHED) {
                                    publish_checkbox.setChecked(true);
                                }
                                ArrayList<String> imagesURLs = post.getImagesURLs();

                                loadEditableImages(imagesURLs);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            finish();
                        }
                    }
            );
        } else if (postType == PostModel.POST_TYPE_HACKATHON) {
            ContestPostModel.getHackathonPostQuery(existingPostID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                PostModel post = ds.getValue(PostModel.class);
                                input_title.setText(post.getTitle());
                                input_description.setText(post.getDescription());
                                ArrayList<String> imagesURLs = post.getImagesURLs();

                                loadEditableImages(imagesURLs);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            finish();
                        }
                    }
            );
        } else {
            ContestPostModel.getContestPostQuery(existingPostID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                PostModel post = ds.getValue(PostModel.class);
                                input_title.setText(post.getTitle());
                                input_description.setText(post.getDescription());
                                ArrayList<String> imagesURLs = post.getImagesURLs();

                                loadEditableImages(imagesURLs);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            finish();
                        }
                    }
            );
        }
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

    // Метод для отображения выбранного времени окончания мероприятия
    private void displayDateAndTime() {
        String dateOfBirthString = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).format(openUntilTime);
        open_until.setText(
                String.format( "Запись открыта до: %s г.", dateOfBirthString )
        );
    }

    // Метод для инициализации адаптера
    private void initializeAdapter() {
        ArrayList<Uri> array = new ArrayList<>();
        addedImagesAdapter = new AddedImagesAdapter(array);
        initializeAdapterOnClickListener();
    }

    // Метод для создания слушателя для адаптера
    private void initializeAdapterOnClickListener() {
        AddedImagesAdapter.OnAddedImageClickListener onAddedImageClickListener =
                new AddedImagesAdapter.OnAddedImageClickListener() {
                    @Override
                    public void onCutBtnClick(View v, int pos) {
                        // Кнопка обрезания изображения
                        currentCuttingPos = pos;
                        Uri imageUri = addedImagesAdapter.getDataSet().get(pos);
                        CropImage.activity(imageUri)
                                .start(NewPostActivity.this);
                    }

                    @Override
                    public void onImageClick(View v, String url) {
                        // Кнопка просмотра изображения
                        // Переходим на другую активность и передаем ссылку на фото
                        Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
                        intent.putExtra("imageURL", url);
                        startActivity(intent);
                    }
                };
        // Устанавливаем созданный слушатель
        addedImagesAdapter.setOnClickListener(onAddedImageClickListener);
    }

    // Метод для инициализации RecyclerView
    private void initializeRecyclerView() {
        initializeAdapter();
        added_images.setLayoutManager(new LinearLayoutManager(this));
        added_images.setNestedScrollingEnabled(false);
        added_images.setAdapter(addedImagesAdapter);
    }

    // Метод для публикации поста
    private void submitPost() {
        // Получение полей
        String title = input_title.getText().toString().trim();
        String description = input_description.getText().toString().trim();

        if (validData(title, description)) {
            // Получаем остальные данные поста
            long time = Calendar.getInstance().getTimeInMillis();
            String link = "";
            long openUntilTime = Calendar.getInstance().getTimeInMillis()+5000;
            if (postType == PostModel.POST_TYPE_CONTEST ||
                postType == PostModel.POST_TYPE_HACKATHON) {
                link = input_link.getText().toString().trim();
            }
            // Получаем ключ поста
            String uid;
            DatabaseReference reference, keyReference;
            if (postType == PostModel.POST_TYPE_NEWS) {
                reference = database.getReference("News_posts");
            } else if (postType == PostModel.POST_TYPE_CONTEST) {
                reference = database.getReference("Contest_posts");
            } else {
                reference = database.getReference("Hackathon_posts");
            }

            if (mode == CREATE_NEW_POST) {
                keyReference = reference.push();
                uid = keyReference.getKey();
            } else {
                keyReference = reference.child(existingPostID);
                uid = existingPostID;
            }
            // Если загружено более 9 фотографий, то стираем их
            while (addedImagesAdapter.getDataSet().size() > 9) {
                addedImagesAdapter.getDataSet().remove(9);
            }
            // Загружаем фотографии
            UploadAddedImages(addedImagesAdapter.getDataSet(), uid);

            Object post;
            // Создаем объект поста
            if (postType == PostModel.POST_TYPE_NEWS) {
                post = new PostModel(time, uid, auth.getCurrentUser().getUid(), user.getCubeId(), title, description,
                        PostModel.STATE_PUBLISHED, new ArrayList<>());
            } else {
                post = new ContestPostModel(time, uid, auth.getCurrentUser().getUid(), user.getCubeId(), title, description,
                        PostModel.STATE_PUBLISHED, new ArrayList<>(), link, openUntilTime);
            }
            // Добавляем пост в БД
            keyReference.setValue(post);

            // Отправка уведомления о посте
            if (previousPostPublishType != PostModel.STATE_PUBLISHED) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json = createPostNotificationJSON(post, postType);
                        new Tasks.NotificationSender(getApplicationContext(), json).doInBackground();
                    }
                }).start();
            }
        }
    }

    // Сохранение поста без валидации полей (черновик поста)
    private void submitWithoutValidation() {
        // Получение полей
        String title = input_title.getText().toString().trim();
        String description = input_description.getText().toString().trim();
        long time = Calendar.getInstance().getTimeInMillis();
        String link = "";
        long openUntilTime = Calendar.getInstance().getTimeInMillis()+5000;
        if (postType == PostModel.POST_TYPE_CONTEST ||
                postType == PostModel.POST_TYPE_HACKATHON) {
            link = input_link.getText().toString().trim();
        }
        // Получаем ключ поста
        String uid;
        DatabaseReference reference, keyReference;
        if (postType == PostModel.POST_TYPE_NEWS) {
            reference = database.getReference("News_posts");
        } else if (postType == PostModel.POST_TYPE_CONTEST) {
            reference = database.getReference("Contest_posts");
        } else {
            reference = database.getReference("Hackathon_posts");
        }

        if (mode == CREATE_NEW_POST) {
            keyReference = reference.push();
            uid = keyReference.getKey();
        } else {
            keyReference = reference.child(existingPostID);
            uid = existingPostID;
        }
        // Если загружено более 9 фотографий, то стираем их
        while (addedImagesAdapter.getDataSet().size() > 9) {
            addedImagesAdapter.getDataSet().remove(9);
        }
        // Загружаем фотографии
        UploadAddedImages(addedImagesAdapter.getDataSet(), uid);

        Object post;
        // Создаем объект поста
        if (postType == PostModel.POST_TYPE_NEWS) {
            post = new PostModel(time, uid, auth.getCurrentUser().getUid(), user.getCubeId(), title, description,
                    PostModel.STATE_DRAFT, new ArrayList<>());
        } else {
            post = new ContestPostModel(time, uid, auth.getCurrentUser().getUid(), user.getCubeId(), title, description,
                    PostModel.STATE_DRAFT, new ArrayList<>(), link, openUntilTime);
        }
        // Добавляем пост в БД
        keyReference.setValue(post);
    }

    // Метод для валидации полученной информации с полей
    private boolean validData(String title, String description) {
        if (TextUtils.isEmpty(title)) {
            // Не введено название поста
            Toast.makeText(this, getString(R.string.no_post_title), Toast.LENGTH_LONG).show();
            return false;
        } else if (description.length() < 100) {
            // Слишком короткий пост
            Toast.makeText(this, getString(R.string.no_post_title), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
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
                String location = String.format("post_images/%s/%s.jpeg", id, imageID);
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
                                setPostImagesLinks(uploadedImagesURLs, id);
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

    // Метод для прикрепления изображений к посту
    private void setPostImagesLinks(ArrayList<String> uploadedImagesURLs, String id) {
        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("imagesURLs", uploadedImagesURLs);

        if (postType == PostModel.POST_TYPE_NEWS) {
            database.getReference("News_posts").child(id)
                    .updateChildren(updatedData);
        } else if (postType == PostModel.POST_TYPE_CONTEST) {
            database.getReference("Contest_posts").child(id)
                    .updateChildren(updatedData);
        } else {
            database.getReference("Hackathon_posts").child(id)
                    .updateChildren(updatedData);
        }
    }

    // Установка нового изображения после его обрезания
    private void updateAdapterImage(Uri croppedImageURI) {
        addedImagesAdapter.getDataSet().set(currentCuttingPos, croppedImageURI);
        addedImagesAdapter.notifyDataSetChanged();
    }

    // Метод для создания уведомления о посте
    private JSONObject createPostNotificationJSON(Object postObject, int postType) {
        try {
            JSONObject notificationJSON = new JSONObject();
            PostModel post = (PostModel) postObject;
            JSONObject notificationDataJSON = new JSONObject();

            notificationDataJSON.put("messageType", postType);
            notificationDataJSON.put("title", post.getTitle());
            notificationDataJSON.put("postAuthorID", post.getAuthorUid());
            notificationDataJSON.put("PostID", post.getUid());

            notificationJSON.put("data", notificationDataJSON);
            notificationJSON.put("to", "/topics/global_topic");

            return notificationJSON;
        } catch (Exception e) {
            return new JSONObject();
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_TO_WRITE) {
            // Вне зависимости от результата запускаем метод заполнения полей
            setupFields();
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
                // Кнопка "опубликовасть"
                if (publish_checkbox.isChecked()) {
                    // Публикация поста
                    submitPost();
                } else {
                    // Сохранение поста в черновик
                    submitWithoutValidation();
                }
                break;
            default:
                // Кнопка "назад"
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
