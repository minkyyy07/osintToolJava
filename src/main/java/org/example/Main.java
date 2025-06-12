package org.example;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Callable;

public class Main implements Callable<Integer> {

    // Класс для цветного оформления консоли
    static class ConsoleColors {
        // Цвета текста
        public static final String RESET = "\033[0m";
        public static final String BLACK = "\033[0;30m";
        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String BLUE = "\033[0;34m";
        public static final String PURPLE = "\033[0;35m";
        public static final String CYAN = "\033[0;36m";
        public static final String WHITE = "\033[0;37m";

        // Жирный текст
        public static final String BLACK_BOLD = "\033[1;30m";
        public static final String RED_BOLD = "\033[1;31m";
        public static final String GREEN_BOLD = "\033[1;32m";
        public static final String YELLOW_BOLD = "\033[1;33m";
        public static final String BLUE_BOLD = "\033[1;34m";
        public static final String PURPLE_BOLD = "\033[1;35m";
        public static final String CYAN_BOLD = "\033[1;36m";
        public static final String WHITE_BOLD = "\033[1;37m";

        // Фон
        public static final String BLACK_BACKGROUND = "\033[40m";
        public static final String RED_BACKGROUND = "\033[41m";
        public static final String GREEN_BACKGROUND = "\033[42m";
        public static final String YELLOW_BACKGROUND = "\033[43m";
        public static final String BLUE_BACKGROUND = "\033[44m";
        public static final String PURPLE_BACKGROUND = "\033[45m";
        public static final String CYAN_BACKGROUND = "\033[46m";
        public static final String WHITE_BACKGROUND = "\033[47m";
    }

    // Методы для рисования интерфейса
    private static void printHeader(String title) {
        String border = "═══════════════════════════════════════════════════════════════";
        System.out.println(ConsoleColors.CYAN + border + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN_BOLD + "   " + title + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + border + ConsoleColors.RESET);
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println(ConsoleColors.YELLOW_BOLD + "► " + title + ConsoleColors.RESET);
        System.out.println(ConsoleColors.YELLOW + "──────────────────────────────────" + ConsoleColors.RESET);
    }

    private static void printResult(String key, String value) {
        System.out.println(ConsoleColors.GREEN_BOLD + "  " + key + ": " + ConsoleColors.WHITE + value + ConsoleColors.RESET);
    }

    private static void printError(String message) {
        System.out.println(ConsoleColors.RED + "✗ " + message + ConsoleColors.RESET);
    }

    private static void printSuccess(String message) {
        System.out.println(ConsoleColors.GREEN_BOLD + "✓ " + message + ConsoleColors.RESET);
    }

    @Option(names = {"-p", "--phone"}, description = "Phone number for search")
    String phone;

    @Option(names = {"-a", "--address"}, description = "Address for search")
    String address;

    public static void main(String[] args) {
        // Очистка консоли
        System.out.print("\033[H\033[2J");
        System.out.flush();

        printHeader("OSINT TOOL v1.0");

        java.util.Scanner scanner = new java.util.Scanner(System.in);

        System.out.print(ConsoleColors.BLUE + "☎ Введите номер телефона " +
                         ConsoleColors.WHITE + "(или оставьте пустым): " + ConsoleColors.WHITE);
        String phoneInput = scanner.nextLine().trim();

        System.out.print(ConsoleColors.BLUE + "🏠 Введите адрес " +
                         ConsoleColors.WHITE + "(или оставьте пустым): " + ConsoleColors.WHITE);
        String addressInput = scanner.nextLine().trim();
        scanner.close();

        System.out.println();
        System.out.println(ConsoleColors.PURPLE + "Загрузка данных..." + ConsoleColors.RESET);

        Main main = new Main();
        if (!phoneInput.isEmpty()) main.phone = phoneInput;
        if (!addressInput.isEmpty()) main.address = addressInput;
        int exitCode = new CommandLine(main).execute();

        System.out.println();
        System.out.println(ConsoleColors.CYAN + "Завершение работы с кодом: " + exitCode + ConsoleColors.RESET);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        OkHttpClient client = new OkHttpClient();

        if (phone == null && address == null) {
            printError("Необходимо указать номер телефона или адрес для поиска.");
            return 1;
        }

        boolean hasResults = false;

        if (phone != null) {
            printSection("ПОИСК ПО НОМЕРУ ТЕЛЕФОНА: " + phone);

            // Демо-режим вместо реального API для тестирования
            boolean useDemo = true; // установите в false, когда у вас будут рабочие API-ключи

            if (!useDemo) {
                try {
                    // Для использования реального API раскомментируйте этот блок и укажите ваши ключи
                    String url = "http://apilayer.net/api/validate";

                    // Создаем URL с параметрами запроса - замените YOUR_REAL_API_KEY на ваш настоящий ключ
                    url += "?access_key=YOUR_REAL_API_KEY";
                    url += "&number=" + java.net.URLEncoder.encode(phone, java.nio.charset.StandardCharsets.UTF_8);
                    url += "&country_code=&format=1";

                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .header("User-Agent", "osintTool/1.0")
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "";
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode jsonNode = mapper.readTree(body);

                            if (jsonNode.has("success") && jsonNode.path("success").asBoolean()) {
                                printSuccess("Информация о номере получена");

                                if (jsonNode.has("valid")) {
                                    printResult("Валидный номер", jsonNode.path("valid").asBoolean() ? "Да" : "Нет");
                                }

                                if (jsonNode.has("country_name")) {
                                    printResult("Страна", jsonNode.path("country_name").asText());
                                }

                                if (jsonNode.has("location")) {
                                    printResult("Местоположение", jsonNode.path("location").asText());
                                }

                                if (jsonNode.has("carrier")) {
                                    printResult("Оператор", jsonNode.path("carrier").asText());
                                }

                                if (jsonNode.has("line_type")) {
                                    printResult("Тип", jsonNode.path("line_type").asText());
                                }

                                hasResults = true;
                            } else {
                                printError("API вернул ошибку: " + jsonNode.path("error").path("info").asText());
                            }
                        } else {
                            printError("Ошибка запроса: " + response.code() + " - " + response.message());
                        }
                    }
                } catch (Exception e) {
                    printError("Ошибка при выполнении запроса: " + e.getMessage());
                }
            }

            // Если API недоступен или включен демо-режим, показываем демо-данные
            if (!hasResults) {
                printSuccess("Демонстрационные данные для номера " + phone);

                // Определяем предполагаемого оператора по первым цифрам (для демонстрации)
                String operator = "Неизвестный оператор";
                String region = "Неизвестный регион";
                String type = "Мобильный";

                // Простая демо-логика определения операторов по префиксам (только для демонстрации)
                if (phone.startsWith("+7") || phone.startsWith("8")) {
                    String code = phone.startsWith("+7") ? phone.substring(2, 5) : phone.substring(1, 4);

                    if (code.equals("900") || code.equals("901") || code.equals("902") || code.equals("904")) {
                        operator = "МТС";
                        region = "Россия";
                    } else if (code.equals("910") || code.equals("911") || code.equals("915")) {
                        operator = "Билайн";
                        region = "Россия";
                    } else if (code.equals("920") || code.equals("921") || code.equals("922")) {
                        operator = "Мегафон";
                        region = "Россия";
                    } else if (code.equals("950") || code.equals("951") || code.equals("952")) {
                        operator = "Tele2";
                        region = "Россия";
                    } else {
                        // Коды регионов (очень упрощенно для демо)
                        if (code.equals("495") || code.equals("499")) {
                            operator = "Городская телефонная сеть";
                            region = "Москва";
                            type = "Стационарный";
                        } else if (code.equals("812")) {
                            operator = "Городская телефонная сеть";
                            region = "Санкт-Петербург";
                            type = "Стационарный";
                        }
                    }
                } else if (phone.startsWith("+380")) {
                    operator = "Украинский оператор";
                    region = "Украина";
                } else if (phone.startsWith("+375")) {
                    operator = "Белорусский оператор";
                    region = "Беларусь";
                } else if (phone.startsWith("+1")) {
                    operator = "Американский/Канадский оператор";
                    region = "США/Канада";
                }

                printResult("Номер телефона", phone);
                printResult("Страна/Регион", region);
                printResult("Оператор", operator);
                printResult("Тип номера", type);
                printResult("Валидный номер", "Да");

                hasResults = true;
            }
        }

        if (address != null) {
            printSection("ПОИСК ПО АДРЕСУ: " + address);

            String url = "https://nominatim.openstreetmap.org/search?q=" +
                    java.net.URLEncoder.encode(address, java.nio.charset.StandardCharsets.UTF_8) +
                    "&format=json&addressdetails=1";
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "osintTool/1.0 (your_email@example.com)")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "";
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode resultsNode = mapper.readTree(body);

                    if (resultsNode.isArray() && resultsNode.size() > 0) {
                        printSuccess("Найдено " + resultsNode.size() + " результатов");

                        for (int i = 0; i < Math.min(3, resultsNode.size()); i++) {
                            JsonNode result = resultsNode.get(i);

                            System.out.println();
                            System.out.println(ConsoleColors.CYAN_BOLD + "  Результат #" + (i+1) + ConsoleColors.RESET);
                            System.out.println(ConsoleColors.CYAN + "  ─────────────" + ConsoleColors.RESET);

                            if (result.has("display_name")) {
                                printResult("Полное название", result.path("display_name").asText());
                            }

                            if (result.has("lat") && result.has("lon")) {
                                printResult("Координаты", result.path("lat").asText() + ", " + result.path("lon").asText());
                            }

                            if (result.has("type")) {
                                printResult("Тип", result.path("type").asText());
                            }

                            if (result.has("addresstype")) {
                                printResult("Тип адреса", result.path("addresstype").asText());
                            }
                        }
                        hasResults = true;
                    } else {
                        printError("Адрес не найден");
                    }
                } else {
                    printError("Ошибка запроса: " + response.code());
                }
            } catch (Exception e) {
                printError("Ошибка при выполнении запроса: " + e.getMessage());
            }
        }

        if (!hasResults) {
            printError("Не удалось получить результаты");
            return 2;
        }

        return 0;
    }
}

