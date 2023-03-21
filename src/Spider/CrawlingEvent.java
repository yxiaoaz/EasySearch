package Spider;
import DataWareHouse.Indexer;
import DataWareHouse.IndexDatabase;
import org.htmlparser.beans.LinkBean;

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
    private IndexDatabase database;
    public CrawlingEvent(ArrayList<URL> root, IndexDatabase savingLocation){
        unfetchedURL = new ConcurrentLinkedQueue<URL>();
        unfetchedURL.addAll(root);
        fetchedURL = ConcurrentHashMap.newKeySet();
        database = savingLocation;
    }

    private class Crawler implements Runnable{
        Indexer indexer;
        public Crawler(){
            indexer = new Indexer(database);
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
