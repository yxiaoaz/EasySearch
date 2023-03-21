package DataWareHouse;

import java.net.URL;
import java.util.Base64;

public class ID_Mapping {
    public static String URL2ID(URL url){
        String encodedURL = Base64.getUrlEncoder().encodeToString(url.toString().getBytes());
        return encodedURL;
    }
    public static String PageID2URL(String ID){
        byte[] actualByte = Base64.getUrlDecoder().decode(ID);
        String actualURLString = new String(actualByte);
        return actualURLString;
    }
    public static String Term2ID(String term){
        //TODO
        return "";
    }
    public static String TermID2Term(String ID){
        //TODO
        return "";
    }
}
