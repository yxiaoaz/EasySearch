import DataWareHouse.FileManager;
import DataWareHouse.FileNameGenerator;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        FileManager manager = new FileManager();
        CrawlingManager cm = new CrawlingManager(manager);
        cm.crawlOnce();

    }
}
