package com.midas.consulting.service;

import com.azure.identity.*;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MicrosoftAuthenticationProvider {


//    public static final String SCOPE = "https://graph.microsoft.com/.default";
//    public static final String CLIENT_ID = "56dcf8f2-e377-4061-ac33-61b554435e69";
//    public static final String TENANT_ID = "e6bd6cbd-5bbc-46f9-b7f6-315072511a61";
//    public static final String VERSION_NAME = "v2.0";

    // The client credentials flow requires that you request the
// /.default scope, and pre-configure your permissions on the
// app registration in Azure. An administrator must grant consent
// to those permissions beforehand.
    final List<String> scopes = Arrays.asList("https://graph.microsoft.com/.default");


    public GraphServiceClient<Request> getGraphClient() throws Exception {
        final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId).tenantId(tenantId).clientSecret(clientSecret).build();

        if (null == scopes || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                scopes, credential);


        final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider).buildClient();
        return graphClient;




    }
    public GraphServiceClient<Request> getGraphClientRedirect() throws Exception
    {


        final String authorizationCode = "AUTH_CODE_FROM_REDIRECT";
        final String redirectUrl = "YOUR_REDIRECT_URI";
        final List<String> scopes = Arrays.asList("User.Read");


        final AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
                .clientId(clientId).tenantId(tenantId).clientSecret(clientSecret)
                .authorizationCode(authorizationCode).redirectUrl(redirectUrl).build();

        if (null == scopes || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                scopes, credential);

        final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider).buildClient();
        return graphClient;


    }

    public GraphServiceClient<Request> getGraphClientCert() throws Exception
    {

        Resource resource = new ClassPathResource("pemauth.pem");
        File file = resource.getFile();

        final String clientCertificatePath = file.getPath();

// The client credentials flow requires that you request the
// /.default scope, and pre-configure your permissions on the
// app registration in Azure. An administrator must grant consent
// to those permissions beforehand.
        final List<String> scopes = Arrays.asList("https://graph.microsoft.com/.default");

        final ClientCertificateCredential credential = new ClientCertificateCredentialBuilder()
                .clientId(clientId).tenantId(tenantId).pemCertificate(clientCertificatePath)
                .build();

        if (null == scopes || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                scopes, credential);

        final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider).buildClient();

return  graphClient;
    }
    public GraphServiceClient getGraphClientSec() throws Exception {


// The client credentials flow requires that you request the
// /.default scope, and pre-configure your permissions on the
// app registration in Azure. An administrator must grant consent
// to those permissions beforehand.
        final List<String> scopes = Arrays.asList("https://graph.microsoft.com/.default");

        final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId).tenantId(tenantId).clientSecret(clientSecret).build();

        if (null == scopes || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                scopes, credential);

        final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider).buildClient();
        return graphClient;
    }

    //    final String clientId = "YOUR_CLIENT_ID";
//    final String tenantId = "YOUR_TENANT_ID"; // or "common" for multi-tenant apps
//    final List<String> scopes = Arrays.asList("User.Read");
    public GraphServiceClient<Request> getDeviceGraphClient() throws Exception {
        final DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
                .clientId(clientId).tenantId(tenantId).challengeConsumer(challenge -> {
                    // Display challenge to the user
                    System.out.println(challenge.getMessage());
                }).build();

        if (null == scopes || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                scopes, credential);

        final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider).buildClient();
        return  graphClient;
    }
}