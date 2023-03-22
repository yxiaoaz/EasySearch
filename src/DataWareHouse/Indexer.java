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
    public void processContent(URL url) throws IOException {
        ArrayList<String> terms = extractTerms(url);
        for(int i=0;i< terms.size();i++){
            String t = terms.get(i);
            indexTerm(t,i,url);
        }
    }

    /**
     * Index a Single Term (not mapped to ID)
     * */
    public void indexTerm(String term, int position, URL url) throws IOException {
        HTree invertedIndex = fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile();
        String termID = ID_Mapping.Term2ID(term);
        String docID = ID_Mapping.URL2ID(url);

        // acquire(construct new) corresponding posting list
        ArrayList<IIPosting> postingList;
        if(invertedIndex.get(termID)==null){
            postingList = new ArrayList<>();

            //to be included in a IIPosting: which document, where in the document
            IIPosting newPosting = new IIPosting(docID);
            newPosting.addPosition(position);
            postingList.add(newPosting);
        }
        else{
            postingList = (ArrayList<IIPosting>) invertedIndex.get(termID);
            IIPosting oldPosting= new IIPosting(docID);;
            for(int j = 0;j<postingList.size();j++){
                if (postingList.get(j).getID().equals(docID)){
                    oldPosting = postingList.get(j);
                    break;
                }
            }

            oldPosting.addPosition(position);
            postingList.add(oldPosting);
        }

        invertedIndex.put(termID,postingList);
    }

}
