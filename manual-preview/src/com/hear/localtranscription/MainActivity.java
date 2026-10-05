package com.hear.localtranscription;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView transcript;
    private boolean listening = false;
    private int tick = 0;
    private String language = "English";

    private final Runnable livePreview = new Runnable() {
        @Override
        public void run() {
            if (!listening) return;
            tick++;
            String sample = language.equals("Tamil")
                    ? "உள்ளூர் மாதிரி தயார். நேரடி உரை இங்கே தோன்றும்."
                    : "Local model ready. Live transcript appears here.";
            transcript.setText(sample + "\n\nPreview tick " + tick + ": microphone audio stays on this phone.");
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 28);
        root.setBackgroundColor(0xFFF6F8F7);

        TextView title = new TextView(this);
        title.setText("HEAR");
        title.setTextSize(32);
        title.setTextColor(0xFF13201D);
        root.addView(title);

        status = new TextView(this);
        status.setTextSize(14);
        status.setTextColor(0xFF42524E);
        status.setPadding(0, 8, 0, 18);
        root.addView(status);

        RadioGroup languages = new RadioGroup(this);
        languages.setOrientation(RadioGroup.HORIZONTAL);
        languages.setGravity(Gravity.START);
        RadioButton english = languageButton("English");
        RadioButton tamil = languageButton("Tamil");
        english.setChecked(true);
        languages.addView(english);
        languages.addView(tamil);
        languages.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checked = group.findViewById(checkedId);
                language = checked.getText().toString();
                refreshStatus();
            }
        });
        root.addView(languages);

        Button download = new Button(this);
        download.setText("Download language package");
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Preview: English and Tamil package would download to private phone storage.");
            }
        });
        root.addView(download);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button start = new Button(this);
        start.setText("Start");
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startListening();
            }
        });
        Button stop = new Button(this);
        stop.setText("Stop");
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopListening();
            }
        });
        Button clear = new Button(this);
        clear.setText("Clear");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transcript.setText("Transcript appears here while you speak.");
            }
        });
        actions.addView(start);
        actions.addView(stop);
        actions.addView(clear);
        root.addView(actions);

        transcript = new TextView(this);
        transcript.setTextSize(18);
        transcript.setTextColor(0xFF172320);
        transcript.setPadding(0, 18, 0, 18);
        transcript.setLineSpacing(4, 1);
        transcript.setText("Transcript appears here while you speak.");

        ScrollView scroll = new ScrollView(this);
        scroll.addView(transcript);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
        refreshStatus();
    }

    private RadioButton languageButton(String text) {
        RadioButton button = new RadioButton(this);
        button.setText(text);
        button.setId(text.hashCode());
        return button;
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }
        listening = true;
        refreshStatus();
        handler.post(livePreview);
    }

    private void stopListening() {
        listening = false;
        refreshStatus();
    }

    private void refreshStatus() {
        String state = listening ? "Listening locally" : "Ready";
        status.setText(language + " selected. " + state + ". No audio leaves the device.");
    }
}
