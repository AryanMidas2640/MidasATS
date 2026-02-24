package com.midas.consulting.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

public class EmailParser {
    public static void main(String[] args) {
        String htmlContent = "<html><body><table><tr>" +
                "<td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">Day </td>" +
                "<td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">" +
                "<div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">" +
                "<div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:58px; background:#f4f4f4\">" +
                "<div style=\"padding-top:18px\">Range</div>" +
                "</div>" +
                "<div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:55px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">" +
                "<div style=\"border-bottom:1px dotted #ccc; padding-top:5px; padding-bottom:5px; min-height:16px\">$72.00</div>" +
                "<div style=\"margin-top:5px\">$72.00</div>" +
                "</div>" +
                "<div style=\"clear:both\"></div>" +
                "</div><br>" +
                "<div style=\"width:180px; border-radius:2px; text-align:right; color:#444; margin:auto; margin-top:5px\">" +
                "<div style=\"width:45%\"></div>" +
                "<div style=\"width:55%; padding:2px; min-height:55px; padding-left:5px; float:right; background:#fff\">" +
                "<div style=\"border-bottom:1px dotted #ccc; padding-top:5px; padding-bottom:5px\">" +
                "<span style=\"color:red; text-decoration:line-through\">" +
                "<div style=\"white-space:pre-line; padding-right:2px\">60.00</div></span>" +
                "</div>" +
                "<div style=\"margin-top:5px\">" +
                "<span style=\"color:red; text-decoration:line-through\">" +
                "<div style=\"white-space:pre-line; padding-right:2px\">65.00</div></span>" +
                "</div>" +
                "</div>" +
                "<div style=\"clear:both\"></div>" +
                "</div></td>" +
                "</tr></table></body></html>";

        Document doc = Jsoup.parse(htmlContent);

        // Parsing the table row
        Element tableRow = doc.select("tr").first();

        // Extracting the data
        String day = tableRow.select("td").get(0).text();
        Elements values = tableRow.select("td").get(1).select("div");

        String currentRate1 = values.get(1).select("div").get(0).text();
        String currentRate2 = values.get(1).select("div").get(1).text();

        String previousRate1 = values.get(3).select("div").get(0).select("div").text();
        String previousRate2 = values.get(3).select("div").get(1).select("div").text();

        // Storing the data in a HashMap
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("Day", day);
        dataMap.put("CurrentRate1", currentRate1);
        dataMap.put("CurrentRate2", currentRate2);
        dataMap.put("PreviousRate1", previousRate1);
        dataMap.put("PreviousRate2", previousRate2);

        // Printing the collected data
        dataMap.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
