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
import org.json.simple.JSONArray;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.lang.Object;

public class CmcAPI {
    
    static Dotenv dotenv = Dotenv.load();
    private static final String CMC_API_KEY = dotenv.get("CMC_API_KEY");

    public static JSONArray getCryptocurrency(String crypto) {
        JSONArray jsonArray = new JSONArray();

        String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        List<NameValuePair> paratmers = new ArrayList<NameValuePair>();
        paratmers.add(new BasicNameValuePair("start","1"));
        paratmers.add(new BasicNameValuePair("limit","200"));
        paratmers.add(new BasicNameValuePair("convert","USD"));

        try {
            String result = makeAPICall(uri, paratmers, CmcAPI.CMC_API_KEY);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonResult = (JSONObject) jsonParser.parse(result);
            JSONArray jsonData = (JSONArray) jsonResult.get("data");
            if (crypto.equals("all")) {
                return jsonData;
            } else {
                for (Object data : jsonData.toArray()) {
                    JSONObject jsonSubData = (JSONObject) data;
                    if (jsonSubData.get("symbol").equals(crypto)) {
                        jsonArray.add(jsonSubData);
                        return jsonArray;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: cannot access content - " + e.toString());
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e.toString());
        } catch (ParseException e) {
            System.out.println("Error: cannot parse result - " + e.toString());
        }
        return jsonArray;
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
