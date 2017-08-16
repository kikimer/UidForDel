
package uidfordel;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
/**
 *
 * @author yurka
 */
public class UidForDel {

  
    public static void main(String[] args) {
        String Catalog;
        if(args.length==0) Catalog = System.getProperty("user.dir");
        else Catalog = args[0];

        //огроменное хранилище всех объектов. В перспрективе распределить его на несколько хранилищ по видам объектов для сужения "зоны поиска";
        Map<String,Uid> uids = new HashMap<>();
        
        //Динамически удаляемо(с конца)/пополняемый (с конца) контейнер для хранения ссылок на объекты, ставшие неудаляемыми.
        Deque<Uid> uids_CantDel = new ArrayDeque<>(); 
        
        //1. Загрузка всех объектво из Metadata
        try(RandomAccessFile Metadata = new RandomAccessFile(new File(Catalog+"/metadata.txt"),"r");){
            byte[] b = new byte[3];
            Metadata.read(b);
            if(b[0] != -17 || b[1] != -69 || b[2] != -65) { Metadata.seek(0);}

            String nextLine;
            while((nextLine = Metadata.readLine()) != null){
                String str_metadata = new String(nextLine.getBytes("ISO-8859-1"),"UTF-8");
                if(str_metadata.trim().charAt(0) == '#') continue;
                String[] fields_metadata = str_metadata.split(";");
                String filename = fields_metadata[3];

                //объекты 1С
                System.out.println("загружается: "+filename);
                try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Catalog+"/"+filename),"UTF-8"))){
                    for(String str = br.readLine();str != null; str = br.readLine()){
                        if(str.startsWith("\ufeff")) str = str.substring(1);
                        if(str.trim().charAt(0) == '#') continue;

                        String[] fields = str.split(";");
                        String UID = fields[0];

                        boolean markDel = fields[1].equals("true");
                        Uid objUid = new Uid(UID, markDel);
                        uids.put(UID, objUid);

                        if(!markDel) uids_CantDel.addLast(objUid);
                    }
                }

            }

            //2. Распределение всех ссылок по объектам
            Metadata.seek(0);
            Metadata.read(b);
            if(b[0] != -17 || b[1] != -69 || b[2] != -65) { Metadata.seek(0);}
            while((nextLine = Metadata.readLine()) != null){
                String str_metadata = new String(nextLine.getBytes("ISO-8859-1"),"UTF-8");
                if(str_metadata.trim().charAt(0) == '#') continue;
                String[] fields_metadata = str_metadata.split(";");
                String filename = fields_metadata[3];

                //объекты 1С
                System.out.println("распределяются ссылки из: "+filename);
                try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Catalog+"/"+filename),"UTF-8"))){
                    for(String str = br.readLine();str != null; str = br.readLine()) {
                        if(str.startsWith("\ufeff")) str = str.substring(1);
                        if(str.trim().charAt(0) == '#') continue;
                            String[] fields = str.split(";");
                            Uid objUid = uids.get(fields[0]);
                            if (fields.length >= 3) {
                                String[] fieldsReferences = fields[2].split(",");
                                for (String Ref : fieldsReferences)
                                    objUid.addReference(uids.get(Ref));
                            }

                        if (fields.length >= 4) {
                            String[] fieldsOutReference = fields[3].split(",");
                            for (String sRef : fieldsOutReference) {
                                Uid OutUID = uids.get(sRef);
                                OutUID.addReference(objUid);
                            }
                        }
                    }

                }
            }


        }catch(Exception ex){
            ex.printStackTrace();
            return;
        }

        //Пометка ссылок, хранящихся в неудаляемых объектах как НЕУДАЛЯЕМЫЕ
        
        while(!uids_CantDel.isEmpty()){
            Uid objUid = uids_CantDel.pollLast();
            Iterator<Uid> i = objUid.getReferenceIterator();
            while(i.hasNext()){
                Uid objUidRef = i.next();
                if (!objUidRef.getCanDel()) continue;//Отсеиваем обработанные или неудаляемые. Их значение не меняется
                objUidRef.setCanDel(false);
                uids_CantDel.addLast(objUidRef);
            } 
            
        }
        
        try(PrintWriter result = new PrintWriter(new OutputStreamWriter(new FileOutputStream(Catalog+"/result.txt"),"UTF-8"))){
            result.print("# УИД удаляемого");
            result.print("\r\n");
            for(Map.Entry<String, Uid> e : uids.entrySet()){
                Uid objUid = e.getValue();
                if(objUid.getCanDel()) {
                    
                    result.print(objUid.toString());
                    result.print("\r\n");
                }
            }
        }catch(Exception ex){
            System.out.println("File open error:"+ex);
        }
    }
    
}