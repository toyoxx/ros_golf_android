package jp.ac.tohoku.mech.srd.dsquiz;

/**
 * Created by toyo on 05/04/2015. Prueba
 */
public class PatternQuestion {


    //public static int numOptions = 7;
    int rightAnswer;
    //int[] wrongOptions = new int[numOptions];
    int userAnswer;

    public PatternQuestion() {
        rightAnswer = -1;
        userAnswer = -1;
        /*for (int i = 0; i < numOptions; i++) {
            wrongOptions[i] = -1;
        }*/
    }


    //public PatternQuestion(int rightAnswer, int[] options, int userAnswer) {
    public PatternQuestion(int rightAnswer, int userAnswer) {
        this.rightAnswer = rightAnswer;
        //this.wrongOptions = options;
        this.userAnswer = userAnswer;
    }

    public int getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(int rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    public int getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(int userAnswer) {
        this.userAnswer = userAnswer;
    }


    public String writableAnswer() {
        StringBuilder sb = new StringBuilder(userAnswer + ";");
        sb.append(rightAnswer + ";");
        return (sb.toString());
    }
}
