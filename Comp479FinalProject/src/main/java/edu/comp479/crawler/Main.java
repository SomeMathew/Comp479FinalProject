import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        TextExtractionEngine te = new TextExtractionEngine();
        Webcrawler crawler = new Webcrawler();
        Tokenizer tokenizer = new Tokenizer();

        // you can change this path to a URL path
        String url = "https://www.concordia.ca";
        String firstUrl = "https://www.concordia.ca/about.html";

        String source = crawler.getUrlContents(firstUrl);

        //List<String> titles = new ArrayList<>();
        //List<String>  bodies = new ArrayList<>();
        List<String> firstPageLinks = new ArrayList<>();
        List<String> visitedLinks = new ArrayList<>();

        visitedLinks.add(firstUrl);

        HashMap<Integer, String> links = te.extractLinksFromHtml(source);
        //titles.add(te.getTitle(source));
        //bodies.addAll(te.getAbsoluteText(source));

        String title = te.getTitle(source);
        String body = te.getHtmlBody(source);

        List<String> titles = tokenizer.getTokens(title);
        List<String> bodies = tokenizer.getTokens(body);

        Document document = new Document(title, body);

        List<Document> documents = new ArrayList<>();
        documents.add(document);


        // print all the links in the document
        for (Map.Entry<Integer, String> entry : links.entrySet()) {

            firstPageLinks.add(entry.getValue());

        }


        int counter = 0;
        List<String> merged = new ArrayList<>();

        /**
         *
         * Basically what this do while does, it goes through the links, see if we already visited the link and if
         * we did not visit them, we add the currently link to the visited links list, extract the links from the current
         * page, extract the title and all the html data from the page and put in the title list and bodies list.  We add the current
         * page links to visit into the main list of links.
         *
         *
         * */
        do{
            HashMap<Integer, String> addedLinks = new HashMap<>();

            if(firstPageLinks.get(counter).startsWith("/") && (!visitedLinks.contains(firstPageLinks.get(counter)))){
                String urlNew = url + firstPageLinks.get(counter);
                String source1 = crawler.getUrlContents(urlNew);

                visitedLinks.add(firstPageLinks.get(counter));
                addedLinks = te.extractLinksFromHtml(source1);

                //titles.add(te.getTitle(source1));
                //bodies.addAll(te.getAbsoluteText(source1));

                String title1 = te.getTitle(source1);
                String body1 = te.getHtmlBody(source1);

                titles = tokenizer.getTokens(title1);
                bodies = tokenizer.getTokens(body1);

                Document doc = new Document(title1, body1);
                documents.add(doc);

                for (Map.Entry<Integer, String> entry : addedLinks.entrySet()) {

                    firstPageLinks.add(entry.getValue());

                }

            }

            counter++;

        }while(counter < 100);



        merged.addAll(titles);
        merged.addAll(bodies);

        merged = tokenizer.removeCap(merged);
        merged = tokenizer.removePunctuation(merged);
        merged = tokenizer.removeDigits(merged);

        System.out.println("end");

    }
}
