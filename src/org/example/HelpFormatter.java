package org.example;

public final class HelpFormatter {

    private HelpFormatter() {
    }

    public static String serverHelpMessage() {
        String[][] rows = {
                {"register login password", "создать учётную запись"},
                {"login user password", "логин и пароль"},
                {"help", "справка"},
                {"info", "тип коллекции и размер"},
                {"show", "все города из базы данных"},
                {"add", "добавить город"},
                {"update id", "изменить город с номером id"},
                {"remove_by_id id", "удалить город"},
                {"clear", "удалить все свои города"},
                {"insert_at [index]", "добавить город в конец (index опционален и игнорируется)"},
                {"add_if_max", "добавить город, если его id больше текущего максимума в коллекции"},
                {"count_less_than_standard_of_living value", "сколько элементов с уровнем жизни выше value"},
                {"filter_by_governor text", "фильтр городов по губернатору"},
                {"print_field_ascending_standard_of_living", "значения standardOfLiving по убыванию ранга"},
                {"exit", "закрыть клиент"}
        };
        return formatRows(rows);
    }

    private static String formatRows(String[][] rows) {
        int maxNameLen = 0;
        for (String[] row : rows) {
            if (row[0].length() > maxNameLen) {
                maxNameLen = row[0].length();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Доступные команды:\n");
        sb.append(repeat('-', maxNameLen + 50)).append('\n');
        for (String[] row : rows) {
            sb.append(String.format("%-" + maxNameLen + "s  |  %s%n", row[0], row[1]));
        }
        sb.append(repeat('-', maxNameLen + 50));
        return sb.toString();
    }

    private static String repeat(char c, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
