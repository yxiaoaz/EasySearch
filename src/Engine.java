import DataWareHouse.*;
import Spider.CrawlingEvent;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import java.lang.Math;

public class Engine {
    FileManager manager;
    public Engine(){
        manager = new FileManager();

    }
    public  void printInfo(DocProfile docprofile) throws IOException {
        URL url = new URL(docprofile.getURLinString());
        HTree parent2childGraph = manager.getIndexFile(FileNameGenerator.getWebGraphName_parent2child(url)).getFile();
        ArrayList<String> childlist = (ArrayList<String>) parent2childGraph.get(docprofile.getID());
        HTree forwardindex = manager.getIndexFile(FileNameGenerator.getForwardIndexFileName(url)).getFile();
        ArrayList<FIPosting> keywords = (ArrayList<FIPosting>) forwardindex.get(docprofile.getID());

        System.out.println("-----------------------------------------------------");
        System.out.println("Page Title: "+docprofile.getTitle());
        System.out.println("URL: "+docprofile.getURLinString());
        System.out.println("Last modification date: "+docprofile.getLastModified().toString());
        System.out.println("Size of page: "+docprofile.getSize());
        System.out.println("Keywords frequency: ");
        for(int i=0;i<Math.min(10,keywords.size());i++){
            System.out.println("---- Keyword "+(i+1)+"  "+ID_Mapping.TermID2Term(keywords.get(i).getID())+" [freq: "+keywords.get(i).getOccurence()+"]");
        }
        System.out.println("Child links: ");
        for(int i=0;i<Math.min(10,childlist.size());i++){
            System.out.println("---- Child Link "+(i+1)+" : "+ID_Mapping.PageID2URL(childlist.get(i)).toString());
        }
        System.out.println("-----------------------------------------------------");

    }
    public void Phase1(){

        try {
            /**
             * For simplicity, in this phase there is only one document for each type of file
             * */
            manager.createIndexFile(FileNameGenerator.CHILD2PARENT);
            manager.createIndexFile(FileNameGenerator.PARENT2CHILD);
            manager.createIndexFile(FileNameGenerator.DOCRECORDS);
            manager.createIndexFile(FileNameGenerator.FORWARDINDEX);
            manager.createIndexFile(FileNameGenerator.INVERTEDINDEX);

            ArrayList<URL> root = new ArrayList<>();
            root.add(new URL("http://www.cse.ust.hk"));
            CrawlingEvent phase1 = new CrawlingEvent(root,manager,1,new AtomicInteger(30));
            phase1.Start();
            for(IndexFile f:manager.getAllIndexFiles()){
                f.saveChanges();
            }

            HTree docrecord = manager.getIndexFile(FileNameGenerator.DOCRECORDS).getFile();
            FastIterator docrecorditerator = docrecord.values();
            int count = 1;
            DocProfile element = (DocProfile) docrecorditerator.next();
            while(count<30 || element!=null){
                printInfo(element);
                element = (DocProfile) docrecorditerator.next();
                count++;
            }
            for(IndexFile f:manager.getAllIndexFiles()){
                f.saveChanges();
                f.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args){
        Engine e = new Engine();
        e.Phase1();
    }
}
