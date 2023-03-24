import DataWareHouse.*;
import Spider.CrawlingEvent;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Engine {


    public static void main(String[] args){
        FileManager manager = new FileManager();
        try {
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
                //f.close();
            }

            HTree docrecord = manager.getIndexFile(FileNameGenerator.DOCRECORDS).getFile();
            FastIterator docrecorditerator = docrecord.values();
            DocProfile element = (DocProfile) docrecorditerator.next();
            while(element!=null){
                System.out.println("---------");
                System.out.println(element.getURLinString());
                System.out.println("----------");
                element = (DocProfile) docrecorditerator.next();
            }
            for(IndexFile f:manager.getAllIndexFiles()){
                f.saveChanges();
                f.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
