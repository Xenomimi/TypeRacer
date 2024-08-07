package pl.wsiz.typeracerfx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ApiCall {
    public String bookUrl = "https://wolnelektury.pl/media/book/txt/lalka-tom-pierwszy.txt";

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public String sendGetRequest() {
        String finalText = "";

        try {
            // Tworzenie obiektu URL z adresem API Wolnych Lektur
            URL url = new URL(bookUrl);

            System.out.println(bookUrl);

            // Otwieranie połączenia HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Ustawianie metody żądania (GET)
            connection.setRequestMethod("GET");

            // Odczytywanie odpowiedzi
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n");
                }
                in.close();

                // Wybranie odpowiednio losowego fragmentu
                String[] lines = response.toString().split(";");
                int minline = 1;
                int maxline = lines.length;
                Random random = new Random();
                finalText += lines[random.nextInt(maxline - minline) + minline];

                finalText = processText(finalText);

                System.out.println(finalText);

            } else {
                System.out.println("Błąd podczas wysyłania żądania. Kod odpowiedzi: " + responseCode);
            }

            // Zamknięcie połączenia
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalText.substring(0);
    }

    public String processText(String inputText) {
        // Usunięcie pierwszego znaku spacji
        inputText = inputText.substring(1);

        inputText = inputText.replace("— ", "");

        inputText = inputText.replace("\n", " ");

        inputText = inputText.replace("  ", " ");

        inputText = inputText.replace("…", "...");

        inputText = inputText.replace("„", "\"");

        inputText = inputText.replace("”", "\"");

        inputText = inputText.replace("–", "-");

        inputText = inputText.replace("»", "");

        inputText = inputText.replace("«", "");

        return inputText;
    }
}
