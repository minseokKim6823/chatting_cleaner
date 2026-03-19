/*
 * Kakao group-chat warning bot bridge script
 *
 * Target runtime:
 * - 채팅 자동응답 봇
 * - 메신저봇 계열
 *
 * How it works:
 * 1. 카카오톡 알림으로 들어온 메시지를 response()에서 받음
 * 2. Spring 서버 /api/bridge/auto-reply 로 전달
 * 3. 서버가 위반 여부와 답장 문구를 결정
 * 4. shouldReply=true 이면 단톡방에 자동 경고 답장 전송
 */

var CONFIG = {
    SERVER_URL: "http://192.168.0.10:8080",
    API_PATH: "/api/bridge/auto-reply",
    HEALTH_PATH: "/api/bridge/health",
    REQUEST_TIMEOUT_MS: 3000,
    DUPLICATE_WINDOW_MS: 5000,
    DEBUG: true,

    /*
     * 비워두면 모든 방에서 동작
     * 예: ["우리반 단톡", "가족방"]
     */
    ALLOWED_ROOMS: [],

    /*
     * 봇 본인 이름이나 무시할 발신자 이름
     * 여기에 봇이 사용하는 닉네임을 넣어야 무한응답을 막을 수 있음
     */
    IGNORED_SENDERS: ["경고봇"]
};

var LAST_SEEN = {};

function response(room, msg, sender, isGroupChat, replier, imageDB, packageName, isMultiChat) {
    try {
        if (!isGroupChat) return;
        if (!room || !sender || !msg) return;
        if (!isAllowedRoom(room)) return;
        if (isIgnoredSender(sender)) return;
        if (isDuplicate(room, sender, msg)) return;

        var trimmed = String(msg).trim();
        if (!trimmed) return;

        if (trimmed === "!경고봇상태") {
            var health = requestHealth();
            replier.reply(health ? "[경고봇] 서버 연결 정상" : "[경고봇] 서버 연결 실패");
            return;
        }

        var result = requestDecision(room, sender, trimmed);
        if (!result) return;
        if (!result.shouldReply) return;
        if (!result.replyMessage) return;

        replier.reply(String(result.replyMessage));
    } catch (e) {
        logDebug("response error: " + e);
    }
}

function isAllowedRoom(room) {
    if (!CONFIG.ALLOWED_ROOMS || CONFIG.ALLOWED_ROOMS.length === 0) {
        return true;
    }

    for (var i = 0; i < CONFIG.ALLOWED_ROOMS.length; i++) {
        if (CONFIG.ALLOWED_ROOMS[i] === room) {
            return true;
        }
    }

    return false;
}

function isIgnoredSender(sender) {
    for (var i = 0; i < CONFIG.IGNORED_SENDERS.length; i++) {
        if (CONFIG.IGNORED_SENDERS[i] === sender) {
            return true;
        }
    }

    return false;
}

function isDuplicate(room, sender, msg) {
    var key = room + "||" + sender + "||" + msg;
    var now = java.lang.System.currentTimeMillis();
    var last = LAST_SEEN[key];

    LAST_SEEN[key] = now;

    if (!last) {
        return false;
    }

    return (now - last) < CONFIG.DUPLICATE_WINDOW_MS;
}

function requestHealth() {
    var response = httpRequest("GET", CONFIG.SERVER_URL + CONFIG.HEALTH_PATH, null);
    return response !== null;
}

function requestDecision(room, sender, msg) {
    var body = new org.json.JSONObject();
    body.put("room", room);
    body.put("sender", sender);
    body.put("message", msg);
    body.put("fromBot", false);

    var json = httpRequest("POST", CONFIG.SERVER_URL + CONFIG.API_PATH, body.toString());
    if (json === null) {
        return null;
    }

    if (!json.has("data") || json.isNull("data")) {
        return null;
    }

    return json.getJSONObject("data");
}

function httpRequest(method, url, body) {
    var connection = null;
    var writer = null;
    var reader = null;

    try {
        connection = new java.net.URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(CONFIG.REQUEST_TIMEOUT_MS);
        connection.setReadTimeout(CONFIG.REQUEST_TIMEOUT_MS);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");

        if (method === "POST") {
            connection.setDoOutput(true);
            writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(connection.getOutputStream(), "UTF-8")
            );
            writer.write(body);
            writer.flush();
        }

        var code = connection.getResponseCode();
        var stream = (code >= 200 && code < 300)
            ? connection.getInputStream()
            : connection.getErrorStream();

        if (stream === null) {
            logDebug("empty response stream: " + code);
            return null;
        }

        reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(stream, "UTF-8")
        );

        var sb = new java.lang.StringBuilder();
        var line;
        while ((line = reader.readLine()) !== null) {
            sb.append(line);
        }

        var text = String(sb.toString());
        if (code < 200 || code >= 300) {
            logDebug("http error " + code + ": " + text);
            return null;
        }

        return new org.json.JSONObject(text);
    } catch (e) {
        logDebug("http request failed: " + e);
        return null;
    } finally {
        closeQuietly(reader);
        closeQuietly(writer);
        if (connection !== null) {
            connection.disconnect();
        }
    }
}

function closeQuietly(resource) {
    try {
        if (resource !== null) {
            resource.close();
        }
    } catch (e) {
    }
}

function logDebug(message) {
    if (!CONFIG.DEBUG) {
        return;
    }
    java.lang.System.out.println("[kakao_warning_bot] " + message);
}
