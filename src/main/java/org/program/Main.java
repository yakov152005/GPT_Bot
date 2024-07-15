package org.program;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * דומיין כללי: https://app.seker.live/fm1/
 *
 * נתיב: send-message
 * תיאור: שליחת הודעה ל-chatGPT
 * פרמטרים נדרשים:
 * String id – תעודת זהות של השולח
 * String text – ההודעה עצמה
 *
 * נתיב: clear-history
 * תיאור: מחיקת ההיסטוריה של השיחה הקודמת על מנת להתחיל שיחה חדשה
 * פרמטרים נדרשים:
 * String id – תעודת זהות של השולח
 *
 * נתיב: check-balance
 * תיאור: בדיקת כמות ההודעות שנותרה לשלוח
 * פרמטרים נדרשים:
 * String id – תעודת זהות של השולח
 *
 *
 *
 *
 * קודי שגיאה אפשריים:
 * 3000 – לא נשלחה תעודת זהות
 * 3001 – תעודת בזהות לא קיימת במאגר
 * 3002 – נגמרה מכסת הבקשות לתעודת זהות זו
 * 3003 – לא נשלח טקסט להודעה
 * 3005 – שגיאה כללית
 */
public class Main {

    public static CloseableHttpClient client = HttpClients.createDefault();
    public static Scanner s = new Scanner(System.in);
    public static final int ERROR_0 = 3000;
    public static final int ERROR_1 = 3001;
    public static final int ERROR_2 = 3002;
    public static final int ERROR_3 = 3003;
    public static final int ERROR_4 = 3005;
    public static final String E_0 = "ID card was not sent";
    public static final String E_1 = "ID card does not exist in the database";
    public static final String E_2 = "The quota of applications for this identity card has expired";
    public static final String E_3 = "No message text was sent";
    public static final String E_4 = "general error";
    public static final String ID = "039575329";
    public static final String DOM = "https://app.seker.live/fm1/";
    public static final String PATH_M = "send-message";
    public static final String PATH_H = "clear-history";
    public static final String PATH_B = "check-balance";

    public String userId;
    private String text;

    public Main() throws IOException, URISyntaxException {
        this.userId = ID;
        this.text = null;

        while (true) {
            mainMenu();
            String chooseStr = s.nextLine();
            int choose = stringToInt(chooseStr);

            if (choose == 0) {
                System.out.println("Exit...");
                break;
            }
            switch (choose) {
                case 1:
                    chatLoop(userId);
                    break;
                case 2:
                    clearHistory(userId);
                    break;
                case 3:
                    checkBalance(ID);
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    public static int stringToInt(String str) {
        int choose = 5;
        try {
            choose = Integer.parseInt(str);
        } catch (NumberFormatException e) {
        }
        return choose;
    }

    public static String inputString() {
        System.out.println("Enter your ID:");
        return s.nextLine();
    }

    public static String inputText() {
        System.out.println("Enter message: ");
        return s.nextLine();
    }

    public static void mainMenu() {
        System.out.println("What do you want to do?");
        System.out.println("""
                1 - Send message to Chat GPT
                2 - Clear history
                3 - Check balance
                0 - Exit.
                """);
    }

    public static URIBuilder getUriBuilder(String httpsName) throws URISyntaxException {
        return new URIBuilder(DOM + httpsName);
    }

    public static void chatLoop(String userId) throws IOException, URISyntaxException {
        while (true) {
            String text = inputText();
            if (text.equalsIgnoreCase("exit")) {
                System.out.println("Exiting chat...");
                break;
            }
            sendMessage(userId, text);
        }
    }

    public static void clearHistory(String userId)throws IOException, URISyntaxException{
        URI uriBuilder = getUriBuilder(PATH_H)
                .setParameter("id",userId).build();

        String myResponse = getAndResponse(uriBuilder);
        Response responseObj = getResponseObj(myResponse);
        if (responseObj.isSuccess()){
            System.out.println("Clear chat...");
        }else {
            errorCode(responseObj);
        }

    }

    public void checkBalance(String userId) throws IOException, URISyntaxException{
        URI uriBuilder = getUriBuilder(PATH_B)
                .setParameter("id",userId)
                .build();

        String myResponse = getAndResponse(uriBuilder);
        Response response = getResponseObj(myResponse);
        if (response.isSuccess()){
            System.out.println("The amount of message do u have is: " + response.getExtra());
        }
    }

    public static void sendMessage(String userId, String text) throws IOException, URISyntaxException {
        URI uriBuilder = getUriBuilder(PATH_M)
                .setParameter("id", userId)
                .setParameter("text", text)
                .build();

        String request = getAndResponse(uriBuilder);
        if (!request.isEmpty()) {
            Response responseObj = getResponseObj(request);
            if (responseObj.isSuccess()) {
                System.out.println(responseObj.getExtra());
            } else {
                errorCode(responseObj);
            }
        } else {
            System.out.println("The message was not sent.");
        }
    }

    public static String postAndResponse(URI uriBuilder) throws IOException {
        HttpPost request = new HttpPost(uriBuilder);
        CloseableHttpResponse response = client.execute(request);
        String result = EntityUtils.toString(response.getEntity());
        System.out.println("HTTP Status: " +(response.getStatusLine().getStatusCode() == 200 ? "תקין" : "לא תקין" ) );
        response.close();
        return result;
    }

    public static String getAndResponse(URI uriBuilder) throws IOException {
        HttpGet get = new HttpGet(uriBuilder);
        CloseableHttpResponse response = client.execute(get);
        return EntityUtils.toString(response.getEntity());
    }

    public static Response getResponseObj(String myResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(myResponse, Response.class);
    }

    public static void errorCode(Response responseObj) {
        Integer errorCode = responseObj.getErrorCode();
        if (errorCode != null) {
            switch (errorCode) {
                case ERROR_0:
                    System.out.println(E_0);
                    break;
                case ERROR_1:
                    System.out.println(E_1);
                    break;
                case ERROR_2:
                    System.out.println(E_2);
                    break;
                case ERROR_3:
                    System.out.println(E_3);
                    break;
                case ERROR_4:
                    System.out.println(E_4);
                    break;
                default:
                    System.out.println("Unknown error code: " + errorCode);
            }
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new Main();
    }
}
