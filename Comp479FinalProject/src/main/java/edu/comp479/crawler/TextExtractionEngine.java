import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 *
 */
public class TextExtractionEngine {

    private final String ENCODING = "UTF-8";


    public String getTitle(String htmlContents){
        Document doc = Jsoup.parse(htmlContents, ENCODING);
        return doc.title();
    }

    public List<String> getAbsoluteText(String htmlContents) {
        Document doc = Jsoup.parse(htmlContents, ENCODING);
        String textContents = doc.text();
        return splitString(textContents);
    }

    public String getHtmlBody(String htmlContents){
        Document doc = Jsoup.parse(htmlContents, ENCODING);
        String body = doc.text();

        return body;
    }

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