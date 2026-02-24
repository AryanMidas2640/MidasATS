package com.midas.consulting.service.hrms.onedrive;

import com.midas.consulting.model.OneDriveConfig;
import okhttp3.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface OneDriveServices {
    Response upload(MultipartFile file) throws IOException;
    String GetAccessToken() throws IOException;
    String GetAccessToken(OneDriveConfig oneDriveConfig) throws IOException;
    Response getFile(String itemId) throws IOException;
    Response uploadCandidateResume(MultipartFile file) throws IOException ;
    Response upload(File file) throws IOException;

    String getAccessToken() throws IOException;
}



//package com.midas.consulting.service.hrms.onedrive;
//
//import okhttp3.Response;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//
//public interface OneDriveServices {
//    Response upload(MultipartFile file) throws IOException;
//    String getAccessToken() throws IOException;
//    Response getFile(String itemId) throws IOException;
//     Response uploadCandidateResume(MultipartFile file) throws IOException ;
//    Response upload(File file) throws IOException;
//
//    byte[] downloadFile(String id) throws IOException;
//}
