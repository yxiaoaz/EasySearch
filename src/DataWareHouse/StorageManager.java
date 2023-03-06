package DataWareHouse;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import DataWareHouse.Posting;

import java.io.IOException;
import java.util.ArrayList;

public class StorageManager {

    private RecordManager recman;

    private HTree InvertedIndex;
    private HTree ForwardIndex;
    private HTree WebPageGraph;

    public StorageManager() throws IOException {
        recman = RecordManagerFactory.createRecordManager("Storage Manager");

        InvertedIndex = HTree.createInstance(recman);
        recman.setNamedObject( "InvertedIndex", InvertedIndex.getRecid() );

        ForwardIndex = HTree.createInstance(recman);
        recman.setNamedObject( "ForwardIndex", ForwardIndex.getRecid() );

        WebPageGraph = HTree.createInstance(recman);
        recman.setNamedObject( "WebPageGraph", WebPageGraph.getRecid() );
    }
    public void finalize() throws IOException {
        recman.commit();
        recman.close();
    }

    // Add a posting for a given key (word/doc)
    public void addPosting(HTree target, String key, Posting item) throws IOException{
        ArrayList<Posting> l;
        if (target.get(key)!=null)
            l = (ArrayList<Posting>) target.get(key);
        else
            l = new ArrayList<>();

        l.add(item);
        target.put(key,l);
    }

    // Delete the whole postings list of a given key (word/doc)
    public void delEntry(HTree target, String key) throws IOException{
        target.remove(key);
    }

}
