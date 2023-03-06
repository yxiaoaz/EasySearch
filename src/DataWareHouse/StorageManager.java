package DataWareHouse;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.io.IOException;

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
}
