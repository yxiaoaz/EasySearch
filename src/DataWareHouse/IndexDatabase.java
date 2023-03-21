package DataWareHouse;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.recman.BaseRecordManager;

import java.io.IOException;
import java.util.ArrayList;

public class IndexDatabase {

    private RecordManager recman;
    private String name;
    /*
    *Create a database with specified record manager name
    *For simplicity, the Database shares the same name.
    **/
    public IndexDatabase(String managerName) throws IOException {
        this.recman = RecordManagerFactory.createRecordManager(managerName);
        name = managerName;
    }

    public boolean createNewFile(String fileName) throws IOException {
        long recid = recman.getNamedObject(fileName);
        if(recid!=0){
            return false;
        }
        HTree newFile = HTree.createInstance(recman);
        recman.setNamedObject( fileName, newFile.getRecid());
        return true;
    }

    public HTree getFile(String fileName) throws IOException {
        long recid = recman.getNamedObject(fileName);

        // load the HTree if found, otherwise return null
        // checked needs to be made upon loading
        if (recid != 0)
            return HTree.load(recman, recid);
        return null;
    }


}
