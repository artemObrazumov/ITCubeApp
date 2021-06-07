package com.artem_obrazumov.it_cubeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class Tasks {
    // AsyncTask для загрузки изображения
    public static class ImageBitmapLoader extends AsyncTask<Void, Void, Bitmap> {
        final String src;

        public ImageBitmapLoader(String src) {
            this.src = src;
        }

        @Override
        public Bitmap doInBackground(Void... voids) {
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // AsyncTask для отправки уведомления
    public static class NotificationSender extends AsyncTask<Void, Void, Void> {
        private final Context context;
        private final JSONObject json;

        public NotificationSender(Context context, JSONObject json) {
            this.context = context;
            this.json = json;
        }

        @Override
        public Void doInBackground(Void... voids) {
            try {
                // Отправка JSON в запрос
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Config.NOTIFICATION_URL,
                        json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {}
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError ignored) {}
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        final Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", Config.AUTHORIZATION_KEY);
                        return headers;
                    }
                };
                requestQueue.add(request);
            } catch (Exception ignored) {}

            return null;
        }
    }
}
