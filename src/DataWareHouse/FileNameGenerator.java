package DataWareHouse;

import java.net.URL;

public class FileNameGenerator {
    public static final String INVERTEDINDEX = "Inverted-";
    public static final String FORWARDINDEX = "Forward-";
    public static final String PARENT2CHILD = "Web Graph Parent as key-";
    public static final String CHILD2PARENT = "Web Graph Child as key-";

    public static String getInvertedIndexFileName(String term){
        return INVERTEDINDEX+term.charAt(0);
    }

    public static String getForwardIndexFileName(URL url){
        return FORWARDINDEX;
    }

    public static String getWebGraphName_parent2child(URL url){
        return PARENT2CHILD+url.getHost();
    }

    public static String getWebGraphName_child2parent(URL url){
        return CHILD2PARENT+url.getHost();
    }

}
