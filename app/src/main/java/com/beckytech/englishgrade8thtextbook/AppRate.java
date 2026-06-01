package com.beckytech.englishgrade8thtextbook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.Task;

public class AppRate {
    private final static String APP_TITLE = "English Grade 8th";
    private static final String APP_PNAME = "com.beckytech.englishgrade8thtextbook";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 5;

    public static void app_launched(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(activity, editor);
            }
        }

        editor.apply();
    }

    @SuppressLint("SetTextI18n")
    public static void showRateDialog(final Activity activity, final SharedPreferences.Editor editor) {
        ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.apply();
                    }
                });
            } else {
                // Fallback to manual dialog if in-app review fails
                showManualRateDialog(activity, editor);
            }
        });
    }

    private static void showManualRateDialog(final Activity activity, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(activity);
        dialog.setTitle("Rate " + APP_TITLE);

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(20, 20, 20, 20);

        TextView tv = new TextView(activity);
        tv.setText("If you enjoy using " + APP_TITLE + ", please take a moment to rate it.\nThanks for your support!");
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);

        Button b1 = new Button(activity);
        b1.setText("Rate Now");
        b1.setOnClickListener(v -> {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + APP_PNAME)));
            if (editor != null) {
                editor.putBoolean("dontshowagain", true);
                editor.apply();
            }
            dialog.dismiss();
        });
        ll.addView(b1);

        Button b2 = new Button(activity);
        b2.setText("Remind me later");
        b2.setOnClickListener(v -> dialog.dismiss());
        ll.addView(b2);

        Button b3 = new Button(activity);
        b3.setText("No, thanks");
        b3.setOnClickListener(v -> {
            if (editor != null) {
                editor.putBoolean("dontshowagain", true);
                editor.apply();
            }
            dialog.dismiss();
        });
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();
    }
}