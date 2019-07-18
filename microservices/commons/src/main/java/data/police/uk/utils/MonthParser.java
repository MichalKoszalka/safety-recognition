package data.police.uk.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MonthParser {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate toLocalDate(String month) {
        return LocalDate.parse(month + "-01", FORMATTER);
    }

}
