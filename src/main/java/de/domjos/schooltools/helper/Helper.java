/*
 * Copyright (C) 2017-2019  Dominic Joas
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */
package de.domjos.schooltools.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.regex.Pattern;

import de.domjos.schooltools.R;
import de.domjos.schooltools.activities.HelpActivity;
import de.domjos.schooltools.activities.MainActivity;
import de.domjos.schooltools.core.model.Note;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 *
 * @author dominic
 */
public class Helper {
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 99;
    public static final int PERMISSIONS_REQUEST_WRITE_CALENDAR = 101;

    public static String getLanguage(Context context, int id) {
        return context.getString(id);
    }

    public static void createToast(Context context, String msg) {
        Helper.createToast(context, msg, true);
    }

    public static void createToast(Activity activity, String msg) {
        Helper.createToast(activity, msg, true);
    }

    private static void createToast(Activity activity, String msg, boolean log) {
        if(log) {
            Log4JHelper.getLogger(activity.getPackageName()).info(msg);
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) activity.findViewById(R.id.custom_toast_container));
        TextView text = layout.findViewById(R.id.text);
        text.setText(msg);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private static void createToast(Context context, String msg, boolean log) {
        if(log) {
            Log4JHelper.getLogger(context.getPackageName()).info(msg);
        }
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void printException(Context context, Throwable ex) {
        StringBuilder message = new StringBuilder(ex.getMessage() + "\n" + ex.toString());
        for(StackTraceElement element : ex.getStackTrace()) {
            message.append(element.getFileName()).append(":").append(element.getClassName()).append(":").append(element.getMethodName()).append(":").append(element.getLineNumber());
        }
        Log.e("Exception", message.toString(), ex);
        Log4JHelper.getLogger(context.getPackageName()).error("Exception", ex);
        Helper.createToast(context, ex.getLocalizedMessage(), false);
    }

    public static void printException(Activity activity, Throwable ex) {
        StringBuilder message = new StringBuilder(ex.getMessage() + "\n" + ex.toString());
        for(StackTraceElement element : ex.getStackTrace()) {
            message.append(element.getFileName()).append(":").append(element.getClassName()).append(":").append(element.getMethodName()).append(":").append(element.getLineNumber());
        }
        Log.e("Exception", message.toString(), ex);
        Log4JHelper.getLogger(activity.getPackageName()).error("Exception", ex);
        Helper.createToast(activity, ex.getLocalizedMessage(), false);
    }

    public static String readFileFromRaw(Context context, int id) {
        StringBuilder content = new StringBuilder();
        InputStream inputStream = null;
        try {
            Resources resources = context.getResources();
            inputStream = resources.openRawResource(id);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
            String line;
            do {
                line = reader.readLine();
                if(line == null) {
                    break;
                }
                content.append(line).append("\n");
            } while (true);
            reader.close();
        } catch (Exception ex) {
            Helper.printException(context, ex);
        } finally {
            try {
                if(inputStream!=null) {
                    inputStream.close();
                }
            } catch (Exception ex) {
                Helper.printException(context, ex);
            }
        }
        return content.toString();
    }

    public static void closeSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static View getRowView(Context context, ViewGroup parent, int layout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater!=null) {
            return inflater.inflate(layout, parent, false);
        }
        return new View(context);
    }

    public static void displaySpeechRecognizer(Activity activity, int req_code) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        activity.startActivityForResult(intent, req_code);
    }

    public static boolean writeStringToFile(String data, String path, Context context) {
        try {
            path = path.replace(" ", "%20");
            String[] spl = path.split("/");
            String fileName = spl[spl.length-1];

            File directory = new File(path.replace(fileName, ""));
            if(!directory.exists()) {
                if(!directory.mkdirs()) {
                    return false;
                }
            }

            File newFile = new File(path);
            if(newFile.exists()) {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(newFile), Charset.defaultCharset());
                outputStreamWriter.write(data);
                outputStreamWriter.close();
                return true;
            } else {
                if(newFile.createNewFile()) {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(newFile), Charset.defaultCharset());
                    outputStreamWriter.write(data);
                    outputStreamWriter.close();
                    return true;
                }
            }
        } catch (Exception ex) {
            Helper.printException(context, ex);
        }
        return false;
    }

    public static Note getNoteFromString(Context context, String result) {
        Note note = new Note();
        if(result.contains(context.getString(R.string.sys_title)) && result.contains(context.getString(R.string.sys_description))) {
            result = result.replace(context.getString(R.string.sys_title), "").trim();
            String[] content = result.split(context.getString(R.string.sys_description));
            note.setTitle(content[0]);
            note.setDescription(content[1]);
        } else if(result.contains(context.getString(R.string.sys_title))) {
            result = result.replace(context.getString(R.string.sys_title), "").trim();
            note.setTitle(result);
        } else if(result.contains(context.getString(R.string.sys_description))) {
            result = result.replace(context.getString(R.string.sys_description), "").trim();
            note.setDescription(result);
        } else {
            note.setDescription(result.trim());
        }
        return note;
    }

    public static String getStringFromFile(String path, Context context) {
        StringBuilder fileContent = new StringBuilder();
        try {
            File readableFile = new File(path);
            if(readableFile.exists() && readableFile.isFile()) {
                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(readableFile), Charset.defaultCharset());
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = reader.readLine()) != null){
                    fileContent.append(line);
                    fileContent.append("\n");
                }
                reader.close();
                inputStreamReader.close();
            }
        } catch (Exception ex) {
            Helper.printException(context, ex);
        }
        return fileContent.toString();
    }

    public static void sendMailWithAttachment(String email, String subject, File file, Context context) {
        Uri attachment = FileProvider.getUriForFile(context, context.getPackageName() + ".my.package.name.provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("vnd.android.cursor.dir/email");
        String[] to = {email};
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    public static boolean compareDateWithCurrentDate(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Calendar today = new GregorianCalendar();
        today.setTime(new Date());

        return calendar.get(Calendar.YEAR)==today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH)==today.get(Calendar.MONTH) && calendar.get(Calendar.DATE)==today.get(Calendar.DATE);
    }

   public static boolean checkPermissions(int callbackId, Activity activity, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(activity, p) == PERMISSION_GRANTED;
        }

        if (!permissions) {
            ActivityCompat.requestPermissions(activity, permissionsId, callbackId);
        }
        return permissions;
   }

    public static boolean checkPermissions(Context context, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(context, p) == PERMISSION_GRANTED;
        }

        return permissions;
    }

    public static void showMenuControls(boolean editMode, BottomNavigationView navigation) {
        if (editMode) {
            navigation.getMenu().getItem(0).setEnabled(false);
            navigation.getMenu().getItem(1).setEnabled(false);
            navigation.getMenu().getItem(2).setEnabled(false);
            navigation.getMenu().getItem(3).setEnabled(true);
            navigation.getMenu().getItem(4).setEnabled(true);
        } else {
            navigation.getMenu().getItem(0).setEnabled(true);
            navigation.getMenu().getItem(3).setEnabled(false);
            navigation.getMenu().getItem(4).setEnabled(false);
        }
    }

    public static boolean isInteger(String number) {
        return Pattern.matches("^\\d+$", number.trim());
    }

    public static boolean isDouble(String number) {
        return Pattern.matches("^[0-9]+(.|,)?[0-9]?$", number.trim());
    }

    public static int checkMenuID(MenuItem item) {
        int id;
        try {
            id = item.getItemId();
        } catch (NullPointerException ex) {
            id = -99;
        }
        return id;
    }

    public static void sendBroadCast(Context context, Class cls) {
        Intent intent = new Intent(context, cls);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, cls));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    public static void receiveBroadCast(Context context, Intent intent, int id) {
        Bundle bundle = intent.getExtras();
        if(bundle!=null) {
            if(intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)) {
                int[] ids = bundle.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.notifyAppWidgetViewDataChanged(ids, id);
            }
        }
    }

    public static SeekBar.OnSeekBarChangeListener getChangeListener(final TextView lbl) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lbl.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    public static MenuItem showHelpMenu(MenuItem item, Context context, String help_id) {
        int id = item.getItemId();
        if(id==R.id.menMainHelp || id==R.id.menHelp) {
            Intent intent = new Intent(context, HelpActivity.class);
            intent.putExtra("helpId", help_id);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        return item;
    }

    @SuppressWarnings("deprecation")
    public static void showHTMLInTextView(Context context, String resource, TextView txt) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(resource, "string", packageName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txt.setText(Html.fromHtml(context.getString(resId), Html.FROM_HTML_MODE_COMPACT));
        } else {
            txt.setText(Html.fromHtml(context.getString(resId)));
        }
    }

    @SuppressWarnings("deprecation")
    public static void setBackgroundToActivity(Activity activity) {
        Map.Entry<String, byte[]> entry = MainActivity.globals.getSqLite().getSetting("background");
        if(entry!=null) {
            if(!entry.getKey().equals("")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    activity.getWindow().getDecorView().getRootView().setBackground(new BitmapDrawable(activity.getResources(), BitmapFactory.decodeByteArray(entry.getValue(), 0, entry.getValue().length)));
                } else {
                    activity.getWindow().getDecorView().getRootView().setBackgroundDrawable(new BitmapDrawable(BitmapFactory.decodeByteArray(entry.getValue(), 0, entry.getValue().length)));
                }
                return;
            }
        }
        activity.getWindow().getDecorView().getRootView().setBackgroundResource(R.drawable.bg_water);
    }

    @SuppressWarnings("deprecation")
    public static void setBackgroundAppBarToActivity(NavigationView navigationView, Activity activity) {
        Map.Entry<String, byte[]> entry = MainActivity.globals.getSqLite().getSetting("app_bar_background");
        if(entry!=null) {
            if(!entry.getKey().equals("")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    navigationView.setBackground(new BitmapDrawable(activity.getResources(), BitmapFactory.decodeByteArray(entry.getValue(), 0, entry.getValue().length)));
                } else {
                    navigationView.setBackgroundDrawable(new BitmapDrawable(BitmapFactory.decodeByteArray(entry.getValue(), 0, entry.getValue().length)));
                }
                return;
            }
        }
        navigationView.setBackgroundResource(R.drawable.bg_ice);
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setSwipeDeleteListener(ListView lv, final ArrayAdapter adapter, final String table) {
        ListViewAdapter listViewAdapter = new ListViewAdapter(lv);
        final SwipeToDismissTouchListener<ListViewAdapter> touchListener = new SwipeToDismissTouchListener<>(
                listViewAdapter,
                new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListViewAdapter recyclerView, int position) {
                        Object object = adapter.getItem(position);
                        MainActivity.globals.getSqLite().deleteEntry(table, object);
                        adapter.remove(object);
                        adapter.notifyDataSetChanged();
                    }
                });

        lv.setOnTouchListener(touchListener);
        lv.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
    }


    public static View.OnTouchListener addOnTouchListenerForScrolling() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return v.performClick();
            }
        };
    }

    public static void initRepeatingService(Activity activity, Class<? extends Service> cls, long frequency) {
        // init Service
        Intent intent = new Intent(activity.getApplicationContext(), cls);
        PendingIntent pendingIntent1 = PendingIntent.getService(activity,  0, intent, 0);

        // init frequently
        AlarmManager alarmManager1 = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager1 != null;
        alarmManager1.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), frequency, pendingIntent1);
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(MainActivity.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
