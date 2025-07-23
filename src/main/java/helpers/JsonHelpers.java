package helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import constants.ConfigData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonHelpers {

    // Get value from JSON file with multiple keys
    public static String getValueJsonObject(String... keys) {
        String value = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode node = objectMapper.readTree(new File(ConfigData.JSON_DATA_FILE_PATH));

            for (String key : keys) {
                node = node.path(key);
            }

            value = node.asText();
            System.out.println("Value: " + value);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    // Get value from JSON file in new file path with multiple keys
    public static String getValueJsonObject_FilePath(String filePath, String... keys) {
        String value = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode node = objectMapper.readTree(new File(filePath));

            for (String key : keys) {
                node = node.path(key);
            }

            value = node.asText();
            System.out.println("Value: " + value);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    // Get value from JSON array
    public static String getValueJsonArray(int itemIndex, String... keys) {
        String value = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(new File(ConfigData.JSON_DATA_FILE_PATH));

            // Lấy item tại index từ mảng gốc
            JsonNode itemNode = rootNode.get(itemIndex);
            if (itemNode == null || !itemNode.isObject()) {
                throw new IllegalArgumentException("Item index không hợp lệ hoặc không phải object.");
            }

            JsonNode current = itemNode;
            for (String key : keys) {
                current = current.path(key);
            }

            value = current.asText();
            System.out.println("Value: " + value);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    // Get value from JSON array in new file path
    public static String getValueJsonArray_FilePath(String filePath, int itemIndex, String... keys) {
        String value = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(new File(filePath));

            // Lấy item tại index từ mảng gốc
            JsonNode itemNode = rootNode.get(itemIndex);
            if (itemNode == null || !itemNode.isObject()) {
                throw new IllegalArgumentException("Item index không hợp lệ hoặc không phải object.");
            }

            JsonNode current = itemNode;
            for (String key : keys) {
                current = current.path(key);
            }

            value = current.asText();
            System.out.println("Value: " + value);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    public static void updateValueJsonObject(String keyName, Number value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(ConfigData.JSON_DATA_FILE_PATH));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            //Update value if exist key
            jsonObject.addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to file
            File jsonFile = new File(ConfigData.JSON_DATA_FILE_PATH);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();
            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonObject(String keyName, String value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(ConfigData.JSON_DATA_FILE_PATH));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            if (jsonObject.has(keyName)) {
                System.out.println("🔁 Update key: '" + keyName + "'");
            } else {
                System.out.println("➕ Add new key: '" + keyName + "'");
            }

            //Update value if exist key
            jsonObject.addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to file
            File jsonFile = new File(ConfigData.JSON_DATA_FILE_PATH);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();
            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonObject(String parentKey, String keyName, Number value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(ConfigData.JSON_DATA_FILE_PATH));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            // Đảm bảo parentKey tồn tại
            if (!jsonObject.has(parentKey) || !jsonObject.get(parentKey).isJsonObject()) {
                jsonObject.add(parentKey, new JsonObject());
            }

            //Update value if exist key
            jsonObject.getAsJsonObject(parentKey).addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to file
            File jsonFile = new File(ConfigData.JSON_DATA_FILE_PATH);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonObject(String parentKey, String keyName, String value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(ConfigData.JSON_DATA_FILE_PATH));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            // Đảm bảo parentKey tồn tại
            if (!jsonObject.has(parentKey) || !jsonObject.get(parentKey).isJsonObject()) {
                jsonObject.add(parentKey, new JsonObject());
            }

            // Gán keyName vào object con
            jsonObject.getAsJsonObject(parentKey).addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to file
            File jsonFile = new File(ConfigData.JSON_DATA_FILE_PATH);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonObject_FilePath(String filePath, String keyName, String value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(filePath));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            // Đảm bảo keyName tồn tại
            if (!jsonObject.has(keyName) || !jsonObject.get(keyName).isJsonObject()) {
                jsonObject.add(keyName, new JsonObject());
            }

            //Update value if exist key
            jsonObject.addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to new file
            File jsonFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();

            //Close reader
            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonObject_FilePath(String filePath, String keyName, Number value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(filePath));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            // Đảm bảo parentKey tồn tại
            if (!jsonObject.has(keyName) || !jsonObject.get(keyName).isJsonObject()) {
                jsonObject.add(keyName, new JsonObject());
            }

            //Update value if exist key
            jsonObject.addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to new file
            File jsonFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();

            //Close reader
            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonObject_FilePath(String filePath, String parentKey, String keyName, String value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(filePath));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            // Đảm bảo parentKey tồn tại
            if (!jsonObject.has(parentKey) || !jsonObject.get(parentKey).isJsonObject()) {
                jsonObject.add(parentKey, new JsonObject());
            }

            //Update value if exist key
            jsonObject.getAsJsonObject(parentKey).addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to file
            File jsonFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonObject_FilePath(String filePath, String parentKey, String keyName, Number value) {
        Reader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(filePath));

            Gson gson = new Gson();
            //Convert Json file to Json Object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println("Original JSON: " + jsonObject);

            // Đảm bảo parentKey tồn tại
            if (!jsonObject.has(parentKey) || !jsonObject.get(parentKey).isJsonObject()) {
                jsonObject.add(parentKey, new JsonObject());
            }

            //Update value if exist key
            jsonObject.getAsJsonObject(parentKey).addProperty(keyName, value);

            System.out.println("Modified JSON: " + jsonObject);

            //Store new Json data to file
            File jsonFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(jsonFile);
            outputStream.write(gson.toJson(jsonObject).getBytes());
            outputStream.flush();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonArray(String value, int index, String... keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một Key");
        }

        try (Reader reader = Files.newBufferedReader(Paths.get(ConfigData.JSON_DATA_FILE_PATH))) {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);

            // Kiểm tra index hợp lệ
            if (index < 0 || index >= jsonArray.size()) {
                throw new IndexOutOfBoundsException("Tham số index nằm ngoài giới hạn.");
            }

            JsonObject current = jsonArray.get(index).getAsJsonObject();

            // Duyệt từ key[0] đến key[n-2], tạo JsonObject nếu chưa có
            for (int i = 0; i < keys.length - 1; i++) {
                String key = keys[i];
                if (!current.has(key) || !current.get(key).isJsonObject()) {
                    current.add(key, new JsonObject());
                }
                current = current.getAsJsonObject(key);
            }

            // Gán giá trị cho key cuối
            current.addProperty(keys[keys.length - 1], value);

            // Ghi lại file
            try (OutputStream outputStream = new FileOutputStream(ConfigData.JSON_DATA_FILE_PATH)) {
                outputStream.write(gson.toJson(jsonArray).getBytes());
                outputStream.flush();
            }

            System.out.println("✅ Updated index " + index + ": " + String.join(" → ", keys) + " = " + value);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateValueJsonArray_FilePath(String filePath, String value, int index, String... keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một Key");
        }

        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);

            // Kiểm tra index hợp lệ
            if (index < 0 || index >= jsonArray.size()) {
                throw new IndexOutOfBoundsException("Tham số index nằm ngoài giới hạn.");
            }

            JsonObject current = jsonArray.get(index).getAsJsonObject();

            // Duyệt từ key[0] đến key[n-2], tạo JsonObject nếu chưa có
            for (int i = 0; i < keys.length - 1; i++) {
                String key = keys[i];
                if (!current.has(key) || !current.get(key).isJsonObject()) {
                    current.add(key, new JsonObject());
                }
                current = current.getAsJsonObject(key);
            }

            // Gán giá trị cho key cuối
            current.addProperty(keys[keys.length - 1], value);

            // Ghi lại file
            try (OutputStream outputStream = new FileOutputStream(filePath)) {
                outputStream.write(gson.toJson(jsonArray).getBytes());
                outputStream.flush();
            }

            System.out.println("✅ Updated index " + index + ": " + String.join(" → ", keys) + " = " + value);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}