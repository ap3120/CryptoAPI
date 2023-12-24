package cryptoapi;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Path;

public class Subscriptions {

    public static void add(String chatId, String symbol, String type, String target) {
        JSONObject json = new JSONObject();
        JSONArray data = new JSONArray();
        JSONObject chat = new JSONObject();
        JSONArray chatSubscriptions = new JSONArray();
        JSONObject chatSubscription = new JSONObject();
        JSONObject usd = new JSONObject();

        usd.put(type, target);
        chatSubscription.put("symbol", symbol);
        chatSubscription.put("USD", usd);
        chatSubscriptions.add(chatSubscription);
        chat.put("chatId", chatId);
        chat.put("subscriptions", chatSubscriptions);
        data.add(chat);
        json.put("data", data);

        try {
            if (isJson() && ! isEmpty()) {
                String str = getJsonContent();
                JSONParser parser = new JSONParser();
                JSONObject tmpJson = (JSONObject) parser.parse(str);
                JSONArray tmpData = (JSONArray) tmpJson.get("data");
                for (Object o : tmpData.toArray()) {
                    JSONObject tmpChat = (JSONObject) o;
                    if (tmpChat.get("chatId").equals(chatId)) {
                        JSONArray tmpChatSubscriptions = (JSONArray) tmpChat.get("subscriptions");
                        for (Object s : tmpChatSubscriptions.toArray()) {
                            JSONObject tmpChatSubscription = (JSONObject) s;
                            if (tmpChatSubscription.get("symbol").equals(symbol)) {
                                JSONObject tmpUsd = (JSONObject) tmpChatSubscription.get("USD");
                                tmpUsd.put(type, target);
                                writeJsonFile(tmpJson);
                                return;
                            }
                        }
                        tmpChatSubscriptions.add(chatSubscription);
                        writeJsonFile(tmpJson);
                        return;
                    }
                }
                tmpData.add(chat);
                writeJsonFile(tmpJson);
            } else {
                writeJsonFile(json);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void remove(String chatId, String symbol, String type) {
        if (! isJson() || isEmpty()) return;
        String str = getJsonContent();
        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject) parser.parse(str);
            JSONArray data = (JSONArray) json.get("data");
            for (Object o : data.toArray()) {
                JSONObject chat = (JSONObject) o;
                if (chat.get("chatId").equals(chatId)) {
                    JSONArray chatSubscriptions = (JSONArray) chat.get("subscriptions");
                    for (Object s : chatSubscriptions.toArray()) {
                        JSONObject chatSubscription = (JSONObject) s;
                        if (chatSubscription.get("symbol").equals(symbol)) {
                            JSONObject usd = (JSONObject) chatSubscription.get("USD");
                            usd.remove(type);
                            if (usd.isEmpty()) chatSubscriptions.remove(chatSubscription);
                        }
                    }
                    if (chatSubscriptions.size() == 0) {
                        data.remove(chat);
                    }
                }
            }
            writeJsonFile(json);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJsonFile(JSONObject json) {
        String str = json.toJSONString();
        try {
            FileWriter fileWriter = new FileWriter("subscriptions.json");
            fileWriter.write(str);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJsonContent() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader("subscriptions.json"));
            String content;
            while ((content = bufferedReader.readLine()) != null) {
                stringBuilder.append(content);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isEmpty() {
        try {
            String str = getJsonContent();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(str);
            JSONArray jsonArray = (JSONArray) jsonObject.get("data");
            if (jsonArray.size() > 0) return false;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static boolean isJson() {
        File file = new File("subscriptions.json");
        return file.isFile();
    }
}
