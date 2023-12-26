package cryptoapi;

import io.github.cdimascio.dotenv.Dotenv;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    private static Dotenv dotenv = Dotenv.load();
    private static String BOT_TOKEN = dotenv.get("TELEGRAM_BOT_API");
    private static String BOT_USERNAME = dotenv.get("TELEGRAM_BOT_USERNAME");
    private static HashMap<String, Chat> chatsState = new HashMap<>();
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            String newMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            if (newMessage.equals("/start")) {

            } else if (newMessage.equals("/add")) {
                message.setText("Select a cryptocurrency or type its name in the text area.");
                setButtons(message, newMessage, chatId);
                Chat chat = new Chat(chatId);
                chat.nextState(newMessage);
                chatsState.put(chatId, chat);
            } else if (newMessage.equals("/remove")) {

            } else if (newMessage.equals("/see")) {

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
                chat.setState(new NoneState(""));
                if (isTargetValid(newMessage)) {
                    String request = chat.getState().getStateString();
                    String[] requestArray = request.split("/", 0);
                    Subscriptions.add(chatId, requestArray[1], requestArray[2], requestArray[3]);
                } else {
                    message.setText("The target must be a number.");
                }
            }
            if (! message.getText().isEmpty()) {
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

    private boolean isCryptoInArray(String crypto) {
        JSONObject allCryptocurrencies = getAllCryptocurrencies();
        JSONArray data = (JSONArray) allCryptocurrencies.get("data");
        for (int i=0; i<data.size(); i++) {
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
        //replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        if (newMessage.equals("/add")) {
            JSONObject allCryptocurrencies = getAllCryptocurrencies();
            JSONArray data = (JSONArray) allCryptocurrencies.get("data");
            for (int i=0; i<data.size(); i++) {
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
