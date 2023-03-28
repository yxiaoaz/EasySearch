package Spider;
import DataWareHouse.FileManager;
import DataWareHouse.Indexer;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


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
    private int numCrawler;
    static AtomicInteger maxPageToCrawl;
    public CrawlingEvent(ArrayList<URL> root, FileManager fileManager,int numCrawler, AtomicInteger maxPageToCrawl){
        unfetchedURL = new ConcurrentLinkedQueue<URL>();
        unfetchedURL.addAll(root);
        fetchedURL = ConcurrentHashMap.newKeySet();
        this.fileManager = fileManager;
        this.numCrawler = numCrawler;
        this.maxPageToCrawl = maxPageToCrawl;
    }
    public void Start() throws IOException {
        ArrayList<Thread> threadpool=new ArrayList<>();
        for (int i=0;i<numCrawler;i++){
            Thread c = new Thread(new Crawler());
            threadpool.add(c);
            c.start();
        }
        while(stillRunning(threadpool)){}


    }

    private boolean stillRunning(ArrayList<Thread> threadpool){
        for(Thread t:threadpool){
            if(t.isAlive()){
                return true;
            }
        }
        return false;
    }
    /**JDBM is THREAD SAFE*/
    private class Crawler implements Runnable{
        Indexer indexer;
        public Crawler() throws IOException {
            indexer = new Indexer(fileManager);
        }
        @Override
        public void run(){
            while(!unfetchedURL.isEmpty() && maxPageToCrawl.get()>0){
                URL url = unfetchedURL.poll();
                //System.out.println("Current URL: "+url.toString());
                try {
                    if(indexer.validURL(url)){
                        ArrayList<URL> URL_array =  indexer.extractLinks(url);
                        for(URL u:URL_array){
                            if(fetchedURL.add(u)){
                                unfetchedURL.add(u);
                            }
                        }
                        indexer.processContent(url);
                        maxPageToCrawl.decrementAndGet();

                    }
                } catch (IOException e) {
                    continue;
                }


            }
        }

    }
}
