package taehwa.kakaotalkbridge;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

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
                .putString(KEY_SERVER_URL, settings.getServerUrl())
                .putString(KEY_ALLOWED_ROOMS, settings.getAllowedRooms())
                .putString(KEY_BOT_NAME, settings.getBotName())
                .putBoolean(KEY_ENABLED, settings.isEnabled())
                .apply();
    }

    public static boolean isRoomAllowed(String allowedRoomsRaw, String room) {
        if (allowedRoomsRaw == null || allowedRoomsRaw.trim().isEmpty()) {
            return true;
        }

        List<String> allowedRooms = splitRooms(allowedRoomsRaw);
        return allowedRooms.contains(room == null ? "" : room.trim());
    }

    private static List<String> splitRooms(String raw) {
        List<String> rooms = new ArrayList<>();
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                rooms.add(trimmed);
            }
        }
        return rooms;
    }

    public static class Settings {
        private final String serverUrl;
        private final String allowedRooms;
        private final String botName;
        private final boolean enabled;

        public Settings(String serverUrl, String allowedRooms, String botName, boolean enabled) {
            this.serverUrl = serverUrl;
            this.allowedRooms = allowedRooms;
            this.botName = botName;
            this.enabled = enabled;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public String getAllowedRooms() {
            return allowedRooms;
        }

        public String getBotName() {
            return botName;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
