package cryptoapi;

import io.github.cdimascio.dotenv.Dotenv;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class App {

    public static void main( String[] args ) {
        Dotenv dotenv = Dotenv.load();
        String CHAT_ID = dotenv.get("CHAT_ID").toString();

        CmcAPI cmcAPI = new CmcAPI();
        Thread cmcAPIThread = new Thread(cmcAPI);
        cmcAPIThread.start();

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            Bot bot = new Bot();
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
