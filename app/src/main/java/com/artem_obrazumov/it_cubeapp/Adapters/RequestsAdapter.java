package com.artem_obrazumov.it_cubeapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.RequestModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private boolean canPerformChanges = false;

    // Пользователи в адаптере
    private ArrayList<RequestModel> dataSet;

    // Интерфейс для обработки нажатий
    private RequestOnClickListener listener;

    // Конструкторы
    public RequestsAdapter(ArrayList<RequestModel> requests) {
        this.dataSet = requests;
    }

    public RequestsAdapter() {}

    // Обновление слушателя
    public void setListener(RequestOnClickListener listener) {
        this.listener = listener;
    }

    // Обновление списка пользователей
    public void setDataSet(ArrayList<RequestModel> requests) {
        this.dataSet = requests;
        notifyDataSetChanged();
    }

    public void setCanPerformChanges(boolean canPerformChanges) {
        this.canPerformChanges = canPerformChanges;
    }

    // Геттер для списка
    public ArrayList<RequestModel> getDataSet() {
        return dataSet;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // View-элементы
        private final TextView request_author_name;
        private final ImageView request_author_avatar;
        private final TextView request_content;
        private final ImageView apply_button, deny_button, reserve_button;
        private final LinearLayout buttons, reserve_button_container;
        private final TextView extra_data;

        public ViewHolder(View view) {
            super(view);
            request_author_name = view.findViewById(R.id.request_author_name);
            request_author_avatar = view.findViewById(R.id.request_author_avatar);
            request_author_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUserAvatarClicked(dataSet.get(getAdapterPosition()).getUserID());
                }
            });
            request_content = view.findViewById(R.id.request_content);
            apply_button = view.findViewById(R.id.apply_button);
            deny_button = view.findViewById(R.id.deny_button);
            reserve_button = view.findViewById(R.id.reserve_button);
            buttons = view.findViewById(R.id.buttons);
            reserve_button_container = view.findViewById(R.id.reserve_button_container);
            extra_data = view.findViewById(R.id.extra_data);
        }

        public TextView getRequestAuthorName() {
            return request_author_name;
        }

        public ImageView getRequestAuthorAvatar() {
            return request_author_avatar;
        }

        public TextView getRequestContent() {
            return request_content;
        }

        public ImageView getApplyButton() {
            return apply_button;
        }

        public ImageView getDenyButton() {
            return deny_button;
        }

        public ImageView getReserveButton() {
            return reserve_button;
        }

        public LinearLayout getButtons() {
            return buttons;
        }

        public LinearLayout getReserveButtonContainer() {
            return reserve_button_container;
        }

        public TextView getExtraData() {
            return extra_data;
        }

        // Получение информации об отправителе
        public void getSenderData(String userID) {
            UserModel.getUserQuery(userID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                UserModel user = ds.getValue(UserModel.class);
                                getRequestAuthorName().setText(user.getSurname() + " " + user.getName());
                                String avatarURL = user.getAvatar();
                                try {
                                    Glide.with(buttons.getContext()).load(avatarURL).placeholder(R.drawable.default_user_profile_icon)
                                            .into(getRequestAuthorAvatar());
                                } catch (Exception ignored) {}
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            getRequestAuthorName().setVisibility(View.GONE);
                            getRequestAuthorAvatar().setVisibility(View.GONE);
                        }
                    }
            );
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.request_row, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        RequestModel request = dataSet.get(position);
        holder.getRequestContent().setText(request.toString());
        holder.getSenderData(request.getUserID());
        Context context = holder.getReserveButton().getContext();
        holder.getApplyButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.want_to_accept_request))
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onRequestAccepted(request);
                            }
                        })
                        .create().show();
            }
        });
        holder.getDenyButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(params);

                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.want_to_deny_request))
                        .setMessage(context.getString(R.string.deny_reson))
                        .setView(input)
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String reason = input.getText().toString().trim();
                                if (reason.length() >= 10) {
                                    listener.onRequestDenied(request, reason);
                                } else {
                                    Toast.makeText(context, context.getString(R.string.short_reason), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .create().show();
            }
        });
        holder.getReserveButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = new EditText(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(params);

                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.want_to_reserve_request))
                        .setMessage(context.getString(R.string.reserved_reason))
                        .setView(input)
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String reason = input.getText().toString().trim();
                                if (reason.length() >= 10) {
                                    listener.onRequestReserved(request, reason);
                                } else {
                                    Toast.makeText(context, context.getString(R.string.short_reason), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .create().show();
            }
        });

        // Прячем кнопки при невозможности отправить заявку в другое состояние
        if (request.getState() == RequestModel.STATE_DENIED ||
                request.getState() == RequestModel.STATE_ENROLLED ||
                !canPerformChanges) {
            holder.getButtons().setVisibility(View.GONE);
            holder.getExtraData().setVisibility(View.VISIBLE);
        } else {
            holder.getButtons().setVisibility(View.VISIBLE);
            holder.getExtraData().setVisibility(View.GONE);
        }

        if (request.getState() == RequestModel.STATE_RESERVED) {
            holder.getReserveButtonContainer().setVisibility(View.GONE);
        }

        // Отображаем состояние заявки
        if (request.getState() == RequestModel.STATE_UNWATCHED) {
            holder.getExtraData().setText(context.getString(R.string.request_unwatched));
        }

        if (request.getState() == RequestModel.STATE_ACCEPTED) {
            holder.getExtraData().setTextColor(ContextCompat.getColor(context, R.color.light_green));
            holder.getExtraData().setText(context.getString(R.string.request_accepted));
        }

        if (request.getState() == RequestModel.STATE_DENIED) {
            holder.getExtraData().setTextColor(ContextCompat.getColor(context, R.color.light_red));
            holder.getExtraData().setText(context.getString(R.string.denied_because) + " " + request.getReason());
        }

        if (request.getState() == RequestModel.STATE_RESERVED) {
            holder.getExtraData().setTextColor(ContextCompat.getColor(context, R.color.light_blue));
            holder.getExtraData().setText(context.getString(R.string.reserved_because) + " " + request.getReason());
        }

        if (request.getState() == RequestModel.STATE_ENROLLED) {
            holder.getExtraData().setTextColor(ContextCompat.getColor(context, R.color.light_green));
            holder.getExtraData().setText(context.getString(R.string.request_enrolled));
        }
    }

    @Override
    public int getItemCount() {
        try {
            return dataSet.size();
        } catch (Exception e) {
            return 0;
        }
    }

    // Интерфейс для обработки нажатий
    public interface RequestOnClickListener {
        void onUserAvatarClicked(String userID);
        void onRequestAccepted(RequestModel request);
        void onRequestDenied(RequestModel request, String reason);
        void onRequestReserved(RequestModel request, String reason);
    }
}
