package com.midas.consulting.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class FolderIdFetcher {

    public static void main(String[] args) throws IOException, IOException {
        String accessToken = "eyJ0eXAiOiJKV1QiLCJub25jZSI6Ik1XQXVLTnpWNlJSM0JfcVk0MjdWWE5CQWQzckY0Umh6U1VyNWl6WmFUNE0iLCJhbGciOiJSUzI1NiIsIng1dCI6Ik1HTHFqOThWTkxvWGFGZnBKQ0JwZ0I0SmFLcyIsImtpZCI6Ik1HTHFqOThWTkxvWGFGZnBKQ0JwZ0I0SmFLcyJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNmJkNmNiZC01YmJjLTQ2ZjktYjdmNi0zMTUwNzI1MTFhNjEvIiwiaWF0IjoxNzE5NTE5MzU0LCJuYmYiOjE3MTk1MTkzNTQsImV4cCI6MTcxOTUyMzI1NCwiYWlvIjoiRTJkZ1lGaTRuYkVpVHFuMWtxMlRxTTUzcjVCbkFBPT0iLCJhcHBfZGlzcGxheW5hbWUiOiJlbXBsb3llZS1kb2N1bWVudHMtdXBsb2FkIiwiYXBwaWQiOiI1NmRjZjhmMi1lMzc3LTQwNjEtYWMzMy02MWI1NTQ0MzVlNjkiLCJhcHBpZGFjciI6IjEiLCJpZHAiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNmJkNmNiZC01YmJjLTQ2ZjktYjdmNi0zMTUwNzI1MTFhNjEvIiwiaWR0eXAiOiJhcHAiLCJvaWQiOiJhYzJjMTk1OC05ZTgxLTQ5NjMtYWUzNS0yYzkzMGY5ZDA5MzkiLCJyaCI6IjAuQVVvQXZXeTk1cnhiLVVhMzlqRlFjbEVhWVFNQUFBQUFBQUFBd0FBQUFBQUFBQUNKQUFBLiIsInJvbGVzIjpbIk1haWwuUmVhZFdyaXRlIiwiTWFpbC5SZWFkQmFzaWMuQWxsIiwiQ2hhbm5lbC5SZWFkQmFzaWMuQWxsIiwiT25QcmVtaXNlc1B1Ymxpc2hpbmdQcm9maWxlcy5SZWFkV3JpdGUuQWxsIiwiU2l0ZXMuUmVhZFdyaXRlLkFsbCIsIkF1ZGl0TG9nc1F1ZXJ5LU9uZURyaXZlLlJlYWQuQWxsIiwiRmlsZXMuUmVhZFdyaXRlLkFsbCIsIlNlcnZpY2VBY3Rpdml0eS1PbmVEcml2ZS5SZWFkLkFsbCIsIkZpbGVzLlJlYWQuQWxsIiwiTWFpbC5SZWFkIiwiQWNjZXNzUmV2aWV3LlJlYWQuQWxsIiwiTWFpbC5SZWFkQmFzaWMiXSwic3ViIjoiYWMyYzE5NTgtOWU4MS00OTYzLWFlMzUtMmM5MzBmOWQwOTM5IiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkFTIiwidGlkIjoiZTZiZDZjYmQtNWJiYy00NmY5LWI3ZjYtMzE1MDcyNTExYTYxIiwidXRpIjoiT1VDQ2sxb3lVVWEyWFI4Ull3TEdBQSIsInZlciI6IjEuMCIsIndpZHMiOlsiMDk5N2ExZDAtMGQxZC00YWNiLWI0MDgtZDVjYTczMTIxZTkwIl0sInhtc19pZHJlbCI6IjcgMjYiLCJ4bXNfdGNkdCI6MTY3ODEwNzk2Nn0.O5wtSTRYkyStDpOVX_8iou9fnpR2L9hx-TwtkUQRGhJtqWurANBsM5DRb6H0HbGOBv3uIZradn6DFKSd8-1_4FGfQLZP-_TJYqZf36ALdxatYqXQISjHEIZYsO1_B-JttSjllgdm0wJ6tGYtg3TDkPSINQYNmlpe0Q5SadyrIOmEpKHm6XcCBbvyx6mW5TArVPzHMZJsu5QSixr-N1MPHXUYPvbniDwqXMndOVMM_h1Qu9apq4ZUJKzomSQPyoZ6UpbQEr2vgydrkcRkEf_0RD3xB7i57FURucdKxU8_h75pzFIP5hrBcHdkhE1kUiG9EmqPnTtxWOsqZXhQRBQAqQ";
        String userEmail = "dheeraj.singh@midasconsulting.org"; // User email

        OkHttpClient client = new OkHttpClient();

        String folderUrl = "https://graph.microsoft.com/v1.0/users/" + userEmail + "/mailFolders";

        Request request = new Request.Builder()
                .url(folderUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray folders = jsonResponse.getJSONArray("value");

                for (int i = 0; i < folders.length(); i++) {
                    JSONObject folder = folders.getJSONObject(i);
                    if ("vector-jobs".equals(folder.getString("displayName"))) {
                        String folderId = folder.getString("id");
                        System.out.println("Folder ID: " + folderId);
                        // Use this folder ID in your subscription payload
                    }
                }
            } else {
                System.out.println("Failed to fetch folders: " + response.message());
            }
        }
    }
}
