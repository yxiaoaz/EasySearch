package DataWareHouse;

import java.net.MalformedURLException;
import java.net.URL;

public class FileNameGenerator {
    public static final String INVERTEDINDEX = "Inverted-";
    public static final String FORWARDINDEX = "Forward-";
    public static final String PARENT2CHILD = "Web Graph Parent as key-";
    public static final String CHILD2PARENT = "Web Graph Child as key-";

    public static final String DOCRECORDS = "Web Page Profile";

    public static String getInvertedIndexFileName(String term){
        //return INVERTEDINDEX+term.charAt(0);
        return INVERTEDINDEX;
    }

    public static String getForwardIndexFileName(URL url){
        return FORWARDINDEX;
    }
    public static String getForwardIndexFileName(String urlID) throws MalformedURLException {
        return getForwardIndexFileName(ID_Mapping.PageID2URL(urlID));
    }
    public static String getWebGraphName_parent2child(URL url){

        //return PARENT2CHILD+url.getHost();
        return PARENT2CHILD;
    }
    public static String getWebGraphName_parent2child(String docID) throws MalformedURLException {

        //return PARENT2CHILD+url.getHost();
        return getWebGraphName_parent2child(ID_Mapping.PageID2URL(docID));
    }
    public static String getWebGraphName_child2parent(URL url){
        //return CHILD2PARENT+url.getHost();
        return CHILD2PARENT;
    }
    public static String getWebGraphName_child2parent(String docID) throws MalformedURLException {
        return getWebGraphName_child2parent(ID_Mapping.PageID2URL(docID));
    }

    public static String getDocRecordsName(URL url){
        //return DOCRECORDS+url.getHost();
        return DOCRECORDS;
    }

    public static String getDocRecordsName(String urlID) throws MalformedURLException{
        return getDocRecordsName(ID_Mapping.PageID2URL(urlID));
    }
}
