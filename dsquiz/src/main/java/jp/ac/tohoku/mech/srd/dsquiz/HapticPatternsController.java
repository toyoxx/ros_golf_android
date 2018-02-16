package jp.ac.tohoku.mech.srd.dsquiz;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by toyo on 10/03/2015.
 */
public class HapticPatternsController {
    private static final String TAG = HapticPatternsController.class.getSimpleName();
    //static int numPatterns = 24;
    static int[] directions = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300, 310, 320, 330, 340, 350};
    //static int[] directions = {0, 45, 90};
    static int repetitions= 2;
    private static HapticPatternsController ourInstance = new HapticPatternsController();
    int currentQuestion = 0;
    PatternQuestion[] questions;

    private HapticPatternsController() {
        questions = new PatternQuestion[directions.length*repetitions];
        for (int i = 0; i < directions.length*repetitions; i++) {
            questions[i] = new PatternQuestion();
        }
        currentQuestion = 0;
    }

    public static int getNumQuestions() {
        return directions.length*repetitions;
        }

    public static HapticPatternsController getInstance() {
        return ourInstance;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public PatternQuestion[] getQuestions() {
        return questions;
    }

    public void setQuestions(PatternQuestion[] questions) {
        this.questions = questions;
    }

    public void advanceQuestion() {
        this.currentQuestion++;
    }

    public void initializeQuestions() {
        ArrayList<Integer> options;
        options = new ArrayList<Integer>();
        for (int i = 0; i < directions.length; i++) {
            for (int j = 0; j < repetitions ; j++ ){
                options.add(directions[i]);
            }
        }
        Collections.shuffle(options);

        for (int i = 0; i < directions.length*repetitions; i++) {
            questions[i].setRightAnswer(options.get(i));
        }
    }

    public void setCurrentAnswer(int val){
        questions[currentQuestion].setUserAnswer(val);
    }

    public boolean hasFinished(){
        if(currentQuestion == directions.length*repetitions)
            return true;
        return false;
    }

    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < directions.length*repetitions; i++) {
            b.append(questions[i].writableAnswer());
            b.append("\n");

        }
        return b.toString();
    }

    public void logStatus() {
        Log.d(TAG, "ans ; right ans ");
        for (int i = 0; i < directions.length*repetitions; i++) {
            Log.d(TAG, questions[i].writableAnswer());
        }
    }


}
