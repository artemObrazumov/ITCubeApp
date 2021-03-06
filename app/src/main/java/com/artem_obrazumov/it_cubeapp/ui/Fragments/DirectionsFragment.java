package com.artem_obrazumov.it_cubeapp.ui.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.artem_obrazumov.it_cubeapp.UserData;
import com.artem_obrazumov.it_cubeapp.databinding.FragmentDirectionsBinding;
import com.artem_obrazumov.it_cubeapp.ui.Activities.MyRequestsActivity;
import com.artem_obrazumov.it_cubeapp.ui.Activities.RequestFormActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.DirectionsListAdapter;
import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DirectionsFragment extends Fragment {

    // Binding
    private FragmentDirectionsBinding binding;

    // Адаптеры для списков
    private DirectionsListAdapter userDirectionsAdapter;
    private DirectionsListAdapter availableDirectionsAdapter;

    // Firebase
    private FirebaseAuth auth;

    private String childId = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentDirectionsBinding.inflate(inflater);
        View root = binding.getRoot();

        // Инициализация элементов
        auth = FirebaseAuth.getInstance();
        if (UserData.thisUser.getUserStatus() != UserModel.STATUS_PARENT) {
            initializeLists(auth.getCurrentUser().getUid());
        }
        else {
            initializeListWithChild();
        }

        return root;
    }

    private void initializeListWithChild() {
        UserData.CreateChildrenNames();
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select_child))
                .setItems(UserData.userChildrenNames.toArray(new String[0]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                childId = UserData.userChildrenList.get(which).getUid();
                                initializeLists(childId);
                            }
                        })
                .create().show();
    }

    // Инициализация адаптеров
    private void initializeLists(String id) {
        UserModel.getUserQuery(id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserModel user = ds.getValue(UserModel.class);
                            initializeCoursesList(user.getDirectionsID());
                            initializeAvailableDirectionsList(
                                    user.getCubeId(), user.getDirectionsID());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Инициализация списка с курсами, на которые записан пользователь
    private void initializeCoursesList(HashMap<String, Object> directionsIDs) {
        ArrayList<DirectionModel> userDirections = new ArrayList<>();
        userDirectionsAdapter = new DirectionsListAdapter(userDirections);
        binding.userDirectionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.userDirectionsList.setNestedScrollingEnabled(false);
        binding.userDirectionsList.setAdapter(userDirectionsAdapter);
        // Создаем разделитель между направлениями
        try {
            DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
            binding.userDirectionsList.addItemDecoration(divider);
        } catch (Exception ignored) {}

        // Заполнение адаптера данными о направлениях пользователя
        for (String key : directionsIDs.keySet()) {
            DirectionModel.getDirectionQuery(key).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                userDirections.add(ds.getValue(DirectionModel.class));
                            }
                            userDirectionsAdapter.setDataSet(userDirections);
                            if (userDirections.size() == 0) {
                                // Если список направлений пуст, то прячем его
                                binding.directionsListTitle.setVisibility(View.GONE);
                                binding.userDirectionsList.setVisibility(View.GONE);
                            } else {
                                binding.directionsListTitle.setVisibility(View.VISIBLE);
                                binding.userDirectionsList.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    // Инициализация списка с курсами, на которые может записаться пользователь
    private void initializeAvailableDirectionsList(String cubeId, HashMap<String, Object> userDirectionsIDs) {
        // Создаем разделитель между направлениями
        DividerItemDecoration divider =
                new DividerItemDecoration(binding.availableDirectionsList.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        binding.availableDirectionsList.addItemDecoration(divider);

        ArrayList<DirectionModel> availableDirections = new ArrayList<>();
        availableDirectionsAdapter = new DirectionsListAdapter(availableDirections, DirectionsListAdapter.MODE_AVAILABLE);
        availableDirectionsAdapter.setOnDirectionClickListener(new DirectionsListAdapter.OnDirectionClickListener() {
            @Override
            public void onDirectionSubscribeButtonClicked(String directionID) {
                Intent intent = new Intent(getActivity(), RequestFormActivity.class);
                intent.putExtra("directionID", directionID);
                if (childId != null) {
                    intent.putExtra("Uid", childId);
                }
                startActivity(intent);
            }
        });
        binding.availableDirectionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.availableDirectionsList.setNestedScrollingEnabled(false);
        binding.availableDirectionsList.setAdapter(availableDirectionsAdapter);

        FirebaseDatabase.getInstance().getReference("IT_Cubes/" + cubeId + "/directions").
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    /* Если мы видим направление, которое доступено в этом кубе, но его нет у
                       пользователя, то добавляем в список с доступными курсами */
                    final String key = ds.getKey();
                    if (!userDirectionsIDs.containsKey(key)) {
                        DirectionModel.getDirectionQuery(key).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            availableDirections.add(ds.getValue(DirectionModel.class));
                                        }
                                        // Инициализация адаптера полученными данными
                                        availableDirectionsAdapter.setDataSet(availableDirections);
                                        if (availableDirections.size() == 0) {
                                            // Если список доступных направлений пуст, то прячем его
                                            binding.availableDirectionsListTitle.setVisibility(View.GONE);
                                            binding.availableDirectionsList.setVisibility(View.GONE);
                                        } else {
                                            binding.availableDirectionsListTitle.setVisibility(View.VISIBLE);
                                            binding.availableDirectionsList.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Добавление уникальных объектов в меню фрагмента
        inflater.inflate(R.menu.directions_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_view_requests) {
            startActivity(new Intent(getContext(), MyRequestsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}