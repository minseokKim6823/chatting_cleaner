package taehwa.kakaotalkbridge;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.RemoteInput;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KakaoNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "KakaoBridge";
    private static final String KAKAO_PACKAGE = "com.kakao.talk";
    private static final long DUPLICATE_WINDOW_MS = 5000L;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, Long> recentMessages = new ConcurrentHashMap<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || !KAKAO_PACKAGE.equals(sbn.getPackageName())) {
            return;
        }

        Notification notification = sbn.getNotification();
        if (notification == null || notification.extras == null) {
            return;
        }

        BridgeSettingsRepository.Settings settings = BridgeSettingsRepository.load(this);
        if (!settings.enabled() || settings.serverUrl().isBlank()) {
            return;
        }

        ParsedMessage parsedMessage = parse(notification.extras);
        if (parsedMessage == null) {
            return;
        }

        if (!BridgeSettingsRepository.isRoomAllowed(settings.allowedRooms(), parsedMessage.room())) {
            return;
        }

        if (!settings.botName().isBlank() && settings.botName().equals(parsedMessage.sender())) {
            return;
        }

        Notification.Action replyAction = findReplyAction(notification);
        if (replyAction == null) {
            return;
        }

        if (isDuplicate(parsedMessage)) {
            return;
        }

        executor.execute(() -> {
            BridgeHttpClient.Decision decision = BridgeHttpClient.requestDecision(
                    settings.serverUrl(),
                    parsedMessage.room(),
                    parsedMessage.sender(),
                    parsedMessage.message()
            );

            if (decision == null || !decision.shouldReply() || decision.replyMessage().isBlank()) {
                return;
            }

            sendReply(replyAction, decision.replyMessage());
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private ParsedMessage parse(Bundle extras) {
        String room = firstNonBlank(
                charSequenceToString(extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)),
                charSequenceToString(extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
        );
        String sender = charSequenceToString(extras.getCharSequence(Notification.EXTRA_TITLE));
        String message = firstNonBlank(
                charSequenceToString(extras.getCharSequence(Notification.EXTRA_TEXT)),
                charSequenceToString(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
        );

        if (isBlank(room) || isBlank(sender) || isBlank(message)) {
            return null;
        }

        return new ParsedMessage(room.trim(), sender.trim(), message.trim());
    }

    private Notification.Action findReplyAction(Notification notification) {
        if (notification.actions == null) {
            return null;
        }

        for (Notification.Action action : notification.actions) {
            if (action != null
                    && action.actionIntent != null
                    && action.getRemoteInputs() != null
                    && action.getRemoteInputs().length > 0) {
                return action;
            }
        }
        return null;
    }

    private void sendReply(Notification.Action action, String replyMessage) {
        try {
            RemoteInput[] remoteInputs = action.getRemoteInputs();
            if (remoteInputs == null || remoteInputs.length == 0) {
                return;
            }

            Intent intent = new Intent();
            Bundle results = new Bundle();
            for (RemoteInput remoteInput : remoteInputs) {
                results.putCharSequence(remoteInput.getResultKey(), replyMessage);
            }

            RemoteInput.addResultsToIntent(remoteInputs, intent, results);
            action.actionIntent.send(this, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Log.w(TAG, "reply pending intent canceled", e);
        } catch (Exception e) {
            Log.w(TAG, "failed to send reply", e);
        }
    }

    private boolean isDuplicate(ParsedMessage parsedMessage) {
        String key = parsedMessage.room() + "|" + parsedMessage.sender() + "|" + parsedMessage.message();
        long now = System.currentTimeMillis();
        Long last = recentMessages.put(key, now);
        return last != null && now - last < DUPLICATE_WINDOW_MS;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String charSequenceToString(CharSequence value) {
        return value == null ? "" : value.toString();
    }

    private record ParsedMessage(String room, String sender, String message) {
    }
}
