public class Document {

    private int documentId = 0;
    private String title;
    private String body;
    private int count = 0;


    public Document(String title, String body){

        this.title = title;
        this.body = body;
        this.documentId = count;

        count++;

    }

    public String getTitle(){
        return this.title;
    }

    public String getBody(){
        return this.body;
    }

    public int getDocumentId(){
        return this.documentId;
    }
}
