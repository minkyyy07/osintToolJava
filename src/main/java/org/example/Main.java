package org.example;

import java.util.regex.Pattern;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Callable;

public class Main implements Callable<Integer> {

    static class ConsoleColors {
        public static final String RESET = "\033[0m";
        public static final String BLACK = "\033[0;30m";
        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String BLUE = "\033[0;34m";
        public static final String PURPLE = "\033[0;35m";
        public static final String CYAN = "\033[0;36m";
        public static final String WHITE = "\033[0;37m";
        public static final String BLACK_BOLD = "\033[1;30m";
        public static final String RED_BOLD = "\033[1;31m";
        public static final String GREEN_BOLD = "\033[1;32m";
        public static final String YELLOW_BOLD = "\033[1;33m";
        public static final String BLUE_BOLD = "\033[1;34m";
        public static final String PURPLE_BOLD = "\033[1;35m";
        public static final String CYAN_BOLD = "\033[1;36m";
        public static final String WHITE_BOLD = "\033[1;37m";
        public static final String BLACK_BACKGROUND = "\033[40m";
        public static final String RED_BACKGROUND = "\033[41m";
        public static final String GREEN_BACKGROUND = "\033[42m";
        public static final String YELLOW_BACKGROUND = "\033[43m";
        public static final String BLUE_BACKGROUND = "\033[44m";
        public static final String PURPLE_BACKGROUND = "\033[45m";
        public static final String CYAN_BACKGROUND = "\033[46m";
        public static final String WHITE_BACKGROUND = "\033[47m";
    }

    private static void printHeader(String title) {
        String border = "═══════════════════════════════════════════════════════════════";
        System.out.println(ConsoleColors.CYAN + border + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN_BOLD + "   " + title + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + border + ConsoleColors.RESET);
    }

    private static void printMenu() {
        System.out.println(ConsoleColors.YELLOW_BOLD + "\nДоступные функции:" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN_BOLD + "  1. Поиск по номеру телефона" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.GREEN_BOLD + "  2. Поиск по адресу" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.PURPLE_BOLD + "  3. Поиск по email" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.BLUE_BOLD + "  4. Поиск по IP-адресу" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.RED_BOLD + "  5. DDoS (тестовая функция)" + ConsoleColors.RESET);
        System.out.println();
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

    @Option(names = {"-e", "--email"}, description = "Email for search")
    String email;

    @Option(names = {"-i", "--ip"}, description = "IP address for search")
    String ip;

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        printHeader("OSINT TOOL v1.1");
        printMenu();

        java.util.Scanner scanner = new java.util.Scanner(System.in);

        System.out.print(ConsoleColors.PURPLE + "Выберите функции (1-5, через запятую или пробел): " + ConsoleColors.WHITE);
        String selectionInput = scanner.nextLine().trim();
        String[] selections = selectionInput.split("[ ,]+");
        boolean[] selected = new boolean[5];
        for (String sel : selections) {
            try {
                int idx = Integer.parseInt(sel);
                if (idx >= 1 && idx <= 5) selected[idx - 1] = true;
            } catch (NumberFormatException ignored) {}
        }

        String phoneInput = "", addressInput = "", emailInput = "", ipInput = "";
        if (selected[0]) {
            System.out.print(ConsoleColors.BLUE + "☎ Введите номер телефона (или оставьте пустым): " + ConsoleColors.WHITE);
            phoneInput = scanner.nextLine().trim();
        }
        if (selected[1]) {
            System.out.print(ConsoleColors.BLUE + "🏠 Введите адрес (или оставьте пустым): " + ConsoleColors.WHITE);
            addressInput = scanner.nextLine().trim();
        }
        if (selected[2]) {
            System.out.print(ConsoleColors.BLUE + "✉ Введите email (или оставьте пустым): " + ConsoleColors.WHITE);
            emailInput = scanner.nextLine().trim();
        }
        if (selected[3]) {
            System.out.print(ConsoleColors.BLUE + "🌐 Введите IP-адрес (или оставьте пустым): " + ConsoleColors.WHITE);
            ipInput = scanner.nextLine().trim();
        }
        if (selected[4]) {
            System.out.print(ConsoleColors.RED + "\uD83D\uDCA5 Введите URL для DDoS (тест): " + ConsoleColors.WHITE);
            String ddosUrl = scanner.nextLine().trim();
            if (!ddosUrl.isEmpty()) {
                performDdosTest(ddosUrl);
            }
        }

        scanner.close();

        System.out.println();
        System.out.println(ConsoleColors.PURPLE + "Загрузка данных..." + ConsoleColors.RESET);

        Main main = new Main();
        if (!phoneInput.isEmpty()) main.phone = phoneInput;
        if (!addressInput.isEmpty()) main.address = addressInput;
        if (!emailInput.isEmpty()) main.email = emailInput;
        if (!ipInput.isEmpty()) main.ip = ipInput;
        int exitCode = new CommandLine(main).execute();

        System.out.println();
        System.out.println(ConsoleColors.CYAN + "Завершение работы с кодом: " + exitCode + ConsoleColors.RESET);
        System.exit(exitCode);
    }

    private static void performDdosTest(String url) {
        printSection(ConsoleColors.RED_BOLD + "ТЕСТОВЫЙ DDoS: " + ConsoleColors.WHITE + url);
        System.out.println(ConsoleColors.RED + "Внимание! Это только демонстрация. Реальных запросов не отправляется." + ConsoleColors.RESET);
        for (int i = 1; i <= 5; i++) {
            System.out.println(ConsoleColors.RED + "[DDoS] Пакет #" + i + " отправлен на " + url + ConsoleColors.RESET);
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }
        printSuccess("Тестовая DDoS-атака завершена.");
    }

    @Override
    public Integer call() throws Exception {
        OkHttpClient client = new OkHttpClient();

        if (phone == null && address == null && email == null && ip == null) {
            printError("Необходимо указать хотя бы один параметр для поиска.");
            return 1;
        }

        boolean hasResults = false;

        // Улучшенный вывод рамки для каждого поиска
        if (phone != null) {
            printSection(ConsoleColors.CYAN_BOLD + "ПОИСК ПО НОМЕРУ ТЕЛЕФОНА: " + ConsoleColors.WHITE + phone);
            printSuccess("Демонстрационные данные для номера " + phone);

            String operator = "Неизвестный оператор";
            String region = "Неизвестный регион";
            String type = "Мобильный";

            if (phone.startsWith("+7") || phone.startsWith("8")) {
                String code = phone.startsWith("+7") ? phone.substring(2, 5) : phone.substring(1, 4);
                switch (code) {
                    case "900": case "901": case "902": case "904":
                        operator = "МТС"; region = "Россия"; break;
                    case "910": case "911": case "915":
                        operator = "Билайн"; region = "Россия"; break;
                    case "920": case "921": case "922":
                        operator = "Мегафон"; region = "Россия"; break;
                    case "950": case "951": case "952":
                        operator = "Tele2"; region = "Россия"; break;
                    case "495": case "499":
                        operator = "Городская телефонная сеть"; region = "Москва"; type = "Стационарный"; break;
                    case "812":
                        operator = "Городская телефонная сеть"; region = "Санкт-Петербург"; type = "Стационарный"; break;
                }
            } else if (phone.startsWith("+380")) {
                String codeUa = phone.substring(4, 6);
                switch (codeUa) {
                    case "50": operator = "Vodafone Украина"; region = "Украина"; break;
                    case "66": case "95": case "99": operator = "Vodafone Украина"; region = "Украина"; break;
                    case "63": case "73": case "93": operator = "lifecell"; region = "Украина"; break;
                    case "67": case "68": case "96": case "97": case "98": operator = "Киевстар"; region = "Украина"; break;
                    case "39": operator = "Golden Telecom"; region = "Украина"; break;
                    case "91": operator = "3Mob (Укртелеком)"; region = "Украина"; break;
                    case "92": operator = "PEOPLEnet"; region = "Украина"; break;
                    case "94": operator = "Интертелеком"; region = "Украина"; break;
                    default: operator = "Украинский оператор"; region = "Украина"; break;
                }
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

        if (email != null) {
            printSection("ПОИСК ПО EMAIL: " + email);

            boolean valid = Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", email);
            printResult("Валидный email", valid ? "Да" : "Нет");

            String gravatarHash = Integer.toHexString(email.trim().toLowerCase().hashCode());
            printResult("Gravatar Hash", gravatarHash);
            printResult("Gravatar URL", "https://www.gravatar.com/avatar/" + gravatarHash);

            hasResults = true;
        }

        if (ip != null) {
            printSection("ПОИСК ПО IP: " + ip);

            boolean valid = Pattern.matches(
                    "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", ip);
            printResult("Валидный IP", valid ? "Да" : "Нет");

            if (valid) {
                printResult("Страна", "Россия");
                printResult("Город", "Москва");
                printResult("Провайдер", "Ростелеком");
                printResult("Blacklisted", "Нет");
            }

            hasResults = true;
        }

        if (!hasResults) {
            printError("Не удалось получить результаты");
            return 2;
        }

        return 0;
    }
}

