package edu.comp479.crawler;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CrawlerMain {
    private static Logger LOGGER = Logger.getLogger(CrawlerMain.class.getName());

    private List<Document> documents;

    public List<Document> getDocuments() {
        return documents;
    }

    public void execute(int interation) throws IOException {

        TextExtractionEngine te = new TextExtractionEngine();
        Webcrawler crawler = new Webcrawler();

        // you can change this path to a URL path
        String url = "https://www.concordia.ca";
        String firstUrl = "https://www.concordia.ca/about.html";

        String source = crawler.getUrlContents(firstUrl);
        List<String> firstPageLinks = new ArrayList<>();
        List<String> visitedLinks = new ArrayList<>();

        visitedLinks.add(firstUrl);

        HashMap<Integer, String> links = te.extractLinksFromHtml(source);

        String title = te.getTitle(source);
        String body = te.getHtmlBody(source);

        Document document = new Document(title, body, firstUrl);

        documents = new ArrayList<>();
        documents.add(document);

        // print all the links in the document
        for (Map.Entry<Integer, String> entry : links.entrySet()) {

            firstPageLinks.add(entry.getValue());

        }

        int counter = 0;
        List<String> socialMedia = new ArrayList<>();
        socialMedia.add("facebook");
        socialMedia.add("twitter");
        socialMedia.add("linkedin");
        socialMedia.add("instagram");
        socialMedia.add("flickr");
        socialMedia.add("pinterest");
        socialMedia.add("youtube");

        /**
         *
         * Basically what this do while does, it goes through the links, see if we
         * already visited the link and if we did not visit them, we add the currently
         * link to the visited links list, extract the links from the current page,
         * extract the title and all the html data from the page and put in the title
         * list and bodies list. We add the current page links to visit into the main
         * list of links.
         *
         *
         */
        do {
            HashMap<Integer, String> addedLinks = new HashMap<>();

            if (firstPageLinks.get(counter).startsWith("/") && (!visitedLinks.contains(firstPageLinks.get(counter)))) {
                String urlNew = url + firstPageLinks.get(counter);
                String source1 = crawler.getUrlContents(urlNew);

                visitedLinks.add(firstPageLinks.get(counter));
                addedLinks = te.extractLinksFromHtml(source1);


                String title1 = te.getTitle(source1);
                String body1 = te.getHtmlBody(source1);

                Document doc = new Document(title1, body1, urlNew);
                documents.add(doc);

                for (Map.Entry<Integer, String> entry : addedLinks.entrySet()) {

                    firstPageLinks.add(entry.getValue());
                }

            }else if((!visitedLinks.contains(firstPageLinks.get(counter)) && (!socialMedia.contains(firstPageLinks.get(counter))))){

                String urlNew = firstPageLinks.get(counter);

                try {

                    //If true than the url allows us to crawl the information on the site
                    if(canCrawl(urlNew)) {


                        String source1 = crawler.getUrlContents(urlNew);

                        visitedLinks.add(firstPageLinks.get(counter));
                        addedLinks = te.extractLinksFromHtml(source1);


                        String title1 = te.getTitleUrl(urlNew);
                        String body1 = te.getBodyUrl(urlNew);

                        Document doc = new Document(title1, body1, urlNew);
                        documents.add(doc);

                        for (Map.Entry<Integer, String> entry : addedLinks.entrySet()) {

                            firstPageLinks.add(entry.getValue());

                        }
                    }
                }catch (Exception e){
                    System.out.println("Cannot Connect to Website");
                }
            }

            counter++;

        } while (counter < interation);
    }


    /**
     * Static method that will determine if a site other than concordia.ca has a robot.txt file and see if we
     * are allowed to crawl the site for its information
     *
     * @param urlPath url to connect to
     * @return boolean to see if the website has a robot.txt
     */
    public static boolean canCrawl(String urlPath) throws IOException {

        //Pattern to match the beginning of a website
        Pattern p = Pattern.compile("^(http(s?)://([^/]+))");
        Matcher m = p.matcher(urlPath);
        boolean canCrawl = true;

        //if there is a match then add the robots to the end of the site
        if (m.find()) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(new URL(m.group(1) + "/robots.txt").openStream()))) {

                String line = null;
                while ((line = in.readLine()) != null) {

                    //Regex to see if the robots.txt has a disallow section
                    Pattern pattern = Pattern.compile("User-agent: (.*)|Disallow: (.*)");
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        canCrawl = false;

                    }
                }
                in.close();

            } catch (Exception e) {
                System.out.println("No Robots.txt File!");
            }
        }

        return canCrawl;
    }

    public List<Long> dumpToDisk(DocDiskManager docDiskManager) {
        LOGGER.info("Creating document cache on disk.");
        return documents.stream().peek(docDiskManager::writeToDisk).map(Document::getDocumentId)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException {
        CrawlerMain main = new CrawlerMain();
        main.execute(10);
        System.out.println("end");
    }
}
