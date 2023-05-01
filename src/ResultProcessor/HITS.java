package ResultProcessor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import DataWareHouse.FileManager;
import DataWareHouse.FileNameGenerator;
import DataWareHouse.ID_Mapping;
import jdbm.htree.HTree;

public class HITS {
    private FileManager fileManager;
    private HashMap<String, Double> Authority;
    private HashMap<String, Double> Hub;
    private ArrayList<String> subgraph;
    private int rootsetsize;
    public HITS(FileManager manager, ArrayList<String> root) throws IOException {
        fileManager = manager;
        Authority = new HashMap<>();
        Hub = new HashMap<>();
        subgraph = root;
        rootsetsize = subgraph.size();
    }

    /**Given a ranked result from VectorSpaceRanker, expand the root set by including their parents and children*/
    private void expandRootSet() throws IOException {
        for(int i=0;i< rootsetsize;i++)
        {
            String docID = subgraph.get(i);
            HTree parent2child = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_parent2child(docID)).getFile();
            HTree child2parent = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_child2parent(docID)).getFile();
            ArrayList<String> children = (ArrayList<String>)parent2child.get(docID);
            ArrayList<String> parents = (ArrayList<String>)child2parent.get(docID);
            //System.out.println(ID_Mapping.PageID2URL(docID));

            if(children!=null) {
                for (String child:children){
                    if(!subgraph.contains(child))
                        subgraph.add(child);
                }
            }
            if(parents!=null) {
                for (String parent : parents) {
                    if (!subgraph.contains(parent))
                        subgraph.add(parent);
                }
            }
        }
    }

    public HashMap<String,Double> rank(double threshold) throws IOException {
        expandRootSet();
        for(String docID: subgraph){
            Authority.put(docID, 1.0);
            Hub.put(docID, 1.0);
        }
        int i = 0;
        boolean converged = true;
        HashMap<String,Double> oldauth = new HashMap<>();
        HashMap<String,Double> oldhub = new HashMap<>();
        String experiment = ID_Mapping.URL2ID(new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/90.html"));
        do{
            converged = true;
            double authSum = 0, hubSum=0;
            for (String docID: subgraph){  /**Update authority of each doc*/
                oldauth.put(docID,Authority.get(docID));
                updateAuth(docID);
                authSum+=Authority.get(docID);
            }
            for (String docID: subgraph){ /** NORMALIZE authority of each doc*/

                if(docID.equals(experiment)){System.out.print("computed auth: " + Authority.get(docID) + "   ->>  ");}
                Authority.put(docID, Authority.get(docID) / authSum); //normalize
                if(docID.equals(experiment)){System.out.println("normalized auth: divide by " + authSum + " = " + Authority.get(docID));}

                if(Math.abs(Authority.get(docID)-oldauth.get(docID))>threshold) {
                    //System.out.println(Math.abs(Hub.get(docID)-oldhub));
                    converged = false;
                }
            }
            for(String docID:subgraph) { /**Update hub of each doc*/
                oldhub.put(docID, Hub.get(docID));
                updateHub(docID);
                hubSum += Hub.get(docID);
            }
            for(String docID:subgraph) {/** NORMALIZE hub of each doc*/
                if(docID.equals(experiment)){System.out.print("computed hub: "+Hub.get(docID)+"   ->>  ");}
                Hub.put(docID,Hub.get(docID)/hubSum);
                if(docID.equals(experiment)){System.out.println("normalized hub: divide by "+hubSum+" = "+Hub.get(docID));}

                if(Math.abs(Hub.get(docID)-oldhub.get(docID))>threshold){
                    //System.out.println(Math.abs(Hub.get(docID)-oldhub));
                    converged=false;
                }

            }
        }while(!converged);

        return Authority;
    }
    private void updateAuth(String docID) throws IOException {
        String experiment = ID_Mapping.URL2ID(new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm"));
        HTree child2parent = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_child2parent(docID)).getFile();
        ArrayList<String> parents = (ArrayList<String>)child2parent.get(docID);

        double newAuth = 0.0;
        if(parents==null) return;
        if(docID.equals(experiment)) {System.out.println("Updating AUTH");}
        for(String parent:parents){
            if(!subgraph.contains(parent)) continue;
            if(docID.equals(experiment)) {System.out.println("---Aggregate HUB value from parent: "+ID_Mapping.PageID2URL(parent));}
            newAuth+=Hub.get(parent);
            if(docID.equals(experiment)) {System.out.println("---Added by : "+Hub.get(parent)+" = "+newAuth);}
        }
        Authority.put(docID,newAuth);
    }

    private void updateHub(String docID) throws IOException {
        String experiment = ID_Mapping.URL2ID(new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/90.html"));
        HTree parent2child = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_parent2child(docID)).getFile();
        ArrayList<String> children = (ArrayList<String>)parent2child.get(docID);
        double newHub = 0.0;
        if(children==null) return;
        if(docID.equals(experiment)) {System.out.println("Updating HUB");}
        for(String child:children){
            if(!subgraph.contains(child)) continue;
            if(docID.equals(experiment)) {System.out.println("---Aggregate AUTH value from child: "+ID_Mapping.PageID2URL(child));}
            newHub+=Authority.get(child);
            if(docID.equals(experiment)) {System.out.println("---Added by : "+Authority.get(child)+" = "+newHub);}
        }
        Hub.put(docID,newHub);
    }
}
