package util;

import java.io.*;
import java.util.Properties;

public class Config {

    private static final String CONFIG_FILE = "bot.properties";
    private static final Properties props = new Properties();

    // 파일 로딩 (최초 한 번)
    static {
        load();
    }

    /** 설정 파일을 로드 */
    private static void load() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;  // 파일이 없으면 생략

        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("⚠️ 설정 파일 로딩 중 오류 발생:");
            e.printStackTrace();
        }
    }

    /** 설정을 저장 */
    public static void save() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Bot Configuration");
        } catch (IOException e) {
            System.err.println("⚠️ 설정 저장 중 오류 발생:");
            e.printStackTrace();
        }
    }

    /** 설정값 가져오기 */
    public static String get(String key) {
        return props.getProperty(key);
    }

    /** 기본값과 함께 설정값 가져오기 */
    public static String getOrDefault(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    /** 설정값 저장 */
    public static void set(String key, String value) {
        props.setProperty(key, value);
        save();
    }

    /** 설정값 제거 */
    public static void remove(String key) {
        props.remove(key);
        save();
    }
}
