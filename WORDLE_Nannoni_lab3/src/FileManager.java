import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.io.BufferedReader;
import java.io.IOException;


/**
 * @author fedenanno
 * Questo file è di proprietà di: fedenanno, ogni suo utilizzo va 
 * concordato con l'autore.
 * Creato in data: 
 * 
 */

public class FileManager {

    //costruttore vuoto
    FileManager(){
        
    }

    /*
    metodo getUserList(string filename) che apre un file json accettando il parametro come stringa utilizando la libreria gson
    il file ha il seguente formato 
    {
        "user": 
        [
            {
                "username" : "<string>",
                "password" : "<string>",
                "wins" : "<int>",
                "lastStreak" : "<int>",
                "bestStreak" : "<int>"
            }
        ]
    }
    per ogni elemento dell'array viene creato un oggetto User tramite il suo costruttore e viene aggiunto alla lista userList
    */
    public ConcurrentHashMap<String, User> getUserList(String filePath) throws Exception {
        Gson gson = new Gson();
        FileReader fileReader = new FileReader(filePath);

        Type userListType = new TypeToken<List<User>>(){}.getType();
        
        List<User> userList = gson.fromJson(fileReader, userListType);

        fileReader.close();

        ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();
        
        for (User user : userList) {
            userMap.put(user.getUsername(), user);
        }

        //stampa i dati del hash map
        // for (String key : userMap.keySet()) {
        //     System.out.println("Username: " + key + " Password: " + userMap.get(key).getPassword());
        // }

        return userMap;
    }
    
    //salva gli utenti all'interno del hashMap in un file json con la stessa struttura di quando era stato letto
    public void saveUser(ConcurrentHashMap<String, User> userMap, String filePath) throws Exception {
        Gson gson = new Gson();

        try(FileWriter fileWriter = new FileWriter(filePath)){
            gson.toJson(userMap.values(), fileWriter);
        }
        catch(Exception e){
            System.err.println("Errore salvataggio utenti");
        }
    }
    
    public static ConcurrentHashMap<String, String> getWord(String filePath) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                map.put(word, word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return map;
    }
    
    
    public static void main(String[] args){

        //stampa la directory attuale
        System.out.println(System.getProperty("user.dir"));
        
        
        String path = "../file/user.json";
        ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();
        //prova ad aprire un file json usando il metodo getUserList
        try {
            FileManager fm = new FileManager();
            userMap = fm.getUserList(path);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        try {
            FileManager fm = new FileManager();
            fm.saveUser(userMap, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String wordPath = "../file/words.txt";
        ConcurrentHashMap<String, String> resultMap = getWord(wordPath);
        
        // Esempio di stampa delle parole e dei relativi valori dalla ConcurrentHashMap
        for (String word : resultMap.keySet()) {
            System.out.println("Parola: |" + word + "|, Valore: " + resultMap.get(word));
        }
    }

}




/*
 * 
 * JsonReader reader = new JsonReader(new java.io.FileReader(filename));
        userList = new ArrayList<User>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("user")) {
                //inizia l'array contenente i dati dell'utente.
                reader.beginArray();
                while (reader.hasNext()) {
                    //inizia l'oggetto contentente i dati dell'utetne
                    reader.beginObject();
                    String nome = reader.nextName();
                    String cognome = reader.nextInt();
                    Integer wins = reader.nextInt();
                    Integer lastStreak = reader.nextInt();
                    Integer bestStreak = reader.nextInt();
                    
                    System.out.println("Nome: "+ nome);
                    reader.endObject();
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return userList;
 */