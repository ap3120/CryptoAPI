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
import java.util.List;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    private static Dotenv dotenv = Dotenv.load();
    private static String BOT_TOKEN = dotenv.get("TELEGRAM_BOT_API");
    private static String BOT_USERNAME = dotenv.get("TELEGRAM_BOT_USERNAME");

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            //ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
            //replyKeyboardRemove.setRemoveKeyboard(true);
            if (update.getMessage().getText().equals("/start")) {

            } else if (update.getMessage().getText().equals("/cryptocurrency")) {
                message.setText("Select a cryptocurrency.");
                //setButtons(message);
                setInline(message);
            } else if (update.getMessage().getText().equals("/subscriptions")) {

            } else if (update.getMessage().getText().equals("/ethgas")) {

            }
            if (! message.getText().isEmpty()) {
                try {
                    execute(message); // Call method to send the message
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            //System.out.println(update.getMessage().getChatId().toString());
        } else if (update.hasCallbackQuery()) {
            answerCallbackQuery(update);
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

    public synchronized void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Hi"));
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton("Help"));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    private void setInline(SendMessage message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();

        JSONArray allCryptocurrencies = CmcAPI.getCryptocurrency("all");
        for (Object cryptocurrency : allCryptocurrencies.toArray()) {
            JSONObject jsonCryptocurrency = (JSONObject) cryptocurrency;
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(jsonCryptocurrency.get("symbol").toString());
            inlineKeyboardButton.setCallbackData(jsonCryptocurrency.get("symbol").toString());
            buttons1.add(inlineKeyboardButton);
        }
        buttons.add(buttons1);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        message.setReplyMarkup(markupKeyboard);
    }

    public synchronized void answerCallbackQuery(Update update) {
        String call_data = update.getCallbackQuery().getData();
        long message_id = update.getCallbackQuery().getMessage().getMessageId();
        long chat_id = update.getCallbackQuery().getMessage().getChatId();

        JSONArray allCryptocurrencies = CmcAPI.getCryptocurrency("all");
        for (Object cryptocurrency : allCryptocurrencies.toArray()) {
            JSONObject jsonCryptocurrency = (JSONObject) cryptocurrency;
            if (call_data.equals(jsonCryptocurrency.get("symbol").toString())) {
                JSONObject quote = (JSONObject) jsonCryptocurrency.get("quote");
                JSONObject usd = (JSONObject) quote.get("USD");
                String answer = jsonCryptocurrency.get("symbol").toString() + "\n" + usd.toJSONString();
                EditMessageText new_message = new EditMessageText();
                new_message.setChatId(chat_id);
                new_message.setMessageId(toIntExact(message_id));
                new_message.setText(answer);
                try {
                    execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            }
        }


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
