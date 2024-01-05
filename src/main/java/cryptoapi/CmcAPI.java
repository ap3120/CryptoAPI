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

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.lang.*;

public class CmcAPI implements Runnable {
    
    static Dotenv dotenv = Dotenv.load();
    private static final String CMC_API_KEY = dotenv.get("CMC_API_KEY");
    private static JSONObject cmcJson;
    private Bot bot = null;

    public CmcAPI(Bot bot) {
        this.bot = bot;
    }

    /**
     * Dispatches alerts to chats with subscriptions every 15 minutes
     */
    @Override
    public void run() {
        while (true) {
            if (Subscriptions.isJson() && !Subscriptions.isEmpty()) {
                cmcJson = getCryptocurrency();
                dispatchAlerts();
            } else {
                cmcJson = null;
            }
            try {
                Thread.sleep(900_000);
                //Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks chats subscriptions and send alert messages
     */
    private void dispatchAlerts() {
        JSONObject allSubscriptions = Subscriptions.getJsonContent();
        JSONArray data = (JSONArray) allSubscriptions.get("data");
        for (Object o : data.toArray()) {
            JSONObject chat = (JSONObject) o;
            String chatId = chat.get("chatId").toString();
            JSONArray subscriptions = (JSONArray) chat.get("subscriptions");
            for (Object obj : subscriptions.toArray()) {
                JSONObject tokenSubscriptions = (JSONObject) obj;
                JSONArray cmcData = (JSONArray) cmcJson.get("data");
                for (int i=0; i<cmcData.size(); i++) {
                    JSONObject cmcToken = (JSONObject) cmcData.get(i);
                    if (cmcToken.get("symbol").toString().equals(tokenSubscriptions.get("symbol").toString())) {
                        JSONObject usd = (JSONObject) tokenSubscriptions.get("USD");
                        JSONObject cmcQuote = (JSONObject) cmcToken.get("quote");
                        JSONObject cmcUsd = (JSONObject) cmcQuote.get("USD");
                        if (usd.get("price") != null) {
                            double targetPrice = Double.parseDouble(usd.get("price").toString());
                            double cmcPrice = Double.parseDouble(cmcUsd.get("price").toString());
                            double percentChangeOneHour = Double.parseDouble(cmcUsd.get("percent_change_1h").toString());
                            if (cmcPrice * Math.min(1, 1 - percentChangeOneHour/100) <= targetPrice && targetPrice <= cmcPrice * Math.max(1, 1 - percentChangeOneHour/100)) {
                                String message = tokenSubscriptions.get("symbol") + " has reached the target price of $ " + usd.get("price").toString() + ".";
                                bot.sendMsg(chatId, message); 
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a json with cryptocurrency data
     *
     * @returns json data
     */
    public static JSONObject getCryptocurrency() {
        JSONObject json = new JSONObject();

        String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        List<NameValuePair> paratmers = new ArrayList<NameValuePair>();
        paratmers.add(new BasicNameValuePair("start","1"));
        paratmers.add(new BasicNameValuePair("limit","200"));
        paratmers.add(new BasicNameValuePair("convert","USD"));

        try {
            String result = makeAPICall(uri, paratmers, CmcAPI.CMC_API_KEY);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonResult = (JSONObject) jsonParser.parse(result);
            json = jsonResult;
        } catch (IOException e) {
            System.out.println("Error: cannot access content - " + e.toString());
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e.toString());
        } catch (ParseException e) {
            System.out.println("Error: cannot parse result - " + e.toString());
        }
        return json;
    }

    /**
     * Makes an api call and returns the result
     *
     * @param uri - the api endpoint uri
     * @param parameters - api request parameters
     * @param key - the api key
     * @returns a result string
     */
    public static String makeAPICall(String uri, List<NameValuePair> parameters, String key) throws URISyntaxException, IOException {
        System.out.println("cmc api called");
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

    public static JSONObject getCmcJson() {
        return cmcJson;
    }

    public static void setCmcJson(JSONObject json) {
        cmcJson = json;
    }
}
