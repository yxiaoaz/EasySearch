import DataWareHouse.*;
import Spider.CrawlingEvent;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import java.lang.Math;

public class CrawlingManager {
    public FileManager manager;
    public CrawlingManager(FileManager manager){
       this.manager = manager;
    }
    public  void printInfo(DocProfile docprofile,FileWriter file) throws IOException {
        URL url = new URL(docprofile.getURLinString());
        HTree parent2childGraph = manager.getIndexFile(FileNameGenerator.getWebGraphName_parent2child(url)).getFile();
        ArrayList<String> childlist = (ArrayList<String>) parent2childGraph.get(docprofile.getID());
        HTree forwardindex = manager.getIndexFile(FileNameGenerator.getForwardIndexFileName(url)).getFile();
        ArrayList<FIPosting> keywords = (ArrayList<FIPosting>) forwardindex.get(docprofile.getID());

        System.out.println("-----------------------------------------------------");
        file.write("-----------------------------------------------------");
        file.write(System.getProperty("line.separator"));
        System.out.println("Page Title: "+docprofile.getTitle());
        file.write("Page Title: "+docprofile.getTitle());
        file.write(System.getProperty("line.separator"));
        System.out.println("URL: "+docprofile.getURLinString());
        file.write("URL: "+docprofile.getURLinString());
        file.write(System.getProperty("line.separator"));
        System.out.println("Last modification date: "+docprofile.getLastModified().toString());
        file.write("Last modification date: "+docprofile.getLastModified().toString());
        file.write(System.getProperty("line.separator"));
        System.out.println("Size of page: "+docprofile.getSize());
        file.write("Size of page: "+docprofile.getSize());
        file.write(System.getProperty("line.separator"));
        System.out.println("Keywords(stemmed) frequency: ");
        file.write("Keywords(stemmed) frequency: ");
        file.write(System.getProperty("line.separator"));
        for(int i=0;i<Math.min(10,keywords.size());i++){
            System.out.println("---- Keyword "+(i+1)+"  "+ID_Mapping.TermID2Term(keywords.get(i).getID())+" [freq: "+keywords.get(i).getOccurence()+"]");
            file.write("---- Keyword "+(i+1)+"  "+ID_Mapping.TermID2Term(keywords.get(i).getID())+" [freq: "+keywords.get(i).getOccurence()+"]");
            file.write(System.getProperty("line.separator"));
        }
        System.out.println("Child links: ");
        file.write("Child links: ");
        file.write(System.getProperty("line.separator"));
        for(int i=0;i<Math.min(10,childlist.size());i++){
            System.out.println("---- Child Link "+(i+1)+" : "+ID_Mapping.PageID2URL(childlist.get(i)).toString());
            file.write("---- Child Link "+(i+1)+" : "+ID_Mapping.PageID2URL(childlist.get(i)).toString());
            file.write(System.getProperty("line.separator"));
        }
        System.out.println("-----------------------------------------------------");
        file.write("-----------------------------------------------------");
        file.write(System.getProperty("line.separator"));

    }
    public void crawlOnce(){
        try {
            /**
             * For simplicity, in this phase there is only one document for each type of file
             * */
            /***manager.createIndexFile(FileNameGenerator.CHILD2PARENT);
            manager.createIndexFile(FileNameGenerator.PARENT2CHILD);
            manager.createIndexFile(FileNameGenerator.DOCRECORDS);
            manager.createIndexFile(FileNameGenerator.FORWARDINDEX);
            manager.createIndexFile(FileNameGenerator.INVERTEDINDEX);*/

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

            FileWriter file = new FileWriter("spider_result.txt");
            while(count<30 || element!=null){
                printInfo(element,file);
                element = (DocProfile) docrecorditerator.next();
                count++;
            }
            for(IndexFile f:manager.getAllIndexFiles()){
                f.saveChanges();
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }
}