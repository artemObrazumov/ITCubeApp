package com.artem_obrazumov.it_cubeapp;

import android.text.TextUtils;

import com.artem_obrazumov.it_cubeapp.Models.ContestPostModel;
import com.artem_obrazumov.it_cubeapp.Models.PostModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Tools {
    public static class VK_Tools {
        public static PostModel getPostFromJSON(JSONObject json) throws JSONException {
            PostModel post = new PostModel();
            post.setDescription(
                    formatPostText(json.getJSONArray("response").getJSONObject(0).get("text").toString())
            );
            ArrayList<String> imagesURL = new ArrayList<>();
            JSONArray attachments = json.getJSONArray("response").getJSONObject(0).getJSONArray("attachments");
            for (int i = 0; i < attachments.length(); i++) {
                JSONObject attachment = attachments.getJSONObject(i);
                if (attachment.getString("type").equals("photo")) {
                    imagesURL.add(attachment.getJSONObject("photo").getString("photo_604"));
                }
            }
            post.setImagesURLs(imagesURL);
            return post;
        }

        public static ContestPostModel getContestPostFromJSON(JSONObject json) throws JSONException {
            PostModel post = getPostFromJSON(json);
            ContestPostModel contestPost = new ContestPostModel();
            contestPost.setDescription(post.getDescription());
            contestPost.setImagesURLs(post.getImagesURLs());
            return contestPost;
        }

        private static String formatPostText(String text) {
            text = text.replaceAll("]", "");
            String[] spittedText = text.split(" ");
            for (int i = 0; i < spittedText.length; i++) {
                String part = spittedText[i];
                if (part.contains("[")) {
                    part = part.split("\\|")[1];
                    spittedText[i] = part;
                }
            }
            return TextUtils.join(" ", spittedText);
        }
    }
}
