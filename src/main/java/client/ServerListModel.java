package client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *Class create list of files in server ftp gui
 *
 * @author Pawel Jaroch
 * @version 1.0
 */
public class ServerListModel implements ListModel {
    protected String currentDirectory = System.getProperty("user.dir");
    protected String list;
    protected Client client;
    
    public ServerListModel(String dir, Client cli){
        currentDirectory = dir;
        client = cli;
        try {
            client.changeDirectory(dir);
            list = client.listToString();
            //client.changeDirectory("/");
        } catch (IOException ex) {
            Logger.getLogger(ServerListModel.class.getName()).log(Level.SEVERE, "list error", ex);
        }
        System.out.println(list);
    }

    @Override
    public int getSize() {
        if(list.equals("empty")) return 1;
        String lines[] = list.split("\\r?\\n");
        return lines.length;
    }

    @Override
    public Object getElementAt(int index) {
        if(list.equals("empty")) return new ServerFile("..", true);
        String lines[] = list.split("\\r?\\n");
        String line[] = lines[index].split("\\s+");
        String filename = line[line.length-1];
        String isDir = line[0];
        boolean dir = false;
        if(isDir.equals("d")) dir = true;
        
        return new ServerFile(filename,dir);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }
    
}
