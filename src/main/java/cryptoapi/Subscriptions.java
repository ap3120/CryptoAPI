package cryptoapi;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Path;

public class Subscriptions {

    public static String add(String chatId, String symbol, String type, String target) {
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

        if (isJson() && !isEmpty()) {
            JSONObject tmpJson = getJsonContent();
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
                            break;
                        }
                    }
                    tmpChatSubscriptions.add(chatSubscription);
                    writeJsonFile(tmpJson);
                    break;
                }
            }
            tmpData.add(chat);
            writeJsonFile(tmpJson);
        } else {
            writeJsonFile(json);
        }
        return "Your subscription was saved. You will get an alert when " + symbol + " reaches a " + type + " of $ " + target;
    }

    public static String remove(String chatId, String symbol, String type) {
        if (!isJson() || isEmpty()) return "You don't have any subscription.";
        JSONObject json = getJsonContent();
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
        return "Your subscription for /" + symbol + "/" + type + " has been removed.";
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

    public static JSONObject getJsonContent() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader("subscriptions.json"));
            String content;
            while ((content = bufferedReader.readLine()) != null) {
                stringBuilder.append(content);
            }
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(stringBuilder.toString());
            return jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEmpty() {
        JSONObject jsonObject = getJsonContent();
        JSONArray jsonArray = (JSONArray) jsonObject.get("data");
        if (jsonArray != null && !jsonArray.isEmpty()) return false;
        return true;
    }

    public static boolean isJson() {
        File file = new File("subscriptions.json");
        return file.isFile();
    }
}
