package taehwa.kakaotalkbridge;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText serverUrlEditText;
    private EditText allowedRoomsEditText;
    private EditText botNameEditText;
    private CheckBox enabledCheckBox;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUrlEditText = findViewById(R.id.serverUrlEditText);
        allowedRoomsEditText = findViewById(R.id.allowedRoomsEditText);
        botNameEditText = findViewById(R.id.botNameEditText);
        enabledCheckBox = findViewById(R.id.enabledCheckBox);
        statusTextView = findViewById(R.id.statusTextView);
        Button saveButton = findViewById(R.id.saveButton);
        Button openListenerSettingsButton = findViewById(R.id.openListenerSettingsButton);
        Button healthCheckButton = findViewById(R.id.healthCheckButton);

        loadSettings();

        saveButton.setOnClickListener(v -> {
            saveSettings();
            Toast.makeText(this, R.string.saved_message, Toast.LENGTH_SHORT).show();
        });

        openListenerSettingsButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        healthCheckButton.setOnClickListener(v -> {
            saveSettings();
            statusTextView.setText(R.string.status_checking);
            executor.execute(() -> {
                boolean ok = BridgeHttpClient.healthCheck(serverUrlEditText.getText().toString().trim());
                runOnUiThread(() -> statusTextView.setText(
                        ok ? R.string.status_server_ok : R.string.status_server_fail
                ));
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void loadSettings() {
        BridgeSettingsRepository.Settings settings = BridgeSettingsRepository.load(this);
        serverUrlEditText.setText(settings.getServerUrl());
        allowedRoomsEditText.setText(settings.getAllowedRooms());
        botNameEditText.setText(settings.getBotName());
        enabledCheckBox.setChecked(settings.isEnabled());
    }

    private void saveSettings() {
        BridgeSettingsRepository.save(this, new BridgeSettingsRepository.Settings(
                serverUrlEditText.getText().toString().trim(),
                allowedRoomsEditText.getText().toString().trim(),
                botNameEditText.getText().toString().trim(),
                enabledCheckBox.isChecked()
        ));
    }
}
