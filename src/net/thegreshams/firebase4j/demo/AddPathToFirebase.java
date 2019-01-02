package net.thegreshams.firebase4j.demo;

import java.io.*;
import java.util.*;

import com.sun.javafx.collections.MappingChange;
import net.thegreshams.firebase4j.error.FirebaseException;
import net.thegreshams.firebase4j.error.JacksonUtilityException;
import net.thegreshams.firebase4j.model.FirebaseResponse;
import net.thegreshams.firebase4j.service.Firebase;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;



public class AddPathToFirebase {
    public static class addCategoryRun implements Runnable{
        Map<String, Object> nameMap;
        String newCategory;
        addCategoryRun(Map<String, Object> nameMap, String newCategory){
            this.nameMap = nameMap;
            this.newCategory = newCategory;
        }
        @Override
        public void run() {
            try {
                Firebase firebase;
                FirebaseResponse response;
                int currentNumber = -1;
                String firebase_baseUrl_main = "https://*...*//Users//";   //!!!
                for (Object name : nameMap.keySet()) {
                    if ((name instanceof String) && (nameMap.get(name) instanceof Map)) {
                        Map categories = (Map) ((Map) nameMap.get(name)).get("Categories_seen");
                        String preferences = ((Map) nameMap.get(name)).get("preferences").toString();
                        if ((categories.size() != 0) && (!categories.containsKey(newCategory))) {
                            String firebase_baseUrl = firebase_baseUrl_main + name + "//Categories_seen";
                            firebase = new Firebase(firebase_baseUrl);
                            categories.put(newCategory, 1);
                            response = firebase.put(categories);
                            int counter = 0;
                            Map<String, Object> treeMap = new TreeMap<String, Object>(categories);
                            for (Object current : treeMap.keySet()) {
                                if (((String) current).equalsIgnoreCase(newCategory)) {
                                    currentNumber = counter;
                                    break;
                                }
                                counter++;
                            }
                        }
                        if ((preferences != null) && (currentNumber != -1)) {
                            String firebase_baseUrl = firebase_baseUrl_main + name + "//preferences";
                            firebase = new Firebase(firebase_baseUrl);
                            preferences = preferences.substring(1, preferences.length() - 1);
                            String[] arrayPreferences = preferences.split(",");
                            StringBuilder rezult = new StringBuilder();
                            for (int i = 0; i < arrayPreferences.length; i++) {
                                if (i != currentNumber) {
                                    rezult.append(arrayPreferences[i]);
                                    rezult.append(",");
                                } else {
                                    if (i != 0) {
                                        rezult.append(" 0,");
                                    } else {
                                        rezult.append("0, ");
                                    }
                                    if (currentNumber != arrayPreferences.length + 1) {
                                        rezult.append(arrayPreferences[i]);
                                        rezult.append(",");
                                    }
                                }
                            }
                            if (currentNumber == arrayPreferences.length) {
                                rezult.append(" 0,");
                            }
                            preferences = "\"[" + rezult.substring(0, rezult.length() - 1) + "]\"";
                            response = firebase.put(preferences);
                        }
                    }
                }
            } catch (IOException ex){
                System.out.println(ex);
            }
            catch (FirebaseException ex){
                System.out.println(ex);
            }
            catch (JacksonUtilityException ex){
                System.out.println(ex);
            }
        }
    }

    public static class delCategoryRun implements Runnable{
        Map<String, Object> nameMap;
        String delCategory;
        delCategoryRun(Map<String, Object> nameMap, String delCategory){
            this.nameMap = nameMap;
            this.delCategory = delCategory;
        }
        @Override
        public void run() {
            try {
                FirebaseResponse response;
                int currentNumber = -1;
                Firebase firebase;
                String firebase_baseUrl_main = "https://*...*//Users//";    //!!!
                for (Object name : nameMap.keySet()) {
                    if ((name instanceof String) && (nameMap.get(name) instanceof Map)) {
                        Map categories = (Map) ((Map) nameMap.get(name)).get("Categories_seen");
                        String preferences = ((Map) nameMap.get(name)).get("preferences").toString();
                        if ((categories.size() != 0) && (categories.containsKey(delCategory))) {
                            String firebase_baseUrl = firebase_baseUrl_main + name + "//Categories_seen";
                            firebase = new Firebase(firebase_baseUrl);
                            int counter = 0;
                            Map<String, Object> treeMap = new TreeMap<String, Object>(categories);
                            for (Object current: treeMap.keySet()) {
                                if (((String)current).equalsIgnoreCase(delCategory)) {
                                    currentNumber = counter;
                                    break;
                                }
                                counter++;
                            }
                            categories.remove(delCategory);
                            response = firebase.put(categories);
                        }
                        if ((preferences != null)&&(currentNumber != -1)) {
                            String firebase_baseUrl = firebase_baseUrl_main + name + "//preferences";
                            firebase = new Firebase(firebase_baseUrl);
                            preferences = preferences.substring(1, preferences.length()-1);
                            String[] arrayPreferences = preferences.split(",");
                            StringBuilder rezult = new StringBuilder();
                            for (int i = 0; i < arrayPreferences.length; i++){
                                if (i != currentNumber) {
                                    rezult.append(arrayPreferences[i]);
                                    rezult.append(",");
                                }
                            }
                            if (currentNumber == 0){
                                preferences ="\"[" + rezult.substring(1, rezult.length()-1) + "]\"";
                            } else {
                                preferences = "\"[" + rezult.substring(0, rezult.length() - 1) + "]\"";
                            }
                            response = firebase.put(preferences);
                        }
                    }
                }

            } catch (IOException ex){
                System.out.println(ex);
            }
            catch (FirebaseException ex){
                System.out.println(ex);
            }
            catch (JacksonUtilityException ex){
                System.out.println(ex);
            }
        }
    }

    //method to download in category.
    public static String pushMemes(Firebase firebase, BufferedReader in, String namecategory)
            throws FirebaseException, IOException {
        //download Map.
        FirebaseResponse response = firebase.get(namecategory);
        Map dataMap = response.getBody();
        //read File and add to Map.
        int number = dataMap.size();
        String input = in.readLine();
        while (input != null) {
            dataMap.put("m" + number, input);
            number++;
            input = in.readLine();
        }
        response = firebase.put(namecategory, new ObjectMapper().writeValueAsString(dataMap));
        return ("\n\nResult of GET (for the PUT):\n" + response);
    }
    //add new category to all users/
    public static void addCategoryToUsers(Firebase firebase, String newCategory)
            throws FirebaseException, IOException, JacksonUtilityException, InterruptedException {
        FirebaseResponse response = firebase.get();
        Map usersMap = response.getBody();
        int counter = 0;
        Map[] nameMap = new Map[4];
        for (int i = 0; i < 4; i++) {
            nameMap[i] = new HashMap();
        }
        for (Object user : usersMap.keySet()) {
            if ((user instanceof String) && (usersMap.get(user) instanceof Map)) {
                nameMap[counter % 4].put(user, usersMap.get(user));
            }
            counter++;
        }
        Thread[] threads = new Thread[4];
        for (int i = 0; i < 4; i++) {
            threads[i] = new Thread(new addCategoryRun(nameMap[i], newCategory));
            threads[i].start();
        }
        for (int i = 0; i < 4; i++) {
            threads[i].join();
        }
    }

    //delete category
    public static void deleteCategoryToUsers(Firebase firebase, String delCategory)
            throws FirebaseException, IOException, JacksonUtilityException, InterruptedException {
        FirebaseResponse response = firebase.get();
        Map usersMap = response.getBody();
        int counter = 0;
        Map[] nameMap = new Map[4];
        for (int i = 0; i < 4; i++) {
            nameMap[i] = new HashMap();
        }
        for (Object user : usersMap.keySet()) {
            if ((user instanceof String) && (usersMap.get(user) instanceof Map)) {
                nameMap[counter % 4].put(user, usersMap.get(user));
            }
            counter++;
        }
        Thread[] threads = new Thread[4];
        for (int i = 0; i < 4; i++) {
            threads[i] = new Thread(new delCategoryRun(nameMap[i], delCategory));
            threads[i].start();
        }
        for (int i = 0; i < 4; i++) {
            threads[i].join();
        }
    }

    public static void main(String[] args) throws Exception, JacksonUtilityException, FirebaseException {
        System.out.println("Выберите действие:\n0: выход;\n1: загрузка объектов в firebase;\n"
                + "2: добавление новой категории всем пользователям;\n3: удаление существующей категории у всех пользователей.");
        int count = 1;
        Scanner input = new Scanner(System.in);
        while (count != 0) {
            count = input.nextInt();
            switch (count) {
                case 1:
                    // get the base-url (ie: 'http://gamma.firebase.com/username')
                    String firebase_baseUrl = "https://*...*//Users";    //!!!
                    // create the firebase
                    Firebase firebase = new Firebase(firebase_baseUrl);
                    System.out.println("Введите имя файла и категории.");
                    String filename = input.next();
                    String namecategory = input.next();
                    File pathtomemes = new File("C:/Users/Александр/Desktop/Всякое/memder/" + filename + ".txt");
                    BufferedReader in = new BufferedReader(new FileReader(pathtomemes));
                    pushMemes(firebase, in, namecategory);
                    System.out.println("Готово.");
                    break;
                case 2:
                    // get the base-url (ie: 'http://gamma.firebase.com/username')
                    firebase_baseUrl = "https://*...*//Users";    //!!!
                    // create the firebase
                    firebase = new Firebase(firebase_baseUrl);
                    System.out.println("Введите имя добавляемой категории.");
                    namecategory = input.next();
                    addCategoryToUsers(firebase, namecategory);
                    System.out.println("Готово.");
                    break;
                case 3:
                    // get the base-url (ie: 'http://gamma.firebase.com/username')
                    firebase_baseUrl = "https://*...*//Users";   //!!!
                    // create the firebase
                    firebase = new Firebase(firebase_baseUrl);
                    System.out.println("Введите имя удаляемой категории.");
                    namecategory = input.next();
                    deleteCategoryToUsers(firebase, namecategory);
                    System.out.println("Готово.");
                    break;
                default:
                    count = 0;
                    System.out.println("Завершение работы.");
                    break;
            }
        }
    }
}




