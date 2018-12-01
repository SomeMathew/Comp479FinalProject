package edu.comp479.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that is used to extract the links and the information off of the web page. Information like the title
 * and the body of the html page
 *
 */
public class TextExtractionEngine {

    private final String ENCODING = "UTF-8";

    /**
     * Gets the Title of the web page from a given URL
     *
     * @param url url to connect to
     * @return String of the title of the web page
     */
    public String getTitleUrl(String url){

        String title = "";

        try{
            Document doc = Jsoup.connect(url).timeout(50000).get();
            title = doc.title();

        }catch (IOException e){
            System.out.println("Connection Timeout");
        }

        return title;
    }

    /**
     * Gets the body of the web page from a given URL
     *
     * @param url url to connect to
     * @return String of the body of the web page
     */
    public String getBodyUrl(String url){
        String body = "";

        try{
            Document doc = Jsoup.connect(url).timeout(50000).get();
            body = doc.text();

        }catch (IOException e){
            System.out.println("Connection Timeout");
        }

        return body;
    }

    /**
     * Gets the Title of the web page from a given web page
     *
     * @param htmlContents contents of the web page
     * @return String of the title of the web page
     */
    public String getTitle(String htmlContents){
        Document doc = Jsoup.parse(htmlContents, ENCODING);
        return doc.title();
    }


    public List<String> getAbsoluteText(String htmlContents) {
        Document doc = Jsoup.parse(htmlContents, ENCODING);
        String textContents = doc.text();
        return splitString(textContents);
    }

    /**
     * Gets the body of the web page from a given web page
     *
     * @param htmlContents contents of the web page
     * @return String of the body of the web page
     */
    public String getHtmlBody(String htmlContents){
        Document doc = Jsoup.parse(htmlContents, ENCODING);
        String body = doc.text();

        return body;
    }

    /**
     * Helper function to split the input based on the space
     *
     * @param input String of information from the website
     * @return result a list of string
     */
    private List<String> splitString(String input) {
        List<String> result = new ArrayList();
        String[] tmp = input.split(" ");

        for (String item : tmp) {
            item = item.trim();
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }


    /**
     * Extracts all the links from the html page
     *
     * @param htmlContents the html contents of the web page
     * @return Hashmap of the links extracted from the web page
     */
    public HashMap<Integer, String> extractLinksFromHtml(String htmlContents) {
        Document doc = Jsoup.parse(htmlContents, ENCODING);
        HashMap<Integer, String> resultsMap = new HashMap();

        Elements links = doc.select("a[href]");
        int index = 0;

        for (Element link : links) {
            String linkUrl = link.attr("href");
            //String linkText = link.text();

            if (!linkUrl.isEmpty() && !linkUrl.startsWith("#")) {
                resultsMap.put(index, linkUrl);
                index++;
            }
        }

        return resultsMap;
    }

}