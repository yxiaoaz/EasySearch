package DataWareHouse;

import jdbm.htree.HTree;

import java.io.IOException;
import java.util.ArrayList;

public class Indexer {
    //The database the indexer is serving
    private IndexDatabase database;

    /**
     * Code of Conduct: Create database-> affiliate indexer to database
     * */
    public Indexer(IndexDatabase database){
        this.database = database;
    }

    //Add a posting for a key
    public boolean addPosting(String fileName, String key, Posting posting) throws IOException {
        HTree file = database.getFile(fileName);
        if(file==null)
            return false;

        ArrayList<Posting> list = (ArrayList<Posting>) file.get(key);
        if(list==null)
            list = new ArrayList<>();

        list.add(posting);
        file.put(key,list);
        return true;
    }

    public boolean deletePosting(String fileName, String key, Posting posting) throws IOException{
        HTree file = database.getFile(fileName);
        if(file==null)
            return false;

        ArrayList<Posting> list = (ArrayList<Posting>) file.get(key);
        if(list==null)
            return false; // no posting list at the first place.

        if(list.contains(posting)){
            list.remove(posting);
        }
        file.put(key,list);
        return true;
    }

    //delete the key together with the whole posting list
    public boolean deleteEntry(String fileName, String key) throws IOException {
        HTree file = database.getFile(fileName);
        if(file==null)
            return false;
        file.remove(key);
        return true;
    }

}
