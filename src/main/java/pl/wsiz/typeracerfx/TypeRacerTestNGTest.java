package pl.wsiz.typeracerfx;

import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.ui.DialogService;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.testng.annotations.Test;

import java.io.IOException;

import static javafx.beans.binding.Bindings.when;
import static org.testng.Assert.*;

public class TypeRacerTestNGTest {

    private ApiCall apiCall = new ApiCall();

    @Test
    @DisplayName("Test poprawnego pobierania tekstu z API")
    public void testSendGetRequest_success() {
        // Ustawienie URL do testowego pliku na wolnelektury.pl
        apiCall.setBookUrl("https://wolnelektury.pl/media/book/txt/lalka-tom-pierwszy.txt");

        // Wywołanie metody sendGetRequest()
        String result = apiCall.sendGetRequest();

        // Sprawdzenie, czy zwrócono poprawny tekst
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Test obsługi błędu podczas wysyłania żądania")
    public void testSendGetRequest_networkError() throws IOException {
        // Ustawienie nieprawidłowego URL, który nie istnieje
        apiCall.setBookUrl("https://example.com/nonexistent.txt");

        // Wywołanie metody sendGetRequest()
        String result = apiCall.sendGetRequest();

        // Oczekujemy, że wynik będzie pusty, bo żądanie zakończy się błędem
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test przetwarzania i filtrowania tekstu")
    public void testProcessText() {
        // Przygotowanie testowych danych do przetworzenia
        String inputText = "— Przykładowy tekst 123 „zawierający” różne znaki - do filtrowania «";

        // Wywołanie metody processText()
        String processedText = apiCall.processText(inputText);

        // Sprawdzenie, czy tekst został poprawnie przetworzony i odfiltrowany
        assertNotNull(processedText);
        assertFalse(processedText.isEmpty());
        assertFalse(processedText.contains("—")); // Upewnienie się, że usunięto znaki specjalne
        assertFalse(processedText.contains("„"));
        assertFalse(processedText.contains("”"));
        assertFalse(processedText.contains("–"));
        assertFalse(processedText.contains("«"));
        assertFalse(processedText.contains("»"));
    }

    @Test
    @DisplayName("Test ustawienia nowego URL")
    public void testSetBookUrl() {
        // Ustawienie nowego URL
        String newUrl = "https://wolnelektury.pl/media/book/txt/dziady-dziady.txt";
        apiCall.setBookUrl(newUrl);

        // Sprawdzenie, czy URL został poprawnie ustawiony
        assertEquals(newUrl, apiCall.bookUrl);
    }
}
