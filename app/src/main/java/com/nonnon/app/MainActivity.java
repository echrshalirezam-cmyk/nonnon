package com.nonnon.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * nonnon - offline-first English learning app.
 * The first release uses native Android views to stay light and compatible with Android 9.
 */
public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int BG = Color.rgb(255, 248, 252);
    private static final int CARD = Color.WHITE;
    private static final int INK = Color.rgb(54, 38, 63);
    private static final int MUTED = Color.rgb(133, 116, 139);
    private static final int PINK = Color.rgb(232, 75, 157);
    private static final int PURPLE = Color.rgb(143, 91, 197);
    private static final int GREEN = Color.rgb(88, 184, 137);
    private static final int PALE_PINK = Color.rgb(255, 232, 244);
    private static final int PALE_PURPLE = Color.rgb(243, 235, 252);
    private static final String[] LEVELS = {"A1", "A2", "B1", "B2", "C1", "C2"};
    private static final String[] LEVEL_NAMES = {
            "شروع‌کننده", "پایه", "متوسط", "بالاتر از متوسط", "پیشرفته", "تسلط کامل"
    };

    private LinearLayout root;
    private LinearLayout content;
    private String page = "home";
    private String selectedLevel = "A1";
    private int selectedUnit = 0;
    private int selectedLesson = 0;
    private int questionIndex = 0;
    private int score = 0;
    private boolean practiceMode = false;
    private String selectedAnswer = null;
    private List<Question> questions = new ArrayList<>();
    private Map<String, List<Word>> decks = new HashMap<>();
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private android.content.SharedPreferences prefs;

    private static class Word {
        final String en, fa, example, pronunciation;
        Word(String en, String fa, String example, String pronunciation) {
            this.en = en; this.fa = fa; this.example = example; this.pronunciation = pronunciation;
        }
    }

    private static class Question {
        final Word word;
        final String prompt;
        final String answer;
        final List<String> options;
        Question(Word word, String prompt, String answer, List<String> options) {
            this.word = word; this.prompt = prompt; this.answer = answer; this.options = options;
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        prefs = getSharedPreferences("nonnon_progress", MODE_PRIVATE);
        seedWords();
        tts = new TextToSpeech(this, this);
        render();
    }

    @Override public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true;
            if (tts != null) tts.setLanguage(Locale.US);
        }
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    private void seedWords() {
        decks.put("A1", words(
                "hello|سلام|Hello, Sara!|heh-LOH",
                "apple|سیب|I eat an apple.|AP-uhl",
                "book|کتاب|This is my book.|book",
                "water|آب|I drink water.|WAW-ter",
                "family|خانواده|My family is kind.|FAM-uh-lee",
                "house|خانه|Our house is small.|hows",
                "school|مدرسه|I go to school.|skool",
                "friend|دوست|Ali is my friend.|frend",
                "happy|خوشحال|I am happy today.|HAP-ee",
                "eat|خوردن|We eat lunch at noon.|eet",
                "read|خواندن|I read every day.|reed",
                "morning|صبح|Good morning!|MOR-ning",
                "beautiful|زیبا|It is a beautiful day.|BYOO-tuh-ful",
                "small|کوچک|The bag is small.|smawl",
                "teacher|معلم|My teacher is nice.|TEE-cher",
                "water|آب|Please drink some water.|WAW-ter"
        ));
        decks.put("A2", words(
                "usually|معمولاً|I usually walk to work.|YOO-zhoo-uh-lee",
                "travel|سفر کردن|We travel every summer.|TRAV-uhl",
                "ticket|بلیط|I bought a train ticket.|TIK-it",
                "weather|آب‌وهوا|The weather is warm.|WETH-er",
                "market|بازار|She works at the market.|MAR-kit",
                "yesterday|دیروز|I called him yesterday.|YES-ter-day",
                "healthy|سالم|Fruit is healthy.|HEL-thee",
                "invite|دعوت کردن|I will invite my friend.|in-VYTE",
                "arrive|رسیدن|They arrive at six.|uh-RYVE",
                "quickly|سریع|Please walk quickly.|KWIK-lee",
                "enough|کافی|We have enough time.|ih-NUF",
                "forget|فراموش کردن|Do not forget your keys.|fer-GET"
        ));
        decks.put("B1", words(
                "improve|بهبود دادن|Practice will improve your English.|im-PROOV",
                "advice|توصیه|Thank you for your advice.|ad-VYSS",
                "decision|تصمیم|It was a difficult decision.|dih-SIZH-un",
                "experience|تجربه|It was a good experience.|ik-SPEER-ee-uhns",
                "although|اگرچه|Although it rained, we went out.|awl-THOH",
                "probably|احتمالاً|She will probably call.|PROB-uh-blee",
                "achieve|دست یافتن|You can achieve your goal.|uh-CHEEV",
                "explain|توضیح دادن|Please explain the rule.|ik-SPLAYN",
                "environment|محیط زیست|We should protect the environment.|en-VY-run-ment",
                "opportunity|فرصت|This is a great opportunity.|op-er-TOO-ni-tee",
                "relationship|رابطه|They have a strong relationship.|rih-LAY-shun-ship",
                "prepare|آماده شدن|I need to prepare for the test.|prih-PAIR"
        ));
        decks.put("B2", words(
                "consider|در نظر گرفتن|Please consider my idea.|kun-SID-er",
                "approach|رویکرد|We need a new approach.|uh-PROHCH",
                "evidence|مدرک|There is no clear evidence.|EV-ih-dens",
                "challenge|چالش|Learning is a challenge.|CHAL-inj",
                "contribute|مشارکت کردن|Everyone can contribute.|kun-TRIB-yoot",
                "influence|تأثیر|Music has a strong influence.|IN-floo-ens",
                "maintain|حفظ کردن|Maintain a healthy routine.|mayn-TAYN",
                "significant|قابل توجه|There was a significant change.|sig-NIF-ih-kunt",
                "require|نیاز داشتن|This task will require time.|rih-KWY-er",
                "issue|موضوع|We discussed the main issue.|ISH-oo",
                "indicate|نشان دادن|The signs indicate danger.|IN-dih-kayt",
                "benefit|مزیت|Exercise has many benefits.|BEN-ih-fit"
        ));
        decks.put("C1", words(
                "comprehensive|جامع|The report is comprehensive.|kom-prih-HEN-siv",
                "sophisticated|پیچیده و پیشرفته|It is a sophisticated system.|suh-FIS-tih-kay-tid",
                "inevitable|اجتناب‌ناپذیر|Change is inevitable.|in-EV-ih-tuh-bul",
                "perspective|دیدگاه|Try to see my perspective.|per-SPEK-tiv",
                "substantial|قابل ملاحظه|They made substantial progress.|sub-STAN-shul",
                "ambiguous|دوپهلو|The message was ambiguous.|am-BIG-yoo-us",
                "coherent|منسجم|Write a coherent paragraph.|koh-HEER-unt",
                "formulate|صورت‌بندی کردن|Formulate a clear plan.|FOR-myoo-layt",
                "reinforce|تقویت کردن|Practice reinforces memory.|ree-in-FORSS",
                "interpret|تفسیر کردن|How do you interpret this?|in-TER-prit",
                "acquire|به دست آوردن|Children acquire language quickly.|uh-KWY-er",
                "relevant|مرتبط|Please share relevant details.|REL-uh-vunt"
        ));
        decks.put("C2", words(
                "ubiquitous|همه‌جا حاضر|Smartphones are ubiquitous.|yoo-BIK-wih-tus",
                "meticulous|بسیار دقیق|She is meticulous about details.|muh-TIK-yuh-lus",
                "pragmatic|عمل‌گرا|We need a pragmatic solution.|prag-MAT-ik",
                "alleviate|تسکین دادن|This may alleviate the pain.|uh-LEE-vee-ayt",
                "discern|تشخیص دادن|It is hard to discern the truth.|dih-SERN",
                "elaborate|مفصل|Please give an elaborate answer.|ih-LAB-uh-rut",
                "nuanced|دارای ظرافت معنایی|It is a nuanced argument.|NOO-ahnst",
                "resilient|تاب‌آور|A resilient person adapts.|rih-ZIL-yunt",
                "paradigm|الگوواره|The discovery changed the paradigm.|PAIR-uh-dym",
                "concise|مختصر و مفید|Keep your answer concise.|kun-SYSS",
                "plausible|باورپذیر|That sounds plausible.|PLAW-zuh-bul",
                "eloquent|فصیح|He gave an eloquent speech.|EL-uh-kwent"
        ));
    }

    private List<Word> words(String... raw) {
        List<Word> out = new ArrayList<>();
        for (String line : raw) {
            String[] p = line.split("\\|", 4);
            out.add(new Word(p[0], p[1], p[2], p[3]));
        }
        return out;
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setLineSpacing(dp(2), 1.0f);
        return t;
    }

    private void pad(View v, int left, int top, int right, int bottom) {
        v.setPadding(dp(left), dp(top), dp(right), dp(bottom));
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void gap(LinearLayout parent, int height) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private LinearLayout panel() {
        LinearLayout p = vertical();
        pad(p, 16, 15, 16, 15);
        p.setBackground(shape(CARD, Color.rgb(246, 225, 238), 20));
        p.setElevation(dp(2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(13);
        content.addView(p, lp);
        return p;
    }

    private Button button(String title, boolean primary, Runnable action) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? Color.WHITE : PURPLE);
        b.setBackground(shape(primary ? PINK : PALE_PURPLE, primary ? PINK : 0, 15));
        b.setMinHeight(dp(48));
        b.setPadding(dp(18), dp(8), dp(18), dp(8));
        b.setElevation(primary ? dp(2) : 0);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        returnButtonHack(b, lp);
        return b;
    }

    private void returnButtonHack(Button b, LinearLayout.LayoutParams lp) {
        // Layout parameters are applied by addButton; keeping this helper makes button creation uniform.
    }

    private void addButton(LinearLayout parent, String title, boolean primary, Runnable action) {
        Button b = button(title, primary, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        parent.addView(b, lp);
    }

    private void addText(LinearLayout parent, String value, int size, int color, boolean bold) {
        TextView t = text(value, size, color, bold);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(5);
        parent.addView(t, lp);
    }

    private void addChip(LinearLayout parent, String value, int color) {
        TextView chip = text(value, 13, color, true);
        chip.setGravity(Gravity.CENTER);
        pad(chip, 13, 8, 13, 8);
        chip.setBackground(shape(color == PINK ? PALE_PINK : PALE_PURPLE, 0, 40));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        parent.addView(chip, lp);
    }

    private void render() {
        root = vertical();
        root.setBackgroundColor(BG);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(root);

        LinearLayout header = horizontal();
        header.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        pad(header, 13, 7, 13, 7);
        TextView flag = text("🇬🇧", 22, INK, true);
        flag.setGravity(Gravity.CENTER);
        header.addView(flag, new LinearLayout.LayoutParams(0, dp(46), 1));
        TextView crown = text("👑 " + Math.max(0, xp() / 10), 15, Color.rgb(229, 164, 35), true);
        crown.setGravity(Gravity.CENTER);
        header.addView(crown, new LinearLayout.LayoutParams(0, dp(46), 1));
        TextView fire = text("🔥 " + streak(), 15, Color.rgb(242, 139, 38), true);
        fire.setGravity(Gravity.CENTER);
        header.addView(fire, new LinearLayout.LayoutParams(0, dp(46), 1));
        TextView heart = text("❤️ " + hearts(), 15, PINK, true);
        heart.setGravity(Gravity.CENTER);
        header.addView(heart, new LinearLayout.LayoutParams(0, dp(46), 1));
        TextView settings = text("⚙", 25, INK, true);
        settings.setGravity(Gravity.CENTER);
        settings.setOnClickListener(v -> { page = "settings"; render(); });
        header.addView(settings, new LinearLayout.LayoutParams(0, dp(46), 1));
        root.addView(header);

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(246, 225, 238));
        root.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = vertical();
        pad(content, 17, 16, 17, 18);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        switch (page) {
            case "home": renderHome(); break;
            case "path": renderPath(); break;
            case "units": renderUnits(); break;
            case "lessons": renderLessons(); break;
            case "quiz": renderQuiz(); break;
            case "done": renderDone(); break;
            case "practice": renderPractice(); break;
            case "words": renderWords(); break;
            case "profile": renderProfile(); break;
            case "leaderboard": renderLeaderboard(); break;
            case "rewards": renderRewards(); break;
            case "settings": renderSettings(); break;
            default: renderHome();
        }
        renderNav();
    }

    private void renderNav() {
        LinearLayout nav = horizontal();
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(8));
        pad(nav, 3, 5, 3, 5);
        String[][] items = {
                {"⌂", "خانه", "home"}, {"📖", "درس‌ها", "path"},
                {"🏆", "جدول رتبه", "leaderboard"}, {"🛡", "پاداش‌ها", "rewards"},
                {"●", "حساب کاربری", "profile"}
        };
        for (String[] item : items) {
            LinearLayout cell = vertical();
            cell.setGravity(Gravity.CENTER);
            int active = page.equals(item[2]) ? PINK : MUTED;
            TextView symbol = text(item[0], 22, active, true);
            symbol.setGravity(Gravity.CENTER);
            TextView label = text(item[1], 10, active, true);
            label.setGravity(Gravity.CENTER);
            cell.addView(symbol);
            cell.addView(label);
            if (page.equals(item[2])) {
                View marker = new View(this);
                marker.setBackground(shape(PINK, 0, 5));
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(dp(28), dp(3));
                mp.topMargin = dp(3);
                cell.addView(marker, mp);
            }
            cell.setOnClickListener(v -> { page = item[2]; render(); });
            nav.addView(cell, new LinearLayout.LayoutParams(0, dp(54), 1));
        }
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(62)));
    }

    private void title(String heading, String subtitle) {
        addText(content, heading, 23, INK, true);
        if (subtitle != null) addText(content, subtitle, 13, MUTED, false);
        gap(content, 12);
    }

    private void renderHome() {
        // Approved nonnon home reference: girl mascot, pink welcome panel and vertical lesson path.
        LinearLayout hero = panel();
        hero.setBackground(shape(Color.rgb(255, 224, 241), 0, 24));
        hero.setPadding(dp(10), dp(10), dp(10), dp(10));
        LinearLayout heroRow = horizontal();
        heroRow.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ImageView mascot = new ImageView(this);
        mascot.setImageResource(R.drawable.nonnon_mascot);
        mascot.setScaleType(ImageView.ScaleType.FIT_CENTER);
        heroRow.addView(mascot, new LinearLayout.LayoutParams(dp(126), dp(145)));
        LinearLayout copy = vertical();
        copy.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        copy.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        addText(copy, "آماده‌ای برای درس امروز؟", 18, INK, true);
        addText(copy, "با یک درس کوتاه،\nزبان انگلیسی را بهتر یاد بگیر!", 13, MUTED, false);
        addButton(copy, "▶  شروع درس", true, () -> openNextLesson());
        heroRow.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        hero.addView(heroRow);

        gap(content, 13);
        LinearLayout section = horizontal();
        TextView heading = text("مسیر یادگیری", 19, INK, true);
        section.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        TextView all = text("مشاهده همه  ←", 12, PINK, true);
        all.setGravity(Gravity.CENTER);
        all.setOnClickListener(v -> { page = "path"; render(); });
        section.addView(all);
        content.addView(section);
        gap(content, 8);

        addHomeLessonNode(0, "معرفی خود", "📖", true);
        addPathConnector();
        addHomeLessonNode(1, "در محل کار", "🔒", prefs.getBoolean(doneKey("A1", 0, 0), false));
        addPathConnector();
        addHomeLessonNode(2, "فعالیت‌های روزمره", "🎧", prefs.getBoolean(doneKey("A1", 0, 1), false));

        gap(content, 10);
        LinearLayout daily = panel();
        addText(daily, "هدف روزانه 🌟", 16, INK, true);
        int goal = prefs.getInt("daily_goal", 20);
        int today = prefs.getInt(todayKey(), 0);
        addText(daily, today + " از " + goal + " امتیاز XP", 12, MUTED, false);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(goal);
        bar.setProgress(Math.min(today, goal));
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(PINK));
        bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(255, 226, 240)));
        daily.addView(bar, new LinearLayout.LayoutParams(-1, dp(9)));
    }

    private void addHomeLessonNode(int lesson, String label, String symbol, boolean unlocked) {
        LinearLayout row = horizontal();
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView node = text(symbol, 30, unlocked ? Color.WHITE : Color.rgb(139, 143, 163), true);
        node.setGravity(Gravity.CENTER);
        node.setBackground(shape(unlocked ? GREEN : Color.rgb(235, 236, 244),
                unlocked ? Color.rgb(255, 220, 80) : Color.rgb(220, 222, 232), 60));
        LinearLayout.LayoutParams nodeLp = new LinearLayout.LayoutParams(dp(94), dp(94));
        nodeLp.rightMargin = dp(12);
        row.addView(node, nodeLp);

        LinearLayout info = vertical();
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        info.setPadding(dp(14), dp(11), dp(14), dp(11));
        info.setBackground(shape(unlocked ? Color.rgb(226, 249, 224) : Color.rgb(242, 242, 248), 0, 20));
        addText(info, "درس " + (lesson + 1), 15, unlocked ? INK : MUTED, true);
        addText(info, label, 14, unlocked ? INK : MUTED, true);
        addText(info, unlocked ? "⭐  " + (prefs.getBoolean(doneKey("A1", 0, lesson), false) ? "5 / 5" : "شروع نشده") : "☆  0 / 5",
                12, unlocked ? Color.rgb(224, 164, 25) : MUTED, true);
        row.addView(info, new LinearLayout.LayoutParams(0, dp(90), 1));
        row.setOnClickListener(v -> {
            if (!unlocked) {
                Toast.makeText(this, "برای باز شدن این درس، درس قبلی را کامل کن.", Toast.LENGTH_SHORT).show();
                return;
            }
            startLesson("A1", 0, lesson, false);
        });
        content.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addPathConnector() {
        TextView dots = text("⋮\n⋮\n⋮", 18, Color.rgb(197, 190, 211), true);
        dots.setGravity(Gravity.CENTER);
        content.addView(dots, new LinearLayout.LayoutParams(dp(94), dp(25)));
    }

    private void renderPath() {
        title("مسیر یادگیری", "از پایه تا تسلط؛ مسیر مخصوص خودت را انتخاب کن.");
        for (int i = 0; i < LEVELS.length; i++) {
            final String level = LEVELS[i];
            int completed = completedInLevel(level);
            LinearLayout card = panel();
            LinearLayout row = horizontal();
            TextView badge = text(level, 21, Color.WHITE, true);
            badge.setGravity(Gravity.CENTER);
            badge.setBackground(shape(i % 2 == 0 ? PINK : PURPLE, 0, 16));
            row.addView(badge, new LinearLayout.LayoutParams(dp(58), dp(58)));
            LinearLayout info = vertical();
            info.setPadding(dp(12), 0, 0, 0);
            info.addView(text(level + " · " + LEVEL_NAMES[i], 16, INK, true));
            info.addView(text(completed + " درس از ۵۰ درس تکمیل شده", 12, MUTED, false));
            row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(row);
            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(50); bar.setProgress(completed);
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(i % 2 == 0 ? PINK : PURPLE));
            card.addView(bar, new LinearLayout.LayoutParams(-1, dp(7)));
            addButton(card, "ورود به سطح " + level + "  ←", false, () -> {
                selectedLevel = level; page = "units"; render();
            });
        }
    }

    private void renderUnits() {
        title("سطح " + selectedLevel, LEVEL_NAMES[indexOfLevel(selectedLevel)] + " · ده بخش آموزشی");
        addButton(content, "← بازگشت به مسیر", false, () -> { page = "path"; render(); });
        for (int i = 0; i < 10; i++) {
            final int unit = i;
            LinearLayout card = panel();
            int done = 0;
            for (int j = 0; j < 5; j++) if (prefs.getBoolean(doneKey(selectedLevel, i, j), false)) done++;
            addText(card, "بخش " + (i + 1) + "  ·  " + unitName(i), 16, INK, true);
            addText(card, done + " از ۵ درس تکمیل شده", 12, MUTED, false);
            addButton(card, "مشاهده درس‌ها", false, () -> {
                selectedUnit = unit; page = "lessons"; render();
            });
        }
    }

    private String unitName(int index) {
        String[] names = {"سلام و معرفی", "خانواده و دوستان", "خانه و مدرسه", "خوراکی‌ها", "زمان و روزها",
                "کارهای روزانه", "شهر و مسیرها", "احساسات", "خرید و گفتگو", "مرور و جمع‌بندی"};
        return names[index % names.length];
    }

    private void renderLessons() {
        title("بخش " + (selectedUnit + 1), "سطح " + selectedLevel + " · " + unitName(selectedUnit));
        addButton(content, "← بازگشت به بخش‌ها", false, () -> { page = "units"; render(); });
        for (int i = 0; i < 5; i++) {
            final int lesson = i;
            boolean done = prefs.getBoolean(doneKey(selectedLevel, selectedUnit, i), false);
            LinearLayout card = panel();
            LinearLayout row = horizontal();
            TextView badge = text(done ? "✓" : String.valueOf(i + 1), 19, done ? GREEN : PINK, true);
            badge.setGravity(Gravity.CENTER);
            badge.setBackground(shape(done ? Color.rgb(231, 249, 239) : PALE_PINK, 0, 40));
            row.addView(badge, new LinearLayout.LayoutParams(dp(42), dp(42)));
            LinearLayout name = vertical();
            name.setPadding(dp(10), 0, 0, 0);
            name.addView(text("درس " + (i + 1), 15, INK, true));
            name.addView(text(done ? "تکمیل شده · امکان مرور دوباره" : "۵ پرسش کوتاه · حدود ۳ دقیقه", 12, MUTED, false));
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(row);
            addButton(card, done ? "مرور دوباره" : "شروع درس  ←", true,
                    () -> startLesson(selectedLevel, selectedUnit, lesson, false));
        }
    }

    private void startLesson(String level, int unit, int lesson, boolean practice) {
        if (hearts() <= 0 && !practice) {
            page = "done"; selectedAnswer = "برای ادامه، قلب‌هایت را با استراحت دوباره پر کن.";
            render(); return;
        }
        selectedLevel = level; selectedUnit = unit; selectedLesson = lesson;
        practiceMode = practice;
        questionIndex = 0; score = 0; selectedAnswer = null;
        questions = buildQuestions(level, unit, lesson);
        page = "quiz"; render();
    }

    private List<Question> buildQuestions(String level, int unit, int lesson) {
        List<Word> bank = decks.get(level);
        List<Question> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Word w = bank.get(Math.floorMod(unit * 3 + lesson * 2 + i * 2, bank.size()));
            boolean toPersian = (i + lesson) % 2 == 0;
            String correct = toPersian ? w.fa : w.en;
            List<String> options = new ArrayList<>();
            options.add(correct);
            for (int j = 1; j <= 3; j++) {
                Word other = bank.get((bank.indexOf(w) + j * 3) % bank.size());
                String choice = toPersian ? other.fa : other.en;
                if (!options.contains(choice)) options.add(choice);
            }
            while (options.size() < 4) options.add(bank.get(options.size()).fa);
            Collections.shuffle(options);
            String prompt = toPersian
                    ? "معنی این کلمه چیست؟\n" + w.en
                    : "معادل انگلیسی این واژه چیست؟\n" + w.fa;
            result.add(new Question(w, prompt, correct, options));
        }
        return result;
    }

    private void renderQuiz() {
        addButton(content, "×  خروج از تمرین", false, () -> {
            new AlertDialog.Builder(this).setMessage("از این تمرین خارج می‌شوی؟")
                    .setNegativeButton("ادامه", (d, w) -> {}).setPositiveButton("خروج", (d, w) -> {
                        page = practiceMode ? "practice" : "lessons"; render();
                    }).show();
        });
        LinearLayout status = horizontal();
        TextView step = text("پرسش " + (questionIndex + 1) + " از " + questions.size(), 13, MUTED, true);
        status.addView(step, new LinearLayout.LayoutParams(0, -2, 1));
        status.addView(text("❤️ " + hearts(), 14, PINK, true));
        content.addView(status);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(questions.size()); bar.setProgress(questionIndex);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(PINK));
        content.addView(bar, new LinearLayout.LayoutParams(-1, dp(9)));
        gap(content, 24);

        Question q = questions.get(questionIndex);
        LinearLayout card = panel();
        card.setBackground(shape(Color.WHITE, Color.rgb(245, 219, 237), 22));
        addText(card, q.prompt, 20, INK, true);
        if (selectedAnswer == null) {
            addButton(card, "🔊  شنیدن تلفظ  " + q.word.en, false, () -> speak(q.word.en));
            for (String option : q.options) {
                addButton(card, option, false, () -> chooseAnswer(option));
            }
        } else {
            boolean correct = selectedAnswer.equals(q.answer);
            addText(card, correct ? "آفرین! پاسخ درست بود 🎉" : "پاسخ درست: " + q.answer,
                    17, correct ? GREEN : PINK, true);
            addText(card, q.word.example, 14, MUTED, false);
            addButton(card, "🔊  شنیدن جمله", false, () -> speak(q.word.example));
            addButton(card, questionIndex == questions.size() - 1 ? "پایان تمرین" : "ادامه  ←",
                    true, () -> nextQuestion());
        }
    }

    private void chooseAnswer(String answer) {
        selectedAnswer = answer;
        if (answer.equals(questions.get(questionIndex).answer)) {
            score++;
        } else if (!practiceMode) {
            prefs.edit().putInt("hearts", Math.max(0, hearts() - 1)).apply();
        }
        render();
    }

    private void nextQuestion() {
        if (questionIndex >= questions.size() - 1 || (!practiceMode && hearts() <= 0)) {
            completeLesson(); return;
        }
        questionIndex++;
        selectedAnswer = null;
        render();
    }

    private void completeLesson() {
        int gain = score * 5 + 5;
        if (!practiceMode) {
            String key = doneKey(selectedLevel, selectedUnit, selectedLesson);
            if (!prefs.getBoolean(key, false)) {
                prefs.edit().putBoolean(key, true).putInt("completed_total",
                        prefs.getInt("completed_total", 0) + 1).apply();
            } else {
                gain = Math.max(2, score * 2);
            }
        }
        prefs.edit().putInt("xp", xp() + gain)
                .putInt(todayKey(), prefs.getInt(todayKey(), 0) + gain)
                .putInt("hearts", Math.min(5, hearts() + 1)).apply();
        updateStreak();
        page = "done";
        selectedAnswer = null;
        render();
    }

    private void renderDone() {
        LinearLayout card = panel();
        card.setGravity(Gravity.CENTER);
        ImageView mascot = new ImageView(this);
        mascot.setImageResource(R.drawable.nonnon_mascot);
        card.addView(mascot, new LinearLayout.LayoutParams(dp(130), dp(130)));
        addText(card, "آفرین! عالی پیش رفتی 🎉", 22, PINK, true);
        addText(card, score + " پاسخ درست از ۵ پرسش", 16, INK, true);
        addText(card, "امتیاز کل: " + xp() + " XP", 14, MUTED, false);
        addButton(card, "ادامه مسیر", true, () -> {
            if (practiceMode) page = "home"; else page = "lessons";
            render();
        });
        addButton(card, "تمرین دوباره", false, () -> startLesson(selectedLevel, selectedUnit, selectedLesson, practiceMode));
    }

    private void startPractice() { page = "practice"; render(); }

    private void renderPractice() {
        title("تمرین روزانه", "پنج پرسش کوتاه برای مرور و تثبیت آموخته‌ها.");
        LinearLayout card = panel();
        addText(card, "سطح تمرین", 16, INK, true);
        for (String level : LEVELS) {
            addButton(card, level + " · " + LEVEL_NAMES[indexOfLevel(level)], selectedLevel.equals(level),
                    () -> { selectedLevel = level; page = "practice"; render(); });
        }
        addButton(card, "شروع تمرین  ✦", true, () -> startLesson(selectedLevel, 0,
                prefs.getInt("completed_total", 0) % 5, true));
        addText(card, "پاسخ‌ها و امتیازها روی همین گوشی ذخیره می‌شوند.", 12, MUTED, false);
    }

    private void renderWords() {
        title("دفتر واژگان", "کلمه‌ها را لمس کن و تلفظ انگلیسی‌شان را بشنو.");
        LinearLayout levels = horizontal();
        for (String level : LEVELS) {
            TextView chip = text(level, 13, selectedLevel.equals(level) ? PINK : MUTED, true);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(shape(selectedLevel.equals(level) ? PALE_PINK : Color.WHITE, 0, 30));
            pad(chip, 13, 9, 13, 9);
            chip.setOnClickListener(v -> { selectedLevel = level; page = "words"; render(); });
            levels.addView(chip, new LinearLayout.LayoutParams(0, dp(42), 1));
        }
        content.addView(levels);
        gap(content, 12);
        for (Word w : decks.get(selectedLevel)) {
            LinearLayout card = panel();
            LinearLayout row = horizontal();
            LinearLayout meaning = vertical();
            meaning.addView(text(w.en, 18, INK, true));
            meaning.addView(text(w.fa + "  ·  " + w.pronunciation, 12, MUTED, false));
            row.addView(meaning, new LinearLayout.LayoutParams(0, -2, 1));
            Button play = button("🔊", false, () -> speak(w.en));
            row.addView(play, new LinearLayout.LayoutParams(dp(66), dp(46)));
            card.addView(row);
            addText(card, w.example, 13, MUTED, false);
        }
    }

    private void renderProfile() {
        title("پروفایل من", "پیشرفت تو در مسیر یادگیری انگلیسی");
        LinearLayout card = panel();
        card.setGravity(Gravity.CENTER);
        ImageView mascot = new ImageView(this);
        mascot.setImageResource(R.drawable.nonnon_mascot);
        card.addView(mascot, new LinearLayout.LayoutParams(dp(116), dp(116)));
        addText(card, "زبان‌آموز nonnon", 20, INK, true);
        addText(card, "ادامه بده؛ هر روز بهتر می‌شی 💗", 13, MUTED, false);
        LinearLayout stats = horizontal();
        stats.addView(statBox("💎 " + xp(), "XP"), new LinearLayout.LayoutParams(0, -2, 1));
        stats.addView(statBox("🔥 " + streak(), "روز پیاپی"), new LinearLayout.LayoutParams(0, -2, 1));
        stats.addView(statBox("📘 " + prefs.getInt("completed_total", 0), "درس کامل"), new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(stats);
        addButton(content, "تنظیمات برنامه", false, () -> { page = "settings"; render(); });
    }

    private LinearLayout statBox(String value, String label) {
        LinearLayout b = vertical();
        b.setGravity(Gravity.CENTER);
        pad(b, 5, 10, 5, 10);
        b.setBackground(shape(PALE_PURPLE, 0, 15));
        b.addView(text(value, 15, PURPLE, true));
        b.addView(text(label, 10, MUTED, false));
        return b;
    }

    private void renderLeaderboard() {
        title("جدول رتبه 🏆", "امتیازهای تمرینت را دنبال کن و رکورد خودت را بهتر کن.");
        LinearLayout card = panel();
        card.setGravity(Gravity.CENTER);
        addText(card, "رتبه فعلی", 14, MUTED, true);
        addText(card, "🥇  زبان‌آموز پرتلاش", 20, INK, true);
        addText(card, xp() + " XP", 26, PINK, true);
        addText(card, "این جدول فعلاً رتبه‌بندی محلی دستگاه تو را نشان می‌دهد.", 12, MUTED, false);
        addButton(content, "رفتن به مسیر درس‌ها", true, () -> { page = "path"; render(); });
    }

    private void renderRewards() {
        title("پاداش‌ها 🎁", "با تمرین روزانه نشان‌های تازه بگیر.");
        int completed = prefs.getInt("completed_total", 0);
        String[][] rewards = {
                {"🌱", "شروع‌کننده", "اولین درس را کامل کن", String.valueOf(completed >= 1)},
                {"🔥", "پشتکار", "۵ درس را کامل کن", String.valueOf(completed >= 5)},
                {"⭐", "ستاره یادگیری", "۱۰ درس را کامل کن", String.valueOf(completed >= 10)},
                {"💎", "صد امتیازی", "۱۰۰ امتیاز XP جمع کن", String.valueOf(xp() >= 100)}
        };
        for (String[] reward : rewards) {
            LinearLayout card = panel();
            addText(card, reward[0] + "  " + reward[1], 17, INK, true);
            addText(card, reward[2], 12, MUTED, false);
            boolean earned = Boolean.parseBoolean(reward[3]);
            addText(card, earned ? "✓ دریافت شد" : "🔒 هنوز قفل است",
                    12, earned ? GREEN : MUTED, true);
        }
    }

    private void renderSettings() {
        title("تنظیمات", "برنامه را مطابق سلیقه‌ات تنظیم کن.");
        LinearLayout card = panel();
        addText(card, "صدا و تلفظ", 17, INK, true);
        Switch sound = new Switch(this);
        sound.setText("فعال بودن راهنمای صوتی");
        sound.setTextColor(INK);
        sound.setChecked(prefs.getBoolean("sound", true));
        sound.setOnCheckedChangeListener((v, checked) -> prefs.edit().putBoolean("sound", checked).apply());
        card.addView(sound);
        gap(card, 12);
        addText(card, "هدف روزانه XP", 15, INK, true);
        LinearLayout goals = horizontal();
        for (int goal : new int[]{10, 20, 30}) {
            TextView chip = text(goal + " XP", 13, prefs.getInt("daily_goal", 20) == goal ? PINK : MUTED, true);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(shape(prefs.getInt("daily_goal", 20) == goal ? PALE_PINK : Color.WHITE, 0, 25));
            pad(chip, 12, 10, 12, 10);
            chip.setOnClickListener(v -> { prefs.edit().putInt("daily_goal", goal).apply(); render(); });
            goals.addView(chip, new LinearLayout.LayoutParams(0, dp(44), 1));
        }
        card.addView(goals);
        LinearLayout about = panel();
        addText(about, "درباره nonnon", 17, INK, true);
        addText(about, "نسخه ۱٫۰ · آموزش انگلیسی با مسیر شش‌سطحی CEFR", 13, MUTED, false);
        addText(about, "پیشرفت و واژگان این نسخه به‌صورت آفلاین روی دستگاه ذخیره می‌شوند.", 12, MUTED, false);
        addButton(content, "بازنشانی کامل پیشرفت", false, () -> new AlertDialog.Builder(this)
                .setTitle("بازنشانی پیشرفت")
                .setMessage("امتیازها، قلب‌ها و درس‌های تکمیل‌شده پاک شوند؟")
                .setNegativeButton("انصراف", (d, w) -> {})
                .setPositiveButton("پاک کردن", (d, w) -> {
                    prefs.edit().clear().apply(); page = "home"; render();
                }).show());
    }

    private void openNextLesson() {
        String level = "A1";
        for (int u = 0; u < 10; u++) {
            for (int l = 0; l < 5; l++) {
                if (!prefs.getBoolean(doneKey(level, u, l), false)) {
                    startLesson(level, u, l, false); return;
                }
            }
        }
        selectedLevel = "A1"; selectedUnit = 0; selectedLesson = 0;
        startLesson(selectedLevel, selectedUnit, selectedLesson, false);
    }

    private void speak(String value) {
        if (!prefs.getBoolean("sound", true)) {
            Toast.makeText(this, "راهنمای صوتی در تنظیمات خاموش است.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ttsReady && tts != null) tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "nonnon-voice");
        else Toast.makeText(this, "موتور تبدیل متن به گفتار گوشی آماده نیست.", Toast.LENGTH_SHORT).show();
    }

    private int hearts() { return prefs.getInt("hearts", 5); }
    private int xp() { return prefs.getInt("xp", 0); }
    private int streak() { return prefs.getInt("streak", 0); }
    private String todayKey() { return "xp_" + new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()); }
    private String doneKey(String level, int unit, int lesson) { return "done_" + level + "_" + unit + "_" + lesson; }
    private int indexOfLevel(String level) {
        for (int i = 0; i < LEVELS.length; i++) if (LEVELS[i].equals(level)) return i;
        return 0;
    }

    private int completedInLevel(String level) {
        int count = 0;
        for (int u = 0; u < 10; u++)
            for (int l = 0; l < 5; l++)
                if (prefs.getBoolean(doneKey(level, u, l), false)) count++;
        return count;
    }

    private void updateStreak() {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        String last = prefs.getString("last_study", "");
        if (today.equals(last)) return;
        String yesterday = new SimpleDateFormat("yyyyMMdd", Locale.US)
                .format(new Date(System.currentTimeMillis() - 86400000L));
        int newStreak = yesterday.equals(last) ? streak() + 1 : 1;
        prefs.edit().putInt("streak", newStreak).putString("last_study", today).apply();
    }

    @Override public void onBackPressed() {
        if ("quiz".equals(page)) {
            new AlertDialog.Builder(this).setMessage("از تمرین خارج می‌شوی؟")
                    .setNegativeButton("ادامه", (d, w) -> {}).setPositiveButton("خروج", (d, w) -> {
                        page = practiceMode ? "practice" : "lessons"; render();
                    }).show();
        } else if ("units".equals(page)) { page = "path"; render(); }
        else if ("lessons".equals(page)) { page = "units"; render(); }
        else if (!"home".equals(page)) { page = "home"; render(); }
        else super.onBackPressed();
    }
}
