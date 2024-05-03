package pl.wsiz.typeracerfx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ApiCall {
    public String sendGetRequest() {
        StringBuilder finalText = new StringBuilder();

        try {
            // Tworzenie obiektu URL z adresem API Wolnych Lektur
            URL url = new URL("https://wolnelektury.pl/media/book/txt/wolter-biale-i-czarne.txt");

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
                int minline = 0;
                int maxline = lines.length;

                Random random = new Random();

                finalText.append(lines[random.nextInt(maxline - minline) + minline]);

                System.out.println(finalText);

            } else {
                System.out.println("Błąd podczas wysyłania żądania. Kod odpowiedzi: " + responseCode);
            }

            // Zamknięcie połączenia
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalText.toString();
    }
}