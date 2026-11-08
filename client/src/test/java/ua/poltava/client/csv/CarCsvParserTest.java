package ua.poltava.client.csv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarCsvParserTest {

    @Test
    void validLineIsParsed() {
        var row = CarCsvParser.parse("WBAXX11AAAP000001,BMW,X5,2017,Бойко Андрій,+380931234567");
        assertEquals("BMW", row.brand());
        assertEquals(2017, row.year());
        assertEquals("+380931234567", row.ownerPhone());
    }

    @Test
    void wrongFieldCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> CarCsvParser.parse("WBAXX11AAAP000001,BMW"));
    }

    @Test
    void badVinAndBadYearAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> CarCsvParser.parse("SHORTVIN123,Honda,Civic,2016,Тест,+380501234000"));
        assertThrows(IllegalArgumentException.class,
                () -> CarCsvParser.parse("TMBJJ7NE5J0000006,Skoda,Octavia,abcd,Тест,+380679876543"));
    }
}
