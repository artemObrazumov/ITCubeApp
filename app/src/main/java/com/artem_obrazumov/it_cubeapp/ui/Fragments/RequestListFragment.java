package com.artem_obrazumov.it_cubeapp.ui.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.databinding.FragmentRequestsListBinding;
import com.artem_obrazumov.it_cubeapp.ui.Activities.ProfileActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.RequestsAdapter;
import com.artem_obrazumov.it_cubeapp.Models.RequestModel;
import com.artem_obrazumov.it_cubeapp.Models.ScheduleModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.Services.MessageService;
import com.artem_obrazumov.it_cubeapp.Tasks;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestListFragment extends Fragment {

    // Binding
    private FragmentRequestsListBinding binding;

    private TabLayout.OnTabSelectedListener onTabSelectedListener;
    private RequestsAdapter adapter;
    private ArrayList<RequestModel> requests;
    private UserModel userStats;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRequestsListBinding.inflate(inflater);
        View root = binding.getRoot();

        // Инициализация элементов
        adapter = new RequestsAdapter();
        initializeAdapter();

        onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadRequestsByState(RequestModel.STATE_UNWATCHED);
                        break;
                    case 1:
                        loadRequestsByState(RequestModel.STATE_ACCEPTED);
                        break;
                    case 2:
                        loadRequestsByState(RequestModel.STATE_DENIED);
                        break;
                    case 3:
                        loadRequestsByState(RequestModel.STATE_RESERVED);
                        break;
                    case 4:
                        loadRequestsByState(RequestModel.STATE_ENROLLED);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        };
        binding.tabs.addOnTabSelectedListener(onTabSelectedListener);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        getUserStats(auth.getCurrentUser().getUid());

        return root;
    }

    // Инициализация адаптера
    private void initializeAdapter() {
        // Создание обработчика нажатий для списка заявок
        adapter.setListener(new RequestsAdapter.RequestOnClickListener() {
            @Override
            public void onUserAvatarClicked(String userID) {
                // Нажатие на аватарку пользователя
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra("profileUid", userID);
                startActivity(intent);
            }

            @Override
            public void onRequestAccepted(RequestModel request) {
                // Заявка одобрена
                HashMap<String, Object> updatedValues = new HashMap<>();
                if (request.getState() == RequestModel.STATE_UNWATCHED ||
                    request.getState() == RequestModel.STATE_RESERVED) {
                    updatedValues.put("state", RequestModel.STATE_ACCEPTED);
                } else {
                    updatedValues.put("state", RequestModel.STATE_ENROLLED);
                    subscribeUserToDirection(request.getUserID(), request.getSchedule(), request.getActivity());
                }

                database.getReference("Request_forms").child(request.getId())
                        .updateChildren(updatedValues);
                Toast.makeText(getContext(), getString(R.string.request_accepted), Toast.LENGTH_SHORT).show();

                if (request.getState() == RequestModel.STATE_UNWATCHED) {
                    sendNotificationRequest(generateRequestNotification(request, RequestModel.STATE_ACCEPTED));
                } else {
                    sendNotificationRequest(generateRequestNotification(request, RequestModel.STATE_ENROLLED));
                }
            }

            @Override
            public void onRequestDenied(RequestModel request, String reason) {
                // Заявка отклонена
                HashMap<String, Object> updatedValues = new HashMap<>();
                updatedValues.put("state", RequestModel.STATE_DENIED);
                updatedValues.put("reason", reason);

                database.getReference("Request_forms").child(request.getId())
                        .updateChildren(updatedValues);
                Toast.makeText(getContext(), getString(R.string.request_denied), Toast.LENGTH_SHORT).show();
                sendNotificationRequest(generateRequestNotification(request, RequestModel.STATE_DENIED));
            }

            @Override
            public void onRequestReserved(RequestModel request, String reason) {
                // Заявка отложена в резерв
                HashMap<String, Object> updatedValues = new HashMap<>();
                updatedValues.put("state", RequestModel.STATE_RESERVED);
                updatedValues.put("reason", reason);

                database.getReference("Request_forms").child(request.getId())
                        .updateChildren(updatedValues);
                Toast.makeText(getContext(), getString(R.string.request_reserved), Toast.LENGTH_SHORT).show();
                sendNotificationRequest(generateRequestNotification(request, RequestModel.STATE_RESERVED));
            }
        });

        // Создание LayoutManager для RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);

        // Присваиваем RecyclerView адаптер с постами и LayoutManager
        binding.requestsList.setLayoutManager(layoutManager);
        binding.requestsList.setAdapter(adapter);
        binding.requestsList.setNestedScrollingEnabled(true);

        // Создаем разделитель между заявками
        DividerItemDecoration divider = new DividerItemDecoration(binding.requestsList.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        binding.requestsList.addItemDecoration(divider);
    }

    // Получение данных пользователя
    private void getUserStats(String id) {
        UserModel.getUserQuery(id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            userStats = ds.getValue(UserModel.class);
                            if (userStats.getUserStatus() >= UserModel.STATUS_ADMIN) {
                                adapter.setCanPerformChanges(true);
                            }
                            getRequests();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Не удалось получить данные пользователя, выходим из фрагмента
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(getView()).navigateUp();
                    }
                }
        );
    }

    // Получение заявок
    private void getRequests() {
        DatabaseReference reference = database.getReference("Request_forms");
        Query requestsQuery = reference.orderByChild("cubeID").equalTo(userStats.getCubeId());
        requests = new ArrayList<>();
        requestsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requests.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    RequestModel request = ds.getValue(RequestModel.class);
                    requests.add(request);
                }
                onTabSelectedListener.onTabSelected(binding.tabs.getTabAt(binding.tabs.getSelectedTabPosition()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.loading_requests_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Фильтрация заявок по их состоянию
    private void loadRequestsByState(int state) {
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<RequestModel> specificRequests = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            RequestModel request = requests.get(i);
            if (request.getState() == state) {
                specificRequests.add(request);
            }
        }
        adapter.setDataSet(specificRequests);
        binding.progressBar.setVisibility(View.GONE);
        if (specificRequests.size() == 0) {
            binding.messageLayout.setVisibility(View.VISIBLE);
        } else {
            binding.messageLayout.setVisibility(View.GONE);
        }
    }

    // Создание JSON-файла для отправки в запрос
    private JSONObject generateRequestNotification(RequestModel request, int newState) {
        try {
            JSONObject notificationJSON = new JSONObject();
            JSONObject notificationDataJSON = new JSONObject();

            String title = "";
            switch (newState) {
                case RequestModel.STATE_ACCEPTED:
                    notificationDataJSON.put("messageType", MessageService.REQUEST_APPROVED);
                    title = String.format(
                            getString(R.string.request_accepted_format), request.getDirectionName()
                    );
                    break;
                case RequestModel.STATE_DENIED:
                    notificationDataJSON.put("messageType", MessageService.REQUEST_DENIED);
                    title = String.format(
                            getString(R.string.request_denied_format), request.getDirectionName()
                    );
                    break;
                case RequestModel.STATE_RESERVED:
                    notificationDataJSON.put("messageType", MessageService.REQUEST_RESERVED);
                    title = String.format(
                            getString(R.string.request_reserved_format), request.getDirectionName()
                    );
                    break;
                case RequestModel.STATE_ENROLLED:
                    notificationDataJSON.put("messageType", MessageService.REQUEST_ENROLLED);
                    title = getString(R.string.request_enrolled_part) + request.getDirectionName();
                    break;
            }

            notificationDataJSON.put("title", title);
            notificationDataJSON.put("toUser", request.getUserID());

            notificationJSON.put("data", notificationDataJSON);
            notificationJSON.put("to", "/topics/global_topic");

            return notificationJSON;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    // Запись ученика на направление
    private void subscribeUserToDirection(String userID, String schedule, String direction) {
        ScheduleModel.getScheduleQuery(schedule).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ArrayList<String> students = ds.getValue(ScheduleModel.class).getStudents();
                            if (students == null) {
                                students = new ArrayList<>();
                            }
                            if (!students.contains(userID)) {
                                students.add(userID);
                            }

                            HashMap<String, Object> updatedValues = new HashMap<>();
                            updatedValues.put("students", students);
                            database.getReference("Schedules").child(schedule)
                                    .updateChildren(updatedValues);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), getString(R.string.cant_subscribe_user), Toast.LENGTH_LONG).show();
                    }
                }
        );

        UserModel.getUserQuery(userID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            UserModel user = ds.getValue(UserModel.class);
                            ArrayList<String> schedules = user.getSchedulesId();
                            if (schedules == null) {
                                schedules = new ArrayList<>();
                            }
                            schedules.add(schedule);
                            ArrayList<String> directions = user.getDirectionsID();
                            if (directions == null) {
                                directions = new ArrayList<>();
                            }
                            directions.add(direction);

                            HashMap<String, Object> updatedValues = new HashMap<>();
                            updatedValues.put("schedulesId", schedules);
                            updatedValues.put("directionsID", directions);

                            database.getReference("Users_data").child(ds.getKey())
                                    .updateChildren(updatedValues);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), getString(R.string.cant_subscribe_user), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // Отправка уведомления
    private void sendNotificationRequest(JSONObject json) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Tasks.NotificationSender(getContext(), json).doInBackground();
            }
        }).start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Перемещение назад при нажатии кнопки
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(getView()).navigateUp();
            }
        });
    }


}