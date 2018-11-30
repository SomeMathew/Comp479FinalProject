package edu.comp479.crawler;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

        // List<String> titles = new ArrayList<>();
        // List<String> bodies = new ArrayList<>();
        List<String> firstPageLinks = new ArrayList<>();
        List<String> visitedLinks = new ArrayList<>();

        visitedLinks.add(firstUrl);

        HashMap<Integer, String> links = te.extractLinksFromHtml(source);
        // titles.add(te.getTitle(source));
        // bodies.addAll(te.getAbsoluteText(source));

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

                // titles.add(te.getTitle(source1));
                // bodies.addAll(te.getAbsoluteText(source1));
                String title1 = te.getTitle(source1);
                String body1 = te.getHtmlBody(source1);

                Document doc = new Document(title1, body1, urlNew);
                documents.add(doc);

                for (Map.Entry<Integer, String> entry : addedLinks.entrySet()) {

                    firstPageLinks.add(entry.getValue());
                }

            }else if((!visitedLinks.contains(firstPageLinks.get(counter)) && (!socialMedia.contains(firstPageLinks.get(counter))))){

                String urlNew = firstPageLinks.get(counter);
                String source1 = crawler.getUrlContents(urlNew);

                visitedLinks.add(firstPageLinks.get(counter));
                addedLinks = te.extractLinksFromHtml(source1);


                String title1 = te.getTitleUrl(urlNew);
                String body1 = te.getBodyUrl(urlNew);

                //titles.addAll(tokenizer.getTokens(title1));
                //bodies.addAll(tokenizer.getTokens(body1));

                Document doc = new Document(title1, body1, urlNew);
                documents.add(doc);

                for (Map.Entry<Integer, String> entry : addedLinks.entrySet()) {

                    firstPageLinks.add(entry.getValue());

                }
            }

            counter++;

        } while (counter < interation);
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
