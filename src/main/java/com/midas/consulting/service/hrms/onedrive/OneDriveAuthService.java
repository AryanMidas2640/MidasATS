package com.midas.consulting.service.hrms.onedrive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midas.consulting.constants.OneDriveConstants;
import com.midas.consulting.model.OneDriveAuthResponse;
import com.midas.consulting.model.OneDriveConfig;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class OneDriveAuthService implements  OneDriveServices{


    private  final Logger logger= LoggerFactory.getLogger(OneDriveAuthService.class);
    private String clientId;
    private String clientSecret;
    // Don't use password grant in your apps. Only use for legacy solutions and automated testing.
    private String grantType = "client_credentials";
    private String tokenEndpoint = "https://login.microsoftonline.com/" + OneDriveConstants.TENANT_ID + "/oauth2/" + OneDriveConstants.VERSION_NAME + "/token";
    private String resourceId = "https%3A%2F%2Fgraph.microsoft.com%2F.default";


    public String getAccessToken() throws IOException {
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, String.format("client_id=%1$s&client_secret=%2$s&grant_type=%3$s&scope=%4$s",
                    OneDriveConstants.CLIENT_ID,
                    OneDriveConstants.CLIENT_SECRET,
                    grantType,
                    resourceId
            ));
            Request request = new Request.Builder()
                    .url(tokenEndpoint)
                    .post(body)
                    .addHeader("cookie", "fpc=AomIw3FjH6hNvWfFD8odWc9w5RZsAwAAALZCHt0OAAAA; esctx=PAQABAAEAAAAmoFfGtYxvRrNriQdPKIZ-eoxVkVwCxfLjAQwhlR7yfpMzZGVcjq_LEm1ndTdoduUhI-86Pi04kWjKnzLCwZpShkNoLrqobosH7WcItD0anxmPoeSvkWK3-xqFRcgAwySOc-_45IVkAi8uRVhmNQPHzMdLJKiYu8jXkFs_FozNt_RV1-7NoeVtPeGfWt3RazIgAA; x-ms-gateway-slice=estsfd; stsservicecookie=estsfd")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("User-Agent", "insomnia/8.5.1")
                    .build();
            response = client.newCall(request).execute();
        } catch (Exception exception) {
            System.out.println("Issues in authentication" + exception.getMessage());
        }
        return response.body().string();
    }

    // Add this method to your OneDriveAuthService class



    public Response upload(MultipartFile file) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body =  RequestBody.create(MediaType.parse(file.getContentType()), file.getBytes());
        ObjectMapper om = new ObjectMapper();
        OneDriveAuthResponse root = om.readValue(getAccessToken(), OneDriveAuthResponse.class);
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/Drives/b!uPqgbIcqFU6hRNh2NNy1aXJWPRtHdIhBmC4SZAJhOhBCF-UF6RIYQ7WCbzH_wEcf/root:/hrms-employee-docs/"+ UUID.randomUUID()+"-"+file.getOriginalFilename() +":/content")
                .put(body)
                .addHeader("User-Agent", "insomnia/8.5.1")
                .addHeader("Authorization", "Bearer "+root.access_token)
                .build();

        Response response = client.newCall(request).execute();
        return response;
      //  System.out.println(response.body().string());
    }

    /**
     * @return
     * @throws IOException
     */
    @Override
    public String GetAccessToken() throws IOException {
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, String.format("",
                    OneDriveConstants.CLIENT_ID,
                    OneDriveConstants.CLIENT_SECRET,
                    grantType,
                    resourceId
            ));
            Request request = new Request.Builder()
                    .url(tokenEndpoint)
                    .post(body)
                    .addHeader("cookie", "fpc=AomIw3FjH6hNvWfFD8odWc9w5RZsAwAAALZCHt0OAAAA; esctx=PAQABAAEAAAAmoFfGtYxvRrNriQdPKIZ-eoxVkVwCxfLjAQwhlR7yfpMzZGVcjq_LEm1ndTdoduUhI-86Pi04kWjKnzLCwZpShkNoLrqobosH7WcItD0anxmPoeSvkWK3-xqFRcgAwySOc-_45IVkAi8uRVhmNQPHzMdLJKiYu8jXkFs_FozNt_RV1-7NoeVtPeGfWt3RazIgAA; x-ms-gateway-slice=estsfd; stsservicecookie=estsfd")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("User-Agent", "insomnia/8.5.1")
                    .build();
            response = client.newCall(request).execute();
        } catch (Exception exception) {
            System.out.println("Issues in authentication" + exception.getMessage());
        }
        return response.body().string();
    }

    /**
     * @param oneDriveConfig
     * @return
     * @throws IOException
     */
    @Override
    public String GetAccessToken(OneDriveConfig oneDriveConfig) throws IOException {
        return null;
    }

    public  Response getFile(String itemId) throws IOException {
//        /sites/{site-id}/drive/items/{item-id}
        OkHttpClient client = new OkHttpClient();
        ObjectMapper om = new ObjectMapper();
        OneDriveAuthResponse root = om.readValue(getAccessToken(), OneDriveAuthResponse.class);
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/sites/{site-id}/drive/items/" + itemId)
                .get()
                .addHeader("User-Agent", "insomnia/8.5.1")
                .addHeader("Authorization", "Bearer " + root.access_token)
                .build();

        Response response = client.newCall(request).execute();
        return response;

    }

    public byte[] downloadFile(String fileId) throws IOException {
        try {
            // Get access token



            JSONObject jsonObject = new JSONObject(getAccessToken());
            String accessToken =jsonObject.getString ("access_token");

            // Build download URL
//            String downloadUrl = String.format("https://graph.microsoft.com/v1.0/me/drive/items/%s/content", fileId);

//                            .url("https://graph.microsoft.com/v1.0/sites/{site-id}/drive/items/" + itemId)

            // Create HTTP request
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://graph.microsoft.com/v1.0/sites/midasconsultingmgmt.sharepoint.com,6ca0fab8-2a87-4e15-a144-d87634dcb569,1b3d5672-7447-4188-982e-126402613a10/drive/items/" + fileId)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            // Execute request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file from OneDrive: " + response.code() + " " + response.message());
                }

                if (response.body() == null) {
                    throw new IOException("Empty response body when downloading file from OneDrive");
                }

                return response.body().bytes();
            }

        } catch (Exception e) {
            logger.error("Error downloading file {} from OneDrive: {}", fileId, e.getMessage(), e);
            throw new IOException("Failed to download file from OneDrive", e);
        }
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public Response uploadCandidateResume(MultipartFile file) throws IOException {
        OkHttpClient client = new OkHttpClient();


        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body =  RequestBody.create(MediaType.parse("application/octet-stream"), file.getBytes());
        ObjectMapper om = new ObjectMapper();
        OneDriveAuthResponse root = om.readValue(getAccessToken(), OneDriveAuthResponse.class);
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/Drives/b!uPqgbIcqFU6hRNh2NNy1aXJWPRtHdIhBmC4SZAJhOhBCF-UF6RIYQ7WCbzH_wEcf/root:/employee-db-resume/"+ UUID.randomUUID()+"-"+file.getOriginalFilename() +":/content")
                .put(body)
                .addHeader("User-Agent", "insomnia/8.5.1")
                .addHeader("Authorization", "Bearer "+root.access_token)
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }


    // for change


    public Response upload(File file) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body =  RequestBody.create(MediaType.parse("application/octet-stream"), file);
        ObjectMapper om = new ObjectMapper();
        OneDriveAuthResponse root = om.readValue(getAccessToken(), OneDriveAuthResponse.class);
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/Drives/b!uPqgbIcqFU6hRNh2NNy1aXJWPRtHdIhBmC4SZAJhOhBCF-UF6RIYQ7WCbzH_wEcf/root:/hrms-employee-docs/"+file.getName()+":/content")
                .put(body)
                .addHeader("User-Agent", "insomnia/8.5.1")
                .addHeader("Authorization", "Bearer "+root.access_token)
                .build();

        Response response = client.newCall(request).execute();
        return response;
        //  System.out.println(response.body().string());
    }
}

