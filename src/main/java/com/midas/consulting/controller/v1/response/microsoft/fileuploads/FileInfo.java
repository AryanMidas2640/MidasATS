package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class FileInfo {
    private String odataContext;
    private String microsoftGraphDownloadUrl;
    private String createdDateTime;
    private String eTag;
    private String id;
    private String lastModifiedDateTime;
    private String name;
    private String webUrl;
    private String cTag;
    private int size;
    private CreatedBy createdBy;
    private LastModifiedBy lastModifiedBy;
    private ParentReference parentReference;
    private File file;
    private FileSystemInfo fileSystemInfo;
    private Shared shared;

    // Getters and Setters

    public static class CreatedBy {
        private Application application;
        private User user;

        // Getters and Setters
    }

    public static class LastModifiedBy {
        private Application application;
        private User user;

        // Getters and Setters
    }

    public static class Application {
        private String id;
        private String displayName;

        // Getters and Setters
    }

    public static class User {
        private String displayName;

        // Getters and Setters
    }

    public static class ParentReference {
        private String driveType;
        private String driveId;
        private String id;
        private String name;
        private String path;
        private String siteId;

        // Getters and Setters
    }

    public static class File {
        private String mimeType;
        private Hashes hashes;

        // Getters and Setters
    }

    public static class Hashes {
        private String quickXorHash;

        // Getters and Setters
    }

    public static class FileSystemInfo {
        private String createdDateTime;
        private String lastModifiedDateTime;

        // Getters and Setters
    }

    public static class Shared {
        private String scope;

        // Getters and Setters
    }
}
