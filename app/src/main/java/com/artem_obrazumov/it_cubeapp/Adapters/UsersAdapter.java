package com.artem_obrazumov.it_cubeapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    // Пользователи в адаптере
    private ArrayList<UserModel> dataSet;
    private ArrayList<UserModel> dataSetBackUp = new ArrayList<>();

    // Интерфейс для обработки нажатий
    private OnUserClickListener onUserClickListener;

    // Конструкторы
    public UsersAdapter(ArrayList<UserModel> users) {
        this.dataSet = users;
        copyBackup();
    }

    public UsersAdapter() {}

    // Обновление слушателя
    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    // Обновление списка пользователей
    public void updateUsersList(ArrayList<UserModel> users) {
        this.dataSet = users;
        copyBackup();
        notifyDataSetChanged();
    }

    // Геттер для списка
    public ArrayList<UserModel> getDataSet() {
        return dataSet;
    }

    // Поиск учеников
    public void filter(String query) {
        dataSet.clear();
        if(query.isEmpty()){
            dataSet.addAll(dataSetBackUp);
        } else {
            query = query.toLowerCase();
            for(UserModel user: dataSetBackUp){
                if(user.getName().toLowerCase().contains(query)
                    || user.getSurname().toLowerCase().contains(query)){
                    dataSet.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Создание копии всех учеников в списке
    public void copyBackup() {
        dataSetBackUp.clear();
        dataSetBackUp.addAll(dataSet);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView user_avatar;
        private final TextView user_name, user_status;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            user_avatar = view.findViewById(R.id.user_avatar);
            user_name = view.findViewById(R.id.user_name);
            user_status = view.findViewById(R.id.user_status);
        }

        public ImageView getUserAvatar() {
            return user_avatar;
        }

        public TextView getUserName() {
            return user_name;
        }

        public TextView getUserStatus() {
            return user_status;
        }

        @Override
        public void onClick(View v) {
            // Обработка нажатия на элемент
            String userID = dataSet.get(getAdapterPosition()).getUid();
            onUserClickListener.onUserClicked(userID);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_row, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        UserModel user = dataSet.get(position);
        viewHolder.getUserName().setText(user.getName() + " " + user.getSurname());
        viewHolder.getUserStatus().setText(UserModel.getUserStatusName(user.getUserStatus()));
        // Загрузка аватарки
        try {
            Glide.with(viewHolder.getUserAvatar()).load(user.getAvatar()).placeholder(R.drawable.default_user_profile_icon).into(viewHolder.getUserAvatar());
        } catch (Exception e) {
            // Если не удалось загрузить изображение с интернета, ничего не делаем
        }
    }

    @Override
    public int getItemCount() {
        try {
            return dataSet.size();
        } catch (Exception ignored) {
            return 0;
        }
    }

    // Интерфейс для обработки нажатий
    public interface OnUserClickListener {
        void onUserClicked(String userID);
        void onUserStatusChange(String userID, int newStatus);
        void onUserBanned(String userID);
    }
}
