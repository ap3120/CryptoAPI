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

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CmcAPI implements Runnable {
    
    static Dotenv dotenv = Dotenv.load();
    private static final String CMC_API_KEY = dotenv.get("CMC_API_KEY");
    private static JSONObject cmcJson;

    @Override
    public void run() {
        while (true) {
            if (Subscriptions.isJson() && !Subscriptions.isEmpty()) {
                cmcJson = getCryptocurrency();
            } else {
                cmcJson = null;
            }
            try {
                Thread.sleep(900_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
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
