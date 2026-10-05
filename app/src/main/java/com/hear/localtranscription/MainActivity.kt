package com.hear.localtranscription

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var store: TranscriptStore
    private lateinit var packs: LanguagePackManager
    private lateinit var transcriptView: TextView
    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = TranscriptStore(this)
        packs = LanguagePackManager(this)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
            setBackgroundColor(0xFFF6F8F7.toInt())
        }

        root.addView(TextView(this).apply {
            text = "HEAR"
            textSize = 32f
            setTextColor(0xFF13201D.toInt())
        })

        statusView = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF42524E.toInt())
            setPadding(0, 8, 0, 18)
        }
        root.addView(statusView)

        val languageGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            gravity = Gravity.START
        }
        Language.entries.forEach { language ->
            languageGroup.addView(RadioButton(this).apply {
                text = language.displayName
                id = View.generateViewId()
                tag = language.code
                isChecked = store.language == language
            })
        }
        languageGroup.setOnCheckedChangeListener { group, checkedId ->
            val checked = group.findViewById<RadioButton>(checkedId)
            store.language = Language.fromCode(checked.tag.toString())
            TranscriptWidgetProvider.updateAll(this)
            refresh()
        }
        root.addView(languageGroup)

        root.addView(Button(this).apply {
            text = "Download language package"
            setOnClickListener {
                packs.downloadMultilingualPack()
                refresh()
            }
        })

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }
        buttonRow.addView(Button(this).apply {
            text = "Start"
            setOnClickListener { startTranscription() }
        })
        buttonRow.addView(Button(this).apply {
            text = "Stop"
            setOnClickListener { stopService(Intent(this@MainActivity, TranscriptionService::class.java)) }
        })
        buttonRow.addView(Button(this).apply {
            text = "Clear"
            setOnClickListener {
                store.clear()
                TranscriptWidgetProvider.updateAll(this@MainActivity)
                refresh()
            }
        })
        root.addView(buttonRow)

        transcriptView = TextView(this).apply {
            textSize = 18f
            setTextColor(0xFF172320.toInt())
            setPadding(0, 18, 0, 18)
            setLineSpacing(4f, 1f)
        }
        val transcriptScroll = ScrollView(this).apply {
            addView(transcriptView)
        }
        root.addView(
            transcriptScroll,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f),
        )

        setContentView(root)
    }

    private fun startTranscription() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
            return
        }
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 11)
            return
        }
        startForegroundService(Intent(this, TranscriptionService::class.java))
    }

    private fun refresh() {
        statusView.text = "${store.language.displayName} selected. ${packs.installedStatus()}"
        transcriptView.text = store.latestText.ifBlank { "Transcript appears here while you speak." }
    }
}
