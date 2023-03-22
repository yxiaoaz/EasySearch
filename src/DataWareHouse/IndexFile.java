package DataWareHouse;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;


public class IndexFile {

    private RecordManager recman;
    private String name;
    private HTree file;
    private FileManager fileManager;
    /*
    *Create a database with specified record manager name
    *For simplicity, the Database shares the same name.
    **/
    public IndexFile(String name, FileManager fileManager) throws IOException {
        this.name = name;
        this.recman = RecordManagerFactory.createRecordManager(name);
        file = HTree.createInstance(recman);
        recman.setNamedObject( name, file.getRecid());
        this.fileManager = fileManager;
    }
    public String getName(){
        return name;
    }
    public HTree getFile(){
        return file;
    }


}
