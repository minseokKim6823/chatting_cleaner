package taehwa.kakaotalkbridge;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BridgeHttpClient {

    private static final int TIMEOUT_MS = 3000;

    public static boolean healthCheck(String serverUrl) {
        return request("GET", normalize(serverUrl) + "/api/bridge/health", null) != null;
    }

    public static Decision requestDecision(String serverUrl, String room, String sender, String message) {
        try {
            JSONObject body = new JSONObject();
            body.put("room", room);
            body.put("sender", sender);
            body.put("message", message);
            body.put("fromBot", false);

            JSONObject json = request("POST", normalize(serverUrl) + "/api/bridge/auto-reply", body.toString());
            if (json == null || json.isNull("data")) {
                return null;
            }

            JSONObject data = json.getJSONObject("data");
            return new Decision(
                    data.optBoolean("shouldReply", false),
                    data.optString("replyMessage", ""),
                    data.optString("violationType", ""),
                    data.optString("restrictionLevel", ""),
                    data.optString("reason", "")
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject request(String method, String urlString, String body) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            if ("POST".equals(method) && body != null) {
                connection.setDoOutput(true);
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(body);
                }
            }

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream == null) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            if (code < 200 || code >= 300) {
                return null;
            }
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String normalize(String serverUrl) {
        if (serverUrl == null) {
            return "";
        }
        return serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
    }

    public static class Decision {
        private final boolean shouldReply;
        private final String replyMessage;
        private final String violationType;
        private final String restrictionLevel;
        private final String reason;

        public Decision(boolean shouldReply, String replyMessage, String violationType, String restrictionLevel, String reason) {
            this.shouldReply = shouldReply;
            this.replyMessage = replyMessage;
            this.violationType = violationType;
            this.restrictionLevel = restrictionLevel;
            this.reason = reason;
        }

        public boolean isShouldReply() {
            return shouldReply;
        }

        public String getReplyMessage() {
            return replyMessage;
        }

        public String getViolationType() {
            return violationType;
        }

        public String getRestrictionLevel() {
            return restrictionLevel;
        }

        public String getReason() {
            return reason;
        }
    }
}
