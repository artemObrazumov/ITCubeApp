package com.artem_obrazumov.it_cubeapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.ScheduleModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    // Информация в адаптере
    private ArrayList<ScheduleModel> dataSet;

    // Слушатель для обработки нажатий
    private UsersAdapter.OnUserClickListener onUserClickListener;

    // Изменение информации в адаптере
    public void setDataSet(ArrayList<ScheduleModel> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    // Изменение слушателя для списка пользователя
    public void setOnUserClickListener(UsersAdapter.OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        // View-элементы
        private final TextView groupName;
        private final RecyclerView studentsList;

        private final UsersAdapter adapter;

        public ViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.group_name);
            studentsList = view.findViewById(R.id.students_list);

            adapter = new UsersAdapter();
            adapter.setOnUserClickListener(onUserClickListener);
            studentsList.setAdapter(adapter);
        }

        public TextView getGroupName() {
            return groupName;
        }

        public RecyclerView getStudentsList() {
            return studentsList;
        }

        public UsersAdapter getAdapter() {
            return adapter;
        }

        // Инициализация списка пользователей в адаптере
        public void initializeData(ArrayList<String> studentsList) {
            ArrayList<UserModel> students = new ArrayList<>();
            for (int i = 0; i < studentsList.size(); i++) {
                UserModel.getUserQuery(studentsList.get(i)).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()) {
                                    UserModel student = ds.getValue(UserModel.class);
                                    students.add(student);
                                }
                                adapter.updateUsersList(students);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        }
                );
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleModel group = dataSet.get(position);
        holder.getGroupName().setText(group.getDirectionName());

        Context context = holder.getGroupName().getContext();
        holder.getStudentsList().setLayoutManager(new LinearLayoutManager(context));
        holder.getStudentsList().setNestedScrollingEnabled(false);

        holder.initializeData(group.getStudents());
    }

    @Override
    public int getItemCount() {
        try {
            return dataSet.size();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
