package cryptoapi;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class App {

    public static void main( String[] args ) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            Bot bot = new Bot();
            telegramBotsApi.registerBot(bot);
            CmcAPI cmcAPI = new CmcAPI(bot);
            Thread cmcAPIThread = new Thread(cmcAPI);
            cmcAPIThread.start();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
