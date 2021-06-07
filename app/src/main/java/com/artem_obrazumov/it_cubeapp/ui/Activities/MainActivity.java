package com.artem_obrazumov.it_cubeapp.ui.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.Models.PostModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    // Конфигурация боковой панели
    private AppBarConfiguration mAppBarConfiguration;

    // FireBase
    private FirebaseAuth auth;

    private UserModel userStats;
    private String userDBid;
    private Boolean isUserAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkUserState();

        // Установка верхней панели
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация кнопки добавления постов
        FloatingActionButton fab = findViewById(R.id.fab_add_post);
        fab.hide();

        // Добавление и установка настроек боковой панели
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Первоначальная настройка и конфигурация боковой панели
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_news, R.id.nav_hakatons, R.id.nav_contests)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(MainActivity.this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Обработчик нажатий для кнопки добавления постов
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.select_post_type))
                        .setItems(new String[]{ getString(R.string.news_post), getString(R.string.hackathon_post), getString(R.string.contest_post) },
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        // Новостной пост
                                        intent.putExtra("postType", PostModel.POST_TYPE_NEWS);
                                        break;
                                    case 1:
                                        // Хакатон
                                        intent.putExtra("postType", PostModel.POST_TYPE_HACKATHON);
                                        break;
                                    case 2:
                                        // Конкурс
                                        intent.putExtra("postType", PostModel.POST_TYPE_CONTEST);
                                        break;
                                }
                                startActivity(intent);
                            }
                        })
                        .create().show();
            }
        });

        // Если пользователь нажал на верхнюю часть боковой панели, то отправляем его в профиль
        navigationView.getHeaderView(0).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("profileUid", auth.getCurrentUser().getUid());
                        startActivity(intent);
                    }
                }
        );

        // Инициализация FireBase
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Заполняем верхнюю часть боковой панели данными пользователя
        TextView tv_name = navigationView.getHeaderView(0).findViewById(R.id.nav_name_surname);
        TextView tv_user_status = navigationView.getHeaderView(0).findViewById(R.id.nav_user_status);
        ImageView avatar_view = navigationView.getHeaderView(0).findViewById(R.id.avatar);

        UserModel.getUserQuery(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    userStats = ds.getValue(UserModel.class);
                    userDBid = ds.getKey();

                    // Устанавливаем имя пользователя и его статус
                    tv_name.setText( userStats.getName() + " " + userStats.getSurname() );
                    tv_user_status.setText( UserModel.getUserStatus(userStats.getUserStatus()) );

                    // Загружаем аватарку пользователя по ссылке
                    String avatarURL = userStats.getAvatar();
                    try {
                        Glide.with(MainActivity.this).load(avatarURL).placeholder(R.drawable.default_user_profile_icon).into(avatar_view);
                    } catch (Exception e) {
                        // Если не удалось загрузить изображение с интернета, ничего не делаем
                    }

                    if (userStats.getCubeId().equals("0")) {
                        // Напоминаем пользователю о выборе IT-Куба
                        Intent intent = getIntent();
                        boolean canShowCubeSelect = intent.getBooleanExtra("canShowCubeSelect", true);
                        if (canShowCubeSelect) {
                            Intent cubeIntent = new Intent(MainActivity.this, CubeSelectActivity.class);
                            startActivityForResult(cubeIntent, RegisterActivity.GET_IT_CUBE);
                        }
                    }

                    /* Если статус пользователя - администратор или выше, то меняем конфигурацию
                        боковой панели и активируем кнопку добавления постов */
                    isUserAdmin = (userStats.getUserStatus() >= UserModel.STATUS_ADMIN);
                    if (navigationView.getMenu().size() < 5) {
                        if (isUserAdmin) {
                            fab.show();
                            navigationView.inflateMenu(R.menu.activity_main_drawer_admin);

                            // Настройка конфигурации боковой панели
                            mAppBarConfiguration = new AppBarConfiguration.Builder(
                                    R.id.nav_news, R.id.nav_hakatons, R.id.nav_contests, R.id.nav_drafts, R.id.nav_admin_panel)
                                    .setDrawerLayout(drawer)
                                    .build();

                        } else {
                            navigationView.inflateMenu(R.menu.activity_main_drawer_user);

                            // Настройка конфигурации боковой панели
                            mAppBarConfiguration = new AppBarConfiguration.Builder(
                                    R.id.nav_news, R.id.nav_hakatons, R.id.nav_contests, R.id.nav_group, R.id.nav_courses)
                                    .setDrawerLayout(drawer)
                                    .build();
                        }

                        // Установка боковой панели
                        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
                        NavigationUI.setupActionBarWithNavController(MainActivity.this, navController, mAppBarConfiguration);
                        NavigationUI.setupWithNavController(navigationView, navController);

                        if (isUserAdmin) {
                            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                                @Override
                                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                    String title = item.getTitle().toString();

                                    if (    title.equals(getString(R.string.menu_news)) ||
                                            title.equals(getString(R.string.menu_hackatons)) ||
                                            title.equals(getString(R.string.menu_contests)) ||
                                            title.equals(getString(R.string.menu_drafts)) ) {
                                        fab.show();
                                    } else {
                                        fab.hide();
                                    }

                                    boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                                    if (handled) {
                                        ViewParent parent = navigationView.getParent();
                                        if (parent instanceof DrawerLayout) {
                                            ((DrawerLayout) parent).closeDrawer(navigationView);
                                        }
                                    }

                                    return handled;
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Уведомляем об отсутствии интернета, если проблема есть
        if (!checkIfInternetAvailable()) {

            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.no_internet))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finishAndRemoveTask();;
                        }
                    })
                    .setNegativeButton(getString(R.string.stay), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).create().show();
        }
    }

    // Проверка, есть ли интернет
    private boolean checkIfInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Каждый раз проверяем, находится ли пользователь в системе
        checkUserState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Устанавливаем меню для активности
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Обработчик нажатий на кнопки верхнего выпадающего меня
        switch (item.getItemId()) {
            case R.id.action_logout:

                new AlertDialog.Builder(this)
                        .setMessage( getString(R.string.want_to_leave) )
                        .setCancelable(true)
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();

                                // Стираем данные пользователя из памяти
                                getSharedPreferences("remember_me", Context.MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("remembered_me", false)
                                        .apply();

                                finish();
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RegisterActivity.GET_IT_CUBE && resultCode == RESULT_OK && data != null) {
            // Получаем идентификатор куба
            String selectedCubeID = data.getStringExtra("selectedCubeID");
            if (!selectedCubeID.equals("0")) {
                userStats.setCubeId(selectedCubeID);
                FirebaseDatabase.getInstance().getReference("Users_data").child(userDBid)
                        .setValue(userStats);
                Toast.makeText(this, getString(R.string.cube_updated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkUserState() {
        // Метод для проверки статуса пользователя
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Пользователь не находится в системе, переходим на вступительную активность
            startActivity( new Intent(this, IntroActivity.class) );
        }
    }
}