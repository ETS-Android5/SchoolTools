package de.domjos.schooltools.core.model.learningCard;

import android.content.Context;
import de.domjos.schooltools.activities.MainActivity;
import de.domjos.schooltools.core.model.objects.BaseCategoryObject;
import de.domjos.schooltools.helper.Helper;

import java.util.LinkedList;
import java.util.List;

public class LearningCardQuery extends BaseCategoryObject {
    private LearningCardGroup learningCardGroup;
    private int priority;
    private int period;
    private int tries;
    private LearningCardQuery wrongCardsOfQuery;
    private boolean periodic;
    private boolean untilDeadLine;
    private boolean answerMustEqual;
    private boolean showNotes;
    private boolean showNotesImmediately;

    public LearningCardQuery() {
        super();
        this.learningCardGroup = null;
        this.priority = 0;
        this.period = 1;
        this.tries = 1;
        this.wrongCardsOfQuery = null;
        this.periodic = false;
        this.untilDeadLine = false;
        this.answerMustEqual = true;
        this.showNotes = false;
        this.showNotes = false;
        this.showNotesImmediately = false;
    }

    public LearningCardGroup getLearningCardGroup() {
        return this.learningCardGroup;
    }

    public void setLearningCardGroup(LearningCardGroup learningCardGroup) {
        this.learningCardGroup = learningCardGroup;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPeriod() {
        return this.period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getTries() {
        return this.tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

    public LearningCardQuery getWrongCardsOfQuery() {
        return this.wrongCardsOfQuery;
    }

    public void setWrongCardsOfQuery(LearningCardQuery wrongCardsOfQuery) {
        this.wrongCardsOfQuery = wrongCardsOfQuery;
    }

    public boolean isPeriodic() {
        return this.periodic;
    }

    public void setPeriodic(boolean periodic) {
        this.periodic = periodic;
    }

    public boolean isUntilDeadLine() {
        return this.untilDeadLine;
    }

    public void setUntilDeadLine(boolean untilDeadLine) {
        this.untilDeadLine = untilDeadLine;
    }

    public boolean isAnswerMustEqual() {
        return this.answerMustEqual;
    }

    public void setAnswerMustEqual(boolean answerMustEqual) {
        this.answerMustEqual = answerMustEqual;
    }

    public boolean isShowNotes() {
        return this.showNotes;
    }

    public void setShowNotes(boolean showNotes) {
        this.showNotes = showNotes;
    }

    public boolean isShowNotesImmediately() {
        return this.showNotesImmediately;
    }

    public void setShowNotesImmediately(boolean showNotesImmediately) {
        this.showNotesImmediately = showNotesImmediately;
    }

    public List<LearningCard> loadLearningCards(Context context) {
        List<LearningCard> learningCards = new LinkedList<>();
        try {
            return MainActivity.globals.getSqLite().getLearningCards(this);
        } catch (Exception ex) {
            Helper.printException(context, ex);
        }
        return learningCards;
    }
}