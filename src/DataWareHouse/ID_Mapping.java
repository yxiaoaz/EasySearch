package DataWareHouse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class ID_Mapping {
    public static String URL2ID(URL url){
        String encodedURL = Base64.getUrlEncoder().encodeToString(url.toString().getBytes());
        return encodedURL;
    }
    public static URL PageID2URL(String ID) throws MalformedURLException {
        byte[] actualByte = Base64.getUrlDecoder().decode(ID);
        URL actualURL = new URL(new String(actualByte));
        return actualURL;
    }
    public static String Term2ID(String term){
        //TODO
        return Base64.getEncoder().encodeToString(term.getBytes());
    }
    public static String TermID2Term(String ID){
        //TODO
        return new String(Base64.getDecoder().decode(ID));
    }
}
