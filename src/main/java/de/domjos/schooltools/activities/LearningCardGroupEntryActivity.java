package de.domjos.schooltools.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import de.domjos.schooltools.R;
import de.domjos.schooltools.adapter.LearningCardAdapter;
import de.domjos.schooltools.core.model.Subject;
import de.domjos.schooltools.core.model.learningCard.LearningCard;
import de.domjos.schooltools.core.model.learningCard.LearningCardGroup;
import de.domjos.schooltools.core.model.timetable.Teacher;
import de.domjos.schooltools.helper.Converter;
import de.domjos.schooltools.helper.Helper;
import de.domjos.schooltools.helper.Validator;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class LearningCardGroupEntryActivity extends AppCompatActivity {
    private Validator validator;
    private LearningCardGroup learningCardGroup;
    private int cardPosition = 0;

    private EditText txtLearningCardGroupTitle, txtLearningCardGroupDeadline, txtLearningCardGroupDescription, txtLearningCardGroupCategory;
    private Spinner spLearningCardGroupSubject, spLearningCardGroupTeacher;
    private ArrayAdapter<Subject> subjectAdapter;
    private ArrayAdapter<Teacher> teacherAdapter;

    private ListView lvLearningCards;
    private List<LearningCard> learningCards;
    private TextView lblLearningCardPriority;
    private SeekBar sbLearningCardPriority;
    private EditText txtLearningCardTitle, txtLearningCardCategory, txtLearningCardQuestion, txtLearningCardAnswer, txtLearningCardNote1, txtLearningCardNote2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.learning_card_group_entry_activity);
        this.initControls();
        this.loadCardGroup();

        this.lvLearningCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cardPosition = position;
                loadCard();
            }
        });

        this.lvLearningCards.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                LearningCard learningCard = learningCards.get(position);
                if(learningCard!=null) {
                    if(learningCard.getID()!=0) {
                        learningCards.remove(learningCard);
                        for(int i = 0; i<=learningCards.size()-1; i++) {
                            LearningCard tmp = learningCards.get(i);
                            if(tmp!=null) {
                                if(tmp.getID()==0) {
                                    cardPosition = i;
                                    loadCard();
                                    break;
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });

        this.txtLearningCardTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    LearningCard learningCard = learningCards.get(cardPosition);
                    if(learningCard!=null) {
                        learningCard.setTitle(s.toString());
                        reloadList();
                    }
                } catch (Exception ex) {
                    Helper.printException(getApplicationContext(), ex);
                }
            }
        });

        this.txtLearningCardCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    LearningCard learningCard = learningCards.get(cardPosition);
                    if(learningCard!=null) {
                        learningCard.setCategory(s.toString());
                        reloadList();
                    }
                } catch (Exception ex) {
                    Helper.printException(getApplicationContext(), ex);
                }
            }
        });

        this.txtLearningCardQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    LearningCard learningCard = learningCards.get(cardPosition);
                    if(learningCard!=null) {
                        learningCard.setQuestion(s.toString());
                        reloadList();
                    }
                } catch (Exception ex) {
                    Helper.printException(getApplicationContext(), ex);
                }
            }
        });

        this.txtLearningCardAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    LearningCard learningCard = learningCards.get(cardPosition);
                    if(learningCard!=null) {
                        learningCard.setAnswer(s.toString());
                        reloadList();
                    }
                } catch (Exception ex) {
                    Helper.printException(getApplicationContext(), ex);
                }
            }
        });

        this.txtLearningCardNote1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    LearningCard learningCard = learningCards.get(cardPosition);
                    if(learningCard!=null) {
                        learningCard.setNote1(s.toString());
                        reloadList();
                    }
                } catch (Exception ex) {
                    Helper.printException(getApplicationContext(), ex);
                }
            }
        });

        this.txtLearningCardNote2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    LearningCard learningCard = learningCards.get(cardPosition);
                    if(learningCard!=null) {
                        learningCard.setNote2(s.toString());
                        reloadList();
                    }
                } catch (Exception ex) {
                    Helper.printException(getApplicationContext(), ex);
                }
            }
        });

        this.sbLearningCardPriority.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    LearningCard learningCard = learningCards.get(cardPosition);
                    if(learningCard!=null) {
                        learningCard.setPriority(progress);
                        reloadList();
                    }
                } catch (Exception ex) {
                    Helper.printException(getApplicationContext(), ex);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    private void initControls() {
        BottomNavigationView navigation = this.findViewById(R.id.navigation);

        BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navLearningCardGroupCancel:
                        setResult(RESULT_OK);
                        finish();
                        return true;
                    case R.id.navLearningCardGroupSave:
                        try {
                            if(validator.getState()) {
                                getCardGroup();
                                MainActivity.globals.getSqLite().insertOrUpdateLearningCardGroup(learningCardGroup);
                                setResult(RESULT_OK);
                                finish();
                            }
                        } catch (Exception ex) {
                            Helper.printException(getApplicationContext(), ex);
                        }
                        return true;
                }
                return false;
            }
        };
        navigation.setOnNavigationItemSelectedListener(listener);


        TabHost host = this.findViewById(R.id.tabHost);
        host.setup();

        TabHost.TabSpec tabSpec1 = host.newTabSpec(this.getString(R.string.learningCard_groups));
        tabSpec1.setContent(R.id.tab1);
        tabSpec1.setIndicator(this.getString(R.string.learningCard_groups));
        host.addTab(tabSpec1);

        TabHost.TabSpec tabSpec2 = host.newTabSpec(this.getString(R.string.main_nav_learningCards));
        tabSpec2.setContent(R.id.tab2);
        tabSpec2.setIndicator(this.getString(R.string.main_nav_learningCards));
        host.addTab(tabSpec2);

        this.txtLearningCardGroupTitle = this.findViewById(R.id.txtLearningCardGroupTitle);
        this.txtLearningCardGroupDescription = this.findViewById(R.id.txtLearningCardGroupDescription);
        this.txtLearningCardGroupCategory = this.findViewById(R.id.txtLearningCardGroupCategory);
        this.txtLearningCardGroupDeadline = this.findViewById(R.id.txtLearningCardGroupDeadline);

        this.spLearningCardGroupSubject = this.findViewById(R.id.spLearningCardGroupSubject);
        this.subjectAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, MainActivity.globals.getSqLite().getSubjects(""));
        this.subjectAdapter.add(new Subject());
        this.spLearningCardGroupSubject.setAdapter(this.subjectAdapter);
        this.subjectAdapter.notifyDataSetChanged();

        this.spLearningCardGroupTeacher = this.findViewById(R.id.spLearningCardGroupTeacher);
        this.teacherAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, MainActivity.globals.getSqLite().getTeachers(""));
        this.teacherAdapter.add(new Teacher());
        this.spLearningCardGroupTeacher.setAdapter(this.teacherAdapter);
        this.teacherAdapter.notifyDataSetChanged();


        this.lvLearningCards = this.findViewById(R.id.lvLearningCards);
        this.learningCards = new LinkedList<>();
        LearningCardAdapter learningCardAdapter = new LearningCardAdapter(this.getApplicationContext(), R.layout.learning_card_item, this.learningCards);
        learningCardAdapter.add(new LearningCard());
        this.lvLearningCards.setAdapter(learningCardAdapter);
        learningCardAdapter.notifyDataSetChanged();

        this.lblLearningCardPriority = this.findViewById(R.id.lblLearningCardPriority);
        this.sbLearningCardPriority = this.findViewById(R.id.sbLearningCardPriority);
        this.lblLearningCardPriority.setText(String.valueOf(this.sbLearningCardPriority.getProgress()));
        this.sbLearningCardPriority.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblLearningCardPriority.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        this.txtLearningCardTitle = this.findViewById(R.id.txtLearningCardTitle);
        this.txtLearningCardCategory = this.findViewById(R.id.txtLearningCardCategory);
        this.txtLearningCardQuestion = this.findViewById(R.id.txtLearningCardQuestion);
        this.txtLearningCardAnswer = this.findViewById(R.id.txtLearningCardAnswer);
        this.txtLearningCardNote1 = this.findViewById(R.id.txtLearningCardNote1);
        this.txtLearningCardNote2 = this.findViewById(R.id.txtLearningCardNote2);
    }

    private void initValidation() {
        this.validator = new Validator(this.getApplicationContext());
        this.validator.addEmptyValidator(this.txtLearningCardTitle);
    }

    private void loadCardGroup() {
        int id = this.getIntent().getIntExtra("ID", 0);
        if(id==0) {
            this.learningCardGroup = new LearningCardGroup();
        } else {
            List<LearningCardGroup> learningCardGroups = MainActivity.globals.getSqLite().getLearningCardGroups("ID=" + id, true);
            this.learningCardGroup = learningCardGroups.get(0);
        }

        this.txtLearningCardGroupTitle.setText(this.learningCardGroup.getTitle());
        this.txtLearningCardGroupCategory.setText(this.learningCardGroup.getCategory());
        this.txtLearningCardGroupDescription.setText(this.learningCardGroup.getDescription());
        Date deadLine = this.learningCardGroup.getDeadLine();
        if(deadLine!=null) {
            this.txtLearningCardGroupDeadline.setText(Converter.convertDateToString(deadLine));
        }

        Subject subject = this.learningCardGroup.getSubject();
        if(subject!=null) {
            this.spLearningCardGroupSubject.setSelection(this.subjectAdapter.getPosition(subject));
        } else {
            for(int i = 0; i<=this.subjectAdapter.getCount()-1; i++) {
                Subject tmp = this.subjectAdapter.getItem(i);
                if(tmp!=null) {
                    if(tmp.getID()==0) {
                        this.spLearningCardGroupSubject.setSelection(i);
                        break;
                    }
                }

            }
        }

        Teacher teacher = this.learningCardGroup.getTeacher();
        if(teacher!=null) {
            this.spLearningCardGroupTeacher.setSelection(this.teacherAdapter.getPosition(teacher));
        } else {
            for(int i = 0; i<=this.teacherAdapter.getCount()-1; i++) {
                Teacher tmp = this.teacherAdapter.getItem(i);
                if(tmp!=null) {
                    if(tmp.getID()==0) {
                        this.spLearningCardGroupTeacher.setSelection(i);
                        break;
                    }
                }

            }
        }

        this.learningCards.addAll(this.learningCardGroup.getLearningCards());
    }

    private void loadCard() {
        LearningCard learningCard = this.learningCards.get(this.cardPosition);
        this.txtLearningCardTitle.setText(learningCard.getTitle());
        this.txtLearningCardCategory.setText(learningCard.getCategory());
        this.txtLearningCardQuestion.setText(learningCard.getQuestion());
        this.txtLearningCardAnswer.setText(learningCard.getAnswer());
        this.txtLearningCardNote1.setText(learningCard.getNote1());
        this.txtLearningCardNote2.setText(learningCard.getNote2());
        this.sbLearningCardPriority.setProgress(learningCard.getPriority());
    }

    private void getCardGroup() throws Exception {
        this.learningCardGroup.setTitle(this.txtLearningCardGroupTitle.getText().toString());
        this.learningCardGroup.setDescription(this.txtLearningCardGroupDescription.getText().toString());
        this.learningCardGroup.setCategory(this.txtLearningCardGroupCategory.getText().toString());

        String deadLine = this.txtLearningCardGroupDeadline.getText().toString();
        if(!deadLine.trim().equals("")) {
            this.learningCardGroup.setDeadLine(Converter.convertStringToDate(deadLine));
        }

        Teacher teacher = this.teacherAdapter.getItem(this.spLearningCardGroupTeacher.getSelectedItemPosition());
        if(teacher!=null) {
            if(teacher.getID()!=0) {
                this.learningCardGroup.setTeacher(teacher);
            } else {
                this.learningCardGroup.setTeacher(null);
            }
        } else {
            this.learningCardGroup.setTeacher(null);
        }

        Subject subject = this.subjectAdapter.getItem(this.spLearningCardGroupSubject.getSelectedItemPosition());
        if(subject!=null) {
            if(subject.getID()!=0) {
                this.learningCardGroup.setSubject(subject);
            } else {
                this.learningCardGroup.setSubject(null);
            }
        } else {
            this.learningCardGroup.setSubject(null);
        }
        this.learningCardGroup.setLearningCards(this.learningCards);
    }

    private void reloadList() {
        boolean isAvailable = false;
        for(LearningCard tmp : learningCards) {
            if(tmp.getID()==0 && tmp.getTitle().trim().equals("") && tmp.getQuestion().trim().equals("")) {
                isAvailable = true;
                break;
            }
        }
        if(!isAvailable) {
            cardPosition = 1;
            learningCards.add(0, new LearningCard());
        }
        lvLearningCards.invalidateViews();
    }
}