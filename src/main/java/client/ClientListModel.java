package client;

import java.io.File;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *Class create list of files in client ftp gui
 * 
 * @author Pawel Jaroch
 * @version 1.0
 */
public class ClientListModel implements ListModel {
    public String currentDirectory = null;
    
    public ClientListModel(String dir){
        currentDirectory = dir;
    }

    /**
     * Get size of file list
     * 
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        File d = new File(currentDirectory);
        return d.listFiles().length;
    }

    /**
     * Get concrete file in the list of files
     * 
     * {@inheritDoc}
     */
    @Override
    public Object getElementAt(int index) {
        File d = new File(currentDirectory);
        String[] filesInDir = d.list();
        return new File(d,filesInDir[index]){
            @Override
            public String toString(){
                return this.getName();
            }
        };
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }
    
}