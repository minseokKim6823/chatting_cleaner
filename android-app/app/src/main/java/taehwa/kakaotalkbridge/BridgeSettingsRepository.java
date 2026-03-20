package taehwa.kakaotalkbridge;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BridgeSettingsRepository {

    private static final String PREFS_NAME = "bridge_settings";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_ALLOWED_ROOMS = "allowed_rooms";
    private static final String KEY_BOT_NAME = "bot_name";
    private static final String KEY_ENABLED = "enabled";

    public static Settings load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new Settings(
                prefs.getString(KEY_SERVER_URL, ""),
                prefs.getString(KEY_ALLOWED_ROOMS, ""),
                prefs.getString(KEY_BOT_NAME, "경고봇"),
                prefs.getBoolean(KEY_ENABLED, true)
        );
    }

    public static void save(Context context, Settings settings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SERVER_URL, settings.serverUrl())
                .putString(KEY_ALLOWED_ROOMS, settings.allowedRooms())
                .putString(KEY_BOT_NAME, settings.botName())
                .putBoolean(KEY_ENABLED, settings.enabled())
                .apply();
    }

    public static boolean isRoomAllowed(String allowedRoomsRaw, String room) {
        if (allowedRoomsRaw == null || allowedRoomsRaw.isBlank()) {
            return true;
        }

        return splitRooms(allowedRoomsRaw).contains(room.trim());
    }

    private static List<String> splitRooms(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public record Settings(String serverUrl, String allowedRooms, String botName, boolean enabled) {
    }
}
