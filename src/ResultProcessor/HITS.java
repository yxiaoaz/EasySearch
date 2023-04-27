package ResultProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import DataWareHouse.FileManager;
import DataWareHouse.FileNameGenerator;
import jdbm.htree.HTree;

public class HITS {
    private FileManager fileManager;
    private HashMap<String, Double> Authority;
    private HashMap<String, Double> Hub;
    private ArrayList<String> subgraph;
    public HITS(FileManager manager, ArrayList<String> root) throws IOException {
        fileManager = manager;
        Authority = new HashMap<>();
        Hub = new HashMap<>();
        subgraph = root;
        expandRootSet();
        for(String docID: subgraph){
            Authority.put(docID, 1.0);
            Hub.put(docID, 1.0);
        }
    }

    /**Given a ranked result from VectorSpaceRanker, expand the root set by including their parents and children*/
    private void expandRootSet() throws IOException {
        for(String docID: subgraph){
            HTree parent2child = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_parent2child(docID)).getFile();
            HTree child2parent = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_child2parent(docID)).getFile();
            ArrayList<String> children = (ArrayList<String>)parent2child.get(docID);
            ArrayList<String> parents = (ArrayList<String>)child2parent.get(docID);

            for (String child:children){
                if(!subgraph.contains(child))
                    subgraph.add(child);
            }
            for (String parent:parents){
                if(!subgraph.contains(parent))
                    subgraph.add(parent);
            }
        }
    }

    public void rank(double threshold){
        double delta=100;
        while(delta>=threshold){
            for (String docID: subgraph){
                updateAuth(docID);
            }
            for(String docID:subgraph){
                updateHub(docID);
            }
        }
    }
    private void updateAuth(String docID){

    }

    private void updateHub(String docID){

    }





}
