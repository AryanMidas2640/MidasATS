package com.midas.consulting.controller.v1.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.xml.transform.Source;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
///api/davinhealthcareallFeed
@RestController
@RequestMapping("/api/v1/davinhealthcare")
@EnableCaching
@CrossOrigin(origins = "*")
public class DavinHealthcareController {

    private final Logger logger = LoggerFactory.getLogger(DavinHealthcareController.class);
    final int size = 16 * 10246 * 10246;

    @GetMapping("/allFeed")
    public String getAllFeeds() {
        logger.info("Started The XMl Into JSON" + LocalDateTime.now());
        String jsonPrettyPrintString = "";
        RestTemplate restTemplate = new RestTemplate();
        String xmlData = restTemplate.getForObject("https://davin.davinhealthcare.com/files/publish/open_contracts_000232.xml", String.class);
        logger.info("Completed the flux" + LocalDateTime.now());
        logger.error(xmlData);
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(xmlData);
            jsonPrettyPrintString = xmlJSONObj.toString(4);
            return jsonPrettyPrintString;
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
        return jsonPrettyPrintString;
    }
}