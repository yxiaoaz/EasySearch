package Spider;
import DataWareHouse.FileManager;
import DataWareHouse.Indexer;
import org.htmlparser.beans.LinkBean;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


/**Abstraction of a one-off crawling event
 * For every single run, we need to know:
 *      1. What is the root set?
 *      2. How many crawlers to operate?
 *      3. What indexers to use? -> where to store and fetch the data?
 * */
public class CrawlingEvent {
    static AbstractQueue<URL> unfetchedURL;
    static ConcurrentHashMap.KeySetView<URL,Boolean> fetchedURL;
    private FileManager fileManager;
    public CrawlingEvent(ArrayList<URL> root, FileManager savingLocation){
        unfetchedURL = new ConcurrentLinkedQueue<URL>();
        unfetchedURL.addAll(root);
        fetchedURL = ConcurrentHashMap.newKeySet();
        fileManager = savingLocation;
    }

    private class Crawler implements Runnable{
        Indexer indexer;
        public Crawler() throws IOException {
            indexer = new Indexer(fileManager);
        }
        public void run(){
            while(!unfetchedURL.isEmpty()){
                URL url = unfetchedURL.poll();

                LinkBean lb = new LinkBean();
                lb.setURL(url.toString());
                URL[] URL_array = lb.getLinks();
                for(URL u:URL_array){
                    if(fetchedURL.add(u)){
                        /**
                         * NOT visited before
                         * */
                        unfetchedURL.add(u);
                    }
                }

                /**
                 * TODO: crawl text and other information from url
                 * */

            }
        }

    }
}
