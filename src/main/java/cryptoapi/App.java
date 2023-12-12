package cryptoapi;

import io.github.cdimascio.dotenv.Dotenv;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.lang.Object;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class App {

    public static void main( String[] args ) {

        Dotenv dotenv = Dotenv.load();
        String API_KEY = dotenv.get("API_KEY");
        String CHAT_ID = dotenv.get("CHAT_ID").toString();

        String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        List<NameValuePair> paratmers = new ArrayList<NameValuePair>();
        paratmers.add(new BasicNameValuePair("start","1"));
        paratmers.add(new BasicNameValuePair("limit","100"));
        paratmers.add(new BasicNameValuePair("convert","USD"));

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            Bot bot = new Bot();
            telegramBotsApi.registerBot(bot);
            try {
                String result = makeAPICall(uri, paratmers, API_KEY);
                String msgToSend = extractFromJson(result);
                if (!msgToSend.isEmpty()) {
                    bot.sendMsg(CHAT_ID, msgToSend);
                }
            } catch (IOException e) {
                System.out.println("Error: cannont access content - " + e.toString());
            } catch (URISyntaxException e) {
                System.out.println("Error: Invalid URL " + e.toString());
            } catch (ParseException e) {
                System.out.println("Error: cannot parse result - " + e.toString());
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static String extractFromJson(String result) throws ParseException {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonResult = (JSONObject) jsonParser.parse(result);
            JSONArray jsonData = (JSONArray) jsonResult.get("data");
            for (Object data : jsonData.toArray()) {
                JSONObject jsonSubData = (JSONObject) data;
                if (jsonSubData.get("symbol").equals("SOL")) {
                    JSONObject tmp = (JSONObject) jsonSubData.get("quote");
                    JSONObject ttmp = (JSONObject) tmp.get("USD");
                    String str = "SOL: " + ttmp.get("price").toString();
                    return str;
                }
            }
        } catch (ParseException e) {
            System.out.println("Error: cannot parse result - " + e.toString());
        }
        return "";
    }

    public static String makeAPICall(String uri, List<NameValuePair> parameters, String key) throws URISyntaxException, IOException {
        String response_content = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", key);

        CloseableHttpResponse response = client.execute(request);

        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        return response_content;
    }
}
