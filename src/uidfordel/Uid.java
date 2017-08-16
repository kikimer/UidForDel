/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uidfordel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;

/**
 *
 * @author yurka
 */
public class Uid implements Comparable<Uid>{
    private String UID;
    private boolean canDel=false;
    private Queue<Uid> references;       //ссылки, хранящиеся в объекте

    Uid(String _UID, boolean _canDel){
        UID = _UID;
        canDel = _canDel;
        references = new LinkedList<>();
        
    }
    
    public boolean getCanDel(){ return canDel;}
    public void setCanDel(boolean _canDel){canDel = _canDel;}
    public void addReference(Uid depUid){
        if (depUid==null) {
            throw new RuntimeException("ref error: "+UID);
        }
        references.add(depUid);
    }
    public String getUID() {return UID;}
    public Iterator<Uid> getReferenceIterator(){ return references.iterator();}

    @Override
    public String toString() {
        return UID;
    }
    
    @Override
    public int compareTo(Uid o) {
        return UID.compareTo(o.UID);
    }
    
    
    
}
