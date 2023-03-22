package DataWareHouse;
import IRUtilities.StopStem;
import org.jsoup.Jsoup;
import jdbm.htree.HTree;
import org.htmlparser.beans.StringBean;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Indexer {
    //The database the indexer is serving
    private FileManager fileManager;
    private static StopStem stopStem;

    /**
     * Code of Conduct: Create FileManager-> affiliate indexer to FileManager
     * */
    public Indexer(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        if(stopStem == null)
            stopStem = new StopStem("stopwords.txt");
    }

    /**
     * @return a list of stemmed terms extracted from the url
     * */
    public ArrayList<String> extractTerms(URL url) throws IOException {
        ArrayList<String> res = new ArrayList<>();

        Document doc = Jsoup.connect(url.toString()).get();
        String text = doc.body().text();
        StringTokenizer st = new StringTokenizer(text);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if(stopStem.isStopWord(t))
                res.add(stopStem.stem(t));
        }
        return res;
    }

    /**
     @param url: where to fetch the terms from
     inverted index files grouped by first letter of the term
     * */
    public ArrayList<String> indexTerms(URL url) throws IOException {
        ArrayList<String> terms = extractTerms(url);
        for(int i=0;i< terms.size();i++){
            String t = terms.get(i);
            
        }
    }

}
