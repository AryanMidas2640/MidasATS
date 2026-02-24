package com.midas.consulting.util;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class ResumeParsingApi {

	//Set APIURL as rest api url for resume parsing provided by RChilli.
	static final String APIURL = "https://rest.rchilli.com/RChilliParser/Rchilli/parseResumeBinary";
	//Set USERKEY as provided by RChilli.
	static final String USERKEY = "48METS0K";
	static final String VERSION = "8.0.0";

	public static void initParse(String filePath) {
		File resumeFile = new File(filePath);
		try {
			//Set Resume File Pat
			String fileData = Base64.encodeBase64String(Files.readAllBytes(resumeFile.toPath()));
			String subUserId = "Midas Consulting";
			String request = "{" 
								+ "\"filedata\":\"" + fileData + "\"," 
								+ "\"filename\":\"" + resumeFile.getName() + "\"," 
								+ "\"userkey\":\"" + USERKEY + "\"," 
								+ "\"version\":\"" + VERSION + "\","
								+ "\"subuserid\":\"" + subUserId + "\"" 
								+ "}";

			URL url = new URL(APIURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			OutputStream os = conn.getOutputStream();
			os.write(request.getBytes());
			os.flush();
			BufferedReader br = null;
			if (conn.getResponseCode() != 200) {

				br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}
			String output;
			StringBuilder sbOutput = new StringBuilder();
			while ((output = br.readLine()) != null) {
				sbOutput.append(output);
			}
			conn.disconnect();
			br.close();
			output = sbOutput.toString();
			System.out.println(output);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}                 