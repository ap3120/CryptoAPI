package cryptoapi;

import io.github.cdimascio.dotenv.Dotenv;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Bot extends TelegramLongPollingBot {

    private static Dotenv dotenv = Dotenv.load();
    private static String BOT_TOKEN = dotenv.get("TELEGRAM_BOT_API");
    private static String BOT_USERNAME = dotenv.get("TELEGRAM_BOT_USERNAME");
    private static HashMap<String, Chat> chatsState = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();
            message.enableHtml(true);
            message.setChatId(update.getMessage().getChatId().toString());
            String newMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            if (newMessage.equals("/start")) {
                StringBuilder introductionMessage = new StringBuilder();
                introductionMessage.append("Hello I'm <b>Neuneu_bot</b> 🪲, you can use me to receive alerts on the Crypto market!\n\n"); // U+1FAB2
                introductionMessage.append("/add : add a subscription\n/remove : remove a subscription\n/view : view your subscriptions");
                message.setText(introductionMessage.toString());
            } else if (newMessage.equals("/add")) {
                message.setText("Select a cryptocurrency or type its name in the text area.");
                setButtons(message, newMessage, chatId);
                Chat chat = new Chat(chatId);
                chat.nextState(newMessage);
                chatsState.put(chatId, chat);
            } else if (newMessage.equals("/remove")) {
                message.setText("Select the cryptocurrency you wish to remove a subscription of.");
                setButtons(message, newMessage, chatId);
                Chat chat = new Chat(chatId);
                chat.nextState(newMessage);
                chatsState.put(chatId, chat);
            } else if (newMessage.equals("/view")) {
                message.setText("You don't have any subscription.");
                if (!Subscriptions.isEmpty() && Subscriptions.isJson()) {
                    JSONObject allSubscriptions = Subscriptions.getJsonContent();
                    JSONArray data = (JSONArray) allSubscriptions.get("data");
                    for (int i = 0; i < data.size(); i++) {
                        JSONObject subscriptions = (JSONObject) data.get(i);
                        if (subscriptions.get("chatId").toString().equals(chatId)) {
                            JSONArray subscriptionsArray = (JSONArray) subscriptions.get("subscriptions");
                            if (subscriptionsArray.size() > 0) {
                                StringBuilder subscriptionsMessage = new StringBuilder();
                                subscriptionsMessage.append("<b>Your subscriptions 🔔:</b>\n\n"); // U+1F514
                                for (int j = 0; j < subscriptionsArray.size(); j++) {
                                    JSONObject tokenSubscription = (JSONObject) subscriptionsArray.get(j);
                                    subscriptionsMessage.append("<b>" + tokenSubscription.get("symbol").toString() + ":</b>\n");
                                    JSONObject usd = (JSONObject) tokenSubscription.get("USD");
                                    usd.keySet().forEach(key -> {
                                        subscriptionsMessage.append(key.toString() + ": $ " + usd.get(key).toString() + "\n");
                                    });
                                    subscriptionsMessage.append("\n");
                                }
                                message.setText(subscriptionsMessage.toString());
                            }
                        }
                    }
                }
            } else if (chatsState.get(chatId) != null && chatsState.get(chatId).getState() instanceof AddCoinState) {
                if (isCryptoInArray(newMessage)) {
                    message.setText("Select the quote.");
                    setButtons(message, newMessage, chatId);
                    Chat chat = chatsState.get(chatId);
                    chat.nextState("/" + newMessage);
                } else {
                    message.setText("The selected token is not valid.");
                    Chat chat = chatsState.get(chatId);
                    chat.setState(new NoneState(""));
                }
            } else if (chatsState.get(chatId) != null && chatsState.get(chatId).getState() instanceof AddTypeState) {
                if (isTypeValid(newMessage)) {
                    message.setText("Select the target.");
                    Chat chat = chatsState.get(chatId);
                    chat.nextState("/" + newMessage);
                } else {
                    message.setText("The quote is not valid.");
                    Chat chat = chatsState.get(chatId);
                    chat.setState(new NoneState(""));
                }
            } else if (chatsState.get(chatId) != null && chatsState.get(chatId).getState() instanceof AddTargetState) {
                Chat chat = chatsState.get(chatId);
                String request = chat.getState().getStateString();
                chat.setState(new NoneState(""));
                if (isTargetValid(newMessage)) {
                    String[] requestArray = request.split("/", 0);
                    message.setText(Subscriptions.add(chatId, requestArray[2], requestArray[3], newMessage));
                } else {
                    message.setText("The target must be a number.");
                }
            } else if (chatsState.get(chatId) != null && chatsState.get(chatId).getState() instanceof RemoveCoinState) {
                if (isCryptoInSubscriptions(newMessage, chatId)) {
                    message.setText("Select the quote.");
                    setButtons(message, newMessage, chatId);
                    Chat chat = chatsState.get(chatId);
                    chat.nextState("/" + newMessage);
                } else {
                    message.setText("You don't have any subscription for the selected token.");
                    Chat chat = chatsState.get(chatId);
                    chat.setState(new NoneState(""));
                }
            } else if (chatsState.get(chatId) != null && chatsState.get(chatId).getState() instanceof RemoveTypeState) {
                Chat chat = chatsState.get(chatId);
                String request = chat.getState().getStateString();
                chat.setState(new NoneState(""));
                String[] requestArray = request.split("/", 0);
                message.setText(Subscriptions.remove(chatId, requestArray[2], newMessage));
            }
            if (!message.getText().isEmpty()) {
                try {
                    execute(message); // Call method to send the message
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getAllCryptocurrencies() {
        JSONObject allCryptocurrencies = CmcAPI.getCmcJson();
        if (allCryptocurrencies == null) {
            allCryptocurrencies = CmcAPI.getCryptocurrency();
            CmcAPI.setCmcJson(allCryptocurrencies);
        }
        return allCryptocurrencies;
    }

    private boolean isCryptoInSubscriptions(String crypto, String chatId) {
        JSONObject allSubscriptions = Subscriptions.getJsonContent();
        JSONArray data = (JSONArray) allSubscriptions.get("data");
        for (int i = 0; i < data.size(); i++) {
            JSONObject chatSubscriptions = (JSONObject) data.get(i);
            if (chatSubscriptions.get("chatId").toString().equals(chatId)) {
                JSONArray subscriptions = (JSONArray) chatSubscriptions.get("subscriptions");
                for (int j = 0; j < subscriptions.size(); j++) {
                    JSONObject subscription = (JSONObject) subscriptions.get(j);
                    if (subscription.get("symbol").toString().equals(crypto)) return true;
                }
            }
        }
        return false;
    }

    private boolean isCryptoInArray(String crypto) {
        JSONObject allCryptocurrencies = getAllCryptocurrencies();
        JSONArray data = (JSONArray) allCryptocurrencies.get("data");
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonCryptocurrency = (JSONObject) data.get(i);
            if (jsonCryptocurrency.get("symbol").toString().equalsIgnoreCase(crypto)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTypeValid(String type) {
        return (type.equals("price") || type.equals("market_cap"));
    }

    private boolean isTargetValid(String target) {
        Pattern isNumeric = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (target == null) return false;
        return isNumeric.matcher(target).matches();
    }

    public synchronized void setButtons(SendMessage sendMessage, String newMessage, String chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        if (newMessage.equals("/add")) {
            JSONObject allCryptocurrencies = getAllCryptocurrencies();
            JSONArray data = (JSONArray) allCryptocurrencies.get("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonCryptocurrency = (JSONObject) data.get(i);
                row.add(new KeyboardButton(jsonCryptocurrency.get("symbol").toString()));
                if (i % 5 == 0 && i > 0) {
                    keyboard.add(row);
                    row = new KeyboardRow();
                }
            }
            keyboard.add(row);
        } else if (chatsState.get(chatId) != null && chatsState.get(chatId).getState() instanceof AddCoinState) {
            row.add(new KeyboardButton("price"));
            row.add(new KeyboardButton("market_cap"));
            keyboard.add(row);
        } else if (newMessage.equals("/remove")) {
            JSONObject allSubscriptions = Subscriptions.getJsonContent();
            JSONArray data = (JSONArray) allSubscriptions.get("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject chatSubscriptions = (JSONObject) data.get(i);
                if (chatSubscriptions.get("chatId").toString().equals(chatId)) {
                    JSONArray subscriptions = (JSONArray) chatSubscriptions.get("subscriptions");
                    for (int j=0; j<subscriptions.size(); j++) {
                        JSONObject subscription = (JSONObject) subscriptions.get(j);
                        row.add(new KeyboardButton(subscription.get("symbol").toString()));
                        if (j % 5 == 0 && j > 0) {
                            keyboard.add(row);
                            row = new KeyboardRow();
                        }
                    }
                }
            }
            keyboard.add(row);
        } else if (chatsState.get(chatId) != null && chatsState.get(chatId).getState() instanceof RemoveCoinState) {
            JSONObject allSubscriptions = Subscriptions.getJsonContent();
            JSONArray data = (JSONArray) allSubscriptions.get("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject chatSubscriptions = (JSONObject) data.get(i);
                if (chatSubscriptions.get("chatId").toString().equals(chatId)) {
                    JSONArray subscriptions = (JSONArray) chatSubscriptions.get("subscriptions");
                    for (int j=0; j<subscriptions.size(); j++) {
                        JSONObject subscription = (JSONObject) subscriptions.get(j);
                        if (subscription.get("symbol").toString().equals(newMessage)) {
                            JSONObject usd = (JSONObject) subscription.get("USD");
                            if (usd.get("price") != null) row.add(new KeyboardButton("price"));
                            if (usd.get("market_cap") != null) row.add(new KeyboardButton("market_cap"));
                        }
                    }
                }
            }
            keyboard.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
