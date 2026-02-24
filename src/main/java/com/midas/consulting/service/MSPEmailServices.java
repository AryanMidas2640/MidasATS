package com.midas.consulting.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.midas.consulting.controller.v1.response.microsoft.attachment.AttachmentRoot;
import com.midas.consulting.controller.v1.response.microsoft.email.EmailRoot;
import com.midas.consulting.controller.v1.response.microsoft.email.Value;
import com.midas.consulting.model.OneDriveAuthResponse;
import com.midas.consulting.service.hrms.onedrive.OneDriveServices;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSPEmailServices {

    private OneDriveServices oneDriveServices;
    @Autowired
    public MSPEmailServices(OneDriveServices oneDriveServices){
        this.oneDriveServices = oneDriveServices;
    }

    public AttachmentRoot getFoldersMSPOpenJobs(String subject) throws IOException {
        EmailRoot folderRoot ;
        AttachmentRoot attachmentRoot = null;
        OkHttpClient client = new OkHttpClient();
        ObjectMapper om = new ObjectMapper();
        OneDriveAuthResponse root;
        try {
             root = om.readValue(oneDriveServices.getAccessToken(), OneDriveAuthResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
                System.out.println(root.access_token);
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/users/mayank.kumar@midastravel.org/mailFolders/AAMkADgwOTE1OWY5LTAzMDktNDhmMi1hYTU3LWY4YWZlN2UxZGRkMQAuAAAAAAACYdqDdp0xRrADd7rkvnTJAQBzEe16O9khT5-DjW_VvTa0AADYyYBJAAA=/messages")
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "insomnia/8.6.0")
                .addHeader("Authorization", "Bearer "+root.access_token)
                .build();
        om = new ObjectMapper();
        Response foldersResponse = client.newCall(request).execute();
        String responseData = foldersResponse.body().string();
        try {
            folderRoot = om.readValue(responseData, EmailRoot.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


     List<Value> valueList=   folderRoot.getValue().stream()
                .max(Comparator.comparing(Value::getReceivedDateTime))
                .map(latestItem ->
                        folderRoot.getValue().stream()
                                .filter(item -> item.getReceivedDateTime().equals(latestItem.getReceivedDateTime()))
                                .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());

                try {
                    attachmentRoot=   getAttachment(valueList.get(0).getId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

        return attachmentRoot;
    }

    public AttachmentRoot getAttachment(String messageId) throws IOException {
        AttachmentRoot attachmentRoot ;
        OkHttpClient client = new OkHttpClient();
        ObjectMapper om = new ObjectMapper();
        OneDriveAuthResponse root;
        try {
            root = om.readValue(oneDriveServices.getAccessToken(), OneDriveAuthResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/users/mayank.kumar@midastravel.org/messages/"+messageId+"/attachments")
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "insomnia/8.6.0")
                .addHeader("Authorization", "Bearer "+root.access_token)
                .build();
        om = new ObjectMapper();
        Response foldersResponse = client.newCall(request).execute();
        String responseData = foldersResponse.body().string();
        try {
             attachmentRoot= om.readValue(responseData, AttachmentRoot.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return attachmentRoot;
    }
}
