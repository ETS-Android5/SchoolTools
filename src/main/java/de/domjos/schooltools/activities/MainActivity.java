/*
 * Copyright (C) 2017-2019  Dominic Joas
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package de.domjos.schooltools.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Config;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.github.angads25.filepicker.view.FilePickerDialog;

import java.util.*;

import de.domjos.schooltools.R;
import de.domjos.schooltools.adapter.*;
import de.domjos.schooltools.adapter.syncAdapter.Authenticator;
import de.domjos.schooltools.core.SearchItem;
import de.domjos.schooltools.core.model.Memory;
import de.domjos.schooltools.core.model.Note;
import de.domjos.schooltools.core.model.Subject;
import de.domjos.schooltools.core.model.TimerEvent;
import de.domjos.schooltools.core.model.mark.SchoolYear;
import de.domjos.schooltools.core.model.mark.Test;
import de.domjos.schooltools.core.model.mark.Year;
import de.domjos.schooltools.core.model.timetable.SchoolClass;
import de.domjos.schooltools.core.model.timetable.Teacher;
import de.domjos.schooltools.core.model.timetable.TimeTable;
import de.domjos.schooltools.core.model.todo.ToDo;
import de.domjos.schooltools.core.model.todo.ToDoList;
import de.domjos.schooltools.custom.AbstractActivity;
import de.domjos.schooltools.helper.Converter;
import de.domjos.schooltools.helper.Helper;
import de.domjos.schooltools.helper.Log4JHelper;
import de.domjos.schooltools.helper.SQLite;
import de.domjos.schooltools.services.AuthenticatorService;
import de.domjos.schooltools.services.CalendarSyncService;
import de.domjos.schooltools.services.MemoryService;
import de.domjos.schooltools.settings.GeneralSettings;
import de.domjos.schooltools.settings.Globals;
import de.domjos.schooltools.settings.MarkListSettings;
import de.domjos.schooltools.settings.UserSettings;
import de.domjos.schooltools.screenWidgets.ButtonScreenWidget;
import de.domjos.schooltools.screenWidgets.ImportantToDoScreenWidget;
import de.domjos.schooltools.screenWidgets.SavedMarkListsScreenWidget;
import de.domjos.schooltools.screenWidgets.SavedTimeTablesScreenWidget;
import de.domjos.schooltools.screenWidgets.TaggedBookMarksScreenWidget;
import de.domjos.schooltools.screenWidgets.TimeTableEventScreenWidget;
import de.domjos.schooltools.screenWidgets.TodayScreenWidget;
import de.domjos.schooltools.screenWidgets.Top5NotesScreenWidget;

import static android.provider.CalendarContract.AUTHORITY;

/**
 * Activity For the Main-Screen
 * @author Dominic Joas
 * @version 1.0
 */
public final class MainActivity extends AbstractActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public static final Globals globals = new Globals();

    private ImageButton cmdRefresh;
    private SearchView cmdSearch;
    private ListView lvSearchResults;
    private SearchAdapter searchAdapter;
    private CountDownTimer countDownTimer;

    private TodayScreenWidget todayScreenWidget;
    private ButtonScreenWidget buttonScreenWidget;
    private Top5NotesScreenWidget top5NotesScreenWidget;
    private ImportantToDoScreenWidget importantToDoScreenWidget;
    private SavedTimeTablesScreenWidget savedTimeTablesScreenWidget;
    private SavedMarkListsScreenWidget savedMarkListsScreenWidget;
    private TimeTableEventScreenWidget timeTableEventScreenWidget;
    private TaggedBookMarksScreenWidget taggedBookMarksScreenWidget;

    public MainActivity() {
        super(R.layout.main_activity);
    }

    @Override
    protected void initActions() {
        Log4JHelper.configure(MainActivity.this);
        MainActivity.globals.setUserSettings(new UserSettings(this.getApplicationContext()));
        MainActivity.globals.setGeneralSettings(new GeneralSettings(this.getApplicationContext()));
        this.resetDatabase();
        this.initDatabase();
        this.initServices();
        Helper.setBackgroundAppBarToActivity(this.navigationView, MainActivity.this);

        this.buttonScreenWidget.init();
        this.buttonScreenWidget.loadButtons();
        this.buttonScreenWidget.hideButtons();
        this.todayScreenWidget.init();
        this.todayScreenWidget.addEvents();
        this.top5NotesScreenWidget.init();
        this.top5NotesScreenWidget.addNotes();
        this.importantToDoScreenWidget.init();
        this.importantToDoScreenWidget.addToDos();
        this.savedTimeTablesScreenWidget.init();
        this.savedTimeTablesScreenWidget.initSavedTimeTables();
        this.savedMarkListsScreenWidget.init();
        this.savedMarkListsScreenWidget.addMarkLists();
        this.timeTableEventScreenWidget.init();
        this.taggedBookMarksScreenWidget.init();
        MainActivity.globals.getUserSettings().openStartModule(this.buttonScreenWidget, MainActivity.this);

        this.hideWidgets();

        this.deleteMemoriesFromPast();
        this.deleteToDosFromPast();
        this.setSavedValuesForWidgets();
        this.openWhatsNew();

        this.countDownTimer = new CountDownTimer(Long.MAX_VALUE, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeTableEventScreenWidget.initCurrentTimeTableEvent();
            }

            @Override
            public void onFinish() {
                countDownTimer.start();
            }
        }.start();



        this.cmdRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todayScreenWidget.addEvents();
                top5NotesScreenWidget.addNotes();
                importantToDoScreenWidget.addToDos();
                savedMarkListsScreenWidget.addMarkLists();
                Helper.createToast(getApplicationContext(), getString(R.string.main_refreshSuccessFully));
            }
        });

        this.cmdSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                lvSearchResults.setVisibility(View.GONE);
                return false;
            }
        });

        this.cmdSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                addSearchItems(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                addSearchItems(newText);
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // settings
        if (requestCode == 98) {
            if(MainActivity.globals.getUserSettings().isWhatsNew()) {
                WhatsNewActivity.resetShown("whats_new", this.getApplicationContext());
                MainActivity.globals.getUserSettings().setWhatsNew(false);

                Intent intent = new Intent(this.getApplicationContext(), WhatsNewActivity.class);
                intent.putExtra(WhatsNewActivity.isWhatsNew, true);
                intent.putExtra(WhatsNewActivity.INFO_PARAM, "whats_new_info");
                this.startActivity(intent);
            }

            this.resetDatabase();
            this.initDatabase();
            this.cmdRefresh.callOnClick();
            this.buttonScreenWidget.hideButtons();
            MainActivity.globals.getUserSettings().hideMenuItems(this.navigationView);
            this.timeTableEventScreenWidget.initCurrentTimeTableEvent();
            MainActivity.globals.getUserSettings().openStartModule(this.buttonScreenWidget, MainActivity.this);
            this.hideWidgets();
            Helper.setBackgroundToActivity(this);
            Helper.setBackgroundAppBarToActivity(this.navigationView, MainActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        if(this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MainActivity.globals.getUserSettings().hideMenuItems(this.navigationView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.menMainExport:
                intent = new Intent(this.getApplicationContext(), ApiActivity.class);
                break;
            case R.id.menMainSettings:
                intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
                break;
            case R.id.menMainHelp:
                intent = new Intent(this.getApplicationContext(), HelpActivity.class);
                break;
            default:
                intent = null;
        }

        if(intent!=null) {
            this.startActivityForResult(intent, 98);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (Helper.checkMenuID(item)) {
            case R.id.navMainMarkList:
                intent = null;
                this.buttonScreenWidget.openMarkListIntent();
                break;
            case R.id.navMainCalculateMark:
                intent = new Intent(this.getApplicationContext(), MarkActivity.class);
                break;
            case R.id.navMainTimeTable:
                intent = new Intent(this.getApplicationContext(), TimeTableActivity.class);
                break;
            case R.id.navMainNotes:
                intent = new Intent(this.getApplicationContext(), NoteActivity.class);
                break;
            case R.id.navMainTimer:
                intent = new Intent(this.getApplicationContext(), TimerActivity.class);
                break;
            case R.id.navMainToDo:
                intent = new Intent(this.getApplicationContext(), ToDoActivity.class);
                break;
            case R.id.navMainLearningCards:
                intent = new Intent(this.getApplicationContext(), LearningCardOverviewActivity.class);
                break;
            case R.id.navMainBookMarks:
                intent = new Intent(this.getApplicationContext(), BookmarkActivity.class);
                break;
            default:
                intent = null;
        }

        if(intent!=null) {
            this.startActivityForResult(intent, 99);
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initServices() {
        try {
            if(MainActivity.globals.getUserSettings().isNotificationsShown()) {
                Helper.initRepeatingService(MainActivity.this, MemoryService.class,  8 * 60 * 60 * 1000);
            }
            if(MainActivity.globals.getUserSettings().isSyncCalendarTurnOn()) {
                if(Helper.checkPermissions(Helper.PERMISSIONS_REQUEST_WRITE_CALENDAR, MainActivity.this, Manifest.permission.READ_CALENDAR) && Helper.checkPermissions(Helper.PERMISSIONS_REQUEST_WRITE_CALENDAR, MainActivity.this, Manifest.permission.WRITE_CALENDAR)) {
                    this.initCalendarSyncService();
                }
            }
        } catch (Exception ex) {
            Helper.printException(this.getApplicationContext(), ex);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            switch (requestCode) {
                case Helper.PERMISSIONS_REQUEST_WRITE_CALENDAR:
                    this.initCalendarSyncService();
                    break;
            }
        } catch (Exception ex) {
            Helper.printException(MainActivity.this, ex);
        }
    }

    private void initCalendarSyncService() {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean("PREF_SETUP_COMPLETE", false);

        Account account = AuthenticatorService.GetAccount(this.getApplicationContext(), "de.domjos.schooltools.account");
        AccountManager accountManager = (AccountManager) this.getApplicationContext().getSystemService(Context.ACCOUNT_SERVICE);
        if(accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            Bundle bundle = new Bundle();
            bundle.putString("name", MainActivity.globals.getUserSettings().getSyncCalendarName());
            ContentResolver.addPeriodicSync(account, AUTHORITY, bundle, 60 * 1000);
            newAccount = true;
        }
        if (newAccount || !setupComplete) {
            PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putBoolean("PREF_SETUP_COMPLETE", true).apply();
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initControls() {
        // init Toolbar
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        // init drawer
        this.drawerLayout = this.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        this.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        this.cmdRefresh = this.findViewById(R.id.cmdRefresh);
        this.cmdSearch = this.findViewById(R.id.cmdSearch);

        // init navigationView
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);

        // init other controls
        this.buttonScreenWidget = new ButtonScreenWidget(this.findViewById(R.id.tblButtons), MainActivity.this);
        this.todayScreenWidget = new TodayScreenWidget(this.findViewById(R.id.llToday), MainActivity.this);
        this.top5NotesScreenWidget = new Top5NotesScreenWidget(this.findViewById(R.id.llCurrentNotes), MainActivity.this);
        this.importantToDoScreenWidget = new ImportantToDoScreenWidget(this.findViewById(R.id.llImportantToDos), MainActivity.this);
        this.savedTimeTablesScreenWidget = new SavedTimeTablesScreenWidget(this.findViewById(R.id.llSavedTimeTables), MainActivity.this);
        this.savedMarkListsScreenWidget = new SavedMarkListsScreenWidget(this.findViewById(R.id.llSavedMarklist), MainActivity.this);
        this.timeTableEventScreenWidget = new TimeTableEventScreenWidget(this.findViewById(R.id.llTodayCurrentTimeTable), MainActivity.this);
        this.taggedBookMarksScreenWidget = new TaggedBookMarksScreenWidget(this.findViewById(R.id.llTaggedBookMarks), MainActivity.this);

        this.lvSearchResults = this.findViewById(R.id.lvSearchResults);
        this.searchAdapter = new SearchAdapter(MainActivity.this);
        this.lvSearchResults.setAdapter(this.searchAdapter);
        this.searchAdapter.notifyDataSetChanged();
    }

    private void initDatabase() {
        SQLite sqLite = new SQLite(this.getApplicationContext(), "schoolTools.db", MainActivity.globals.getGeneralSettings().getCurrentVersionCode(MainActivity.this));
        MainActivity.globals.setSqLite(sqLite);
    }

    private void addSearchItems(String search) {
        this.searchAdapter.clear();

        for(MarkListSettings settings : MainActivity.globals.getSqLite().getMarkListSearch(search)) {
            this.searchAdapter.add(new SearchItem(settings.getId(), settings.getTitle(), this.getString(R.string.main_nav_mark_list)));
        }

        for(SchoolYear schoolYear : MainActivity.globals.getSqLite().getSchoolYears("")) {
            for(Test test : schoolYear.getTests()) {
                if(test.getTitle().toLowerCase().contains(search.toLowerCase())) {
                    this.searchAdapter.add(new SearchItem(test.getID(), test.getTitle(), this.getString(R.string.mark_test)));
                }
            }
        }

        for(Note note : MainActivity.globals.getSqLite().getNotes("title like '%" + search + "%'")) {
            this.searchAdapter.add(new SearchItem(note.getID(), note.getTitle(), this.getString(R.string.main_nav_notes)));
        }

        for(ToDoList toDoList : MainActivity.globals.getSqLite().getToDoLists("title like '%" + search + "%'")) {
            this.searchAdapter.add(new SearchItem(toDoList.getID(), toDoList.getTitle(), this.getString(R.string.todo_list)));
        }

        for(ToDo toDo : MainActivity.globals.getSqLite().getToDos("title like '%" + search + "%'")) {
            this.searchAdapter.add(new SearchItem(toDo.getID(), toDo.getTitle(), this.getString(R.string.main_nav_todo)));
        }

        for(TimeTable timeTable : MainActivity.globals.getSqLite().getTimeTables("title like '%" + search + "%'")) {
            this.searchAdapter.add(new SearchItem(timeTable.getID(), timeTable.getTitle(), this.getString(R.string.main_nav_timetable)));
        }

        for(TimerEvent timerEvent : MainActivity.globals.getSqLite().getTimerEvents("title like '%" + search + "%'")) {
            SearchItem searchItem = new SearchItem(timerEvent.getID(), timerEvent.getTitle(), this.getString(R.string.main_nav_timer));
            searchItem.setExtra(Converter.convertDateToString(timerEvent.getEventDate()));
            this.searchAdapter.add(searchItem);
        }


        for(Subject subject : MainActivity.globals.getSqLite().getSubjects("title like '%" + search + "'")) {
            this.searchAdapter.add(new SearchItem(subject.getID(), subject.getTitle(), this.getString(R.string.timetable_lesson)));
        }

        for(Teacher teacher : MainActivity.globals.getSqLite().getTeachers("lastName like '%" + search + "'")) {
            this.searchAdapter.add(new SearchItem(teacher.getID(), teacher.getLastName(), this.getString(R.string.timetable_teacher)));
        }

        for(SchoolClass schoolClass : MainActivity.globals.getSqLite().getClasses("title like '%" + search + "'")) {
            this.searchAdapter.add(new SearchItem(schoolClass.getID(), schoolClass.getTitle(), this.getString(R.string.timetable_class)));
        }

        for(Year year : MainActivity.globals.getSqLite().getYears("title like '%" + search + "'")) {
            this.searchAdapter.add(new SearchItem(year.getID(), year.getTitle(), this.getString(R.string.mark_year)));
        }


        if(this.searchAdapter.isEmpty()) {
            this.lvSearchResults.setVisibility(View.GONE);
        } else {
            this.lvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    private void hideWidgets() {
        this.buttonScreenWidget.setVisibility(false);
        this.todayScreenWidget.setVisibility(false);
        this.top5NotesScreenWidget.setVisibility(false);
        this.importantToDoScreenWidget.setVisibility(false);
        this.savedTimeTablesScreenWidget.setVisibility(false);
        this.savedMarkListsScreenWidget.setVisibility(false);
        this.timeTableEventScreenWidget.setVisibility(false);
        this.taggedBookMarksScreenWidget.setVisibility(false);

        for(String item : MainActivity.globals.getUserSettings().getStartWidgets()) {
            if(item.equals(this.getString(R.string.main_nav_buttons))) {
                this.buttonScreenWidget.setVisibility(true);
            }
            if(item.equals(this.getString(R.string.main_today))) {
                this.todayScreenWidget.setVisibility(true);
            }
            if(item.equals(this.getString(R.string.main_today_timetable))) {
                this.timeTableEventScreenWidget.setVisibility(true);
            }
            if(item.equals(this.getString(R.string.main_savedTimeTables))) {
                this.savedTimeTablesScreenWidget.setVisibility(true);
            }
            if(item.equals(this.getString(R.string.main_top5Notes))) {
                this.top5NotesScreenWidget.setVisibility(true);
            }
            if(item.equals(this.getString(R.string.main_importantToDos))) {
                this.importantToDoScreenWidget.setVisibility(true);
            }
            if(item.equals(this.getString(R.string.main_savedMarkList))) {
                this.savedMarkListsScreenWidget.setVisibility(true);
            }
            if(item.equals(this.getString(R.string.main_taggedBookMarks))) {
                this.taggedBookMarksScreenWidget.setVisibility(true);
            }
        }
    }

    private void deleteMemoriesFromPast() {
        if(MainActivity.globals.getUserSettings().isDeleteMemories()) {
            for(Memory memory : MainActivity.globals.getSqLite().getCurrentMemories()) {
                try {
                    if (Helper.compareDateWithCurrentDate(Converter.convertStringToDate(memory.getDate()))) {
                        MainActivity.globals.getSqLite().deleteEntry("memories", "itemID=" + memory.getID());
                    }
                } catch (Exception ex) {
                    MainActivity.globals.getSqLite().deleteEntry("memories", "itemID=" + memory.getID());
                    Log4JHelper.getLogger(MainActivity.this.getPackageName()).error(ex.getMessage());
                }
            }
        }
    }

    private void deleteToDosFromPast() {
        if(MainActivity.globals.getUserSettings().isDeleteToDoAfterDeadline()) {
            for(ToDoList toDoList : MainActivity.globals.getSqLite().getToDoLists("")) {
                try {
                    if(Helper.compareDateWithCurrentDate(toDoList.getListDate())) {
                        MainActivity.globals.getSqLite().deleteEntry("toDoLists", "ID=" + toDoList.getID());
                    }
                } catch (Exception ex) {
                    Log4JHelper.getLogger(MainActivity.this.getPackageName()).error(ex.getMessage());
                }
            }
        }
    }

    private void openWhatsNew() {
        Intent intent = new Intent(this.getApplicationContext(), WhatsNewActivity.class);
        intent.putExtra(WhatsNewActivity.isWhatsNew, true);
        intent.putExtra(WhatsNewActivity.INFO_PARAM, "whats_new_info");
        this.startActivity(intent);
    }

    private void resetDatabase() {
        try {
            if(MainActivity.globals.getUserSettings().isGeneralResetDatabase()) {
                this.getApplicationContext().deleteDatabase("schoolTools.db");
            }
        } catch (Exception ex) {
            Helper.printException(this.getApplicationContext(), ex);
        }
    }

    private void setSavedValuesForWidgets() {
        this.savedMarkListsScreenWidget.getSavedValue(MainActivity.globals.getGeneralSettings().getWidgetMarkListSpinner());
        this.savedTimeTablesScreenWidget.getSavedValues(MainActivity.globals.getGeneralSettings().getWidgetTimetableSpinner());
    }
}
