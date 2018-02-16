package jp.ac.tohoku.mech.srd.dsquiz;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.sefford.circularprogressdrawable.CircularProgressDrawable;
import com.triggertrap.seekarc.SeekArc;
import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import jp.ac.tohoku.mech.srd.dsquiz.DSTalker;
import android.widget.Toast;
import android.hardware.Camera;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.format.Formatter;



public class MainActivity extends RosActivity {
    private CircularProgressDrawable drawable;
    ImageView ivDrawable;
    private SeekArc mSeekArc;
    private TextView mSeekArcProgress;
    private TextView questionNumber;
    private ImageView imageView;
    private Button nextButton;
    private EditText name;
    private DSTalker talker;
    Animator currentAnimation;
    private boolean firstTime = true;
    private int currentProgress;
    private static final Logger logger = LoggerFactory.getLogger();

    public MainActivity() {
        super("CameraTutorial", "CameraTutorial");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NoisePlayer.getInstance().playNoise(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mSeekArc = (SeekArc) findViewById(R.id.seekArc);
        imageView = (ImageView) findViewById(R.id.arrow);
        mSeekArcProgress = (TextView) findViewById(R.id.seekArcVal);
        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress,
                                          boolean fromUser) {
                mSeekArcProgress.setText("Direction: "+progress+"°");
                imageView.setRotation((float)-1*progress);
                currentProgress = progress;
            }
        });


        ivDrawable = (ImageView) findViewById(R.id.spinner);
        drawable = new CircularProgressDrawable.Builder()
                .setRingWidth(getResources().getDimensionPixelSize(R.dimen.drawable_ring_size))
                .setOutlineColor(getResources().getColor(android.R.color.darker_gray))
                .setRingColor(getResources().getColor(android.R.color.holo_green_light))
                .setCenterColor(getResources().getColor(android.R.color.holo_blue_dark))
                .create();
        ivDrawable.setImageDrawable(drawable);
        ivDrawable.setVisibility(View.INVISIBLE);
        nextButton = (Button) findViewById(R.id.nextButton);
        questionNumber = (TextView) findViewById(R.id.questionNumber);
        name = (EditText) findViewById(R.id.nameText);
        HapticPatternsController.getInstance().initializeQuestions();




    }

    private Animator prepareStyle1Animation() {
        AnimatorSet animation = new AnimatorSet();

        final Animator indeterminateAnimation = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.PROGRESS_PROPERTY, 0, 3600);
        indeterminateAnimation.setDuration(3600);

        Animator innerCircleAnimation = ObjectAnimator.ofFloat(drawable, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0f, 0.75f);
        innerCircleAnimation.setDuration(3600);
        innerCircleAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                drawable.setIndeterminate(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                indeterminateAnimation.end();
                drawable.setIndeterminate(false);
                drawable.setProgress(0);
                ivDrawable.setVisibility(View.INVISIBLE);


                mSeekArc.setEnabled(true);
                mSeekArc.setProgress(0);
                nextButton.setEnabled(true);
                questionNumber.setText("Question "+(HapticPatternsController.getInstance().getCurrentQuestion()+1));
                //advanceToQuestionScreen();
                //Decide what to do after

            }
        });

        animation.playTogether(innerCircleAnimation, indeterminateAnimation);
        return animation;
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        talker = new DSTalker();
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(talker, nodeConfiguration);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            //talker.testSend();
        }
        return true;
    }

    public void answerPressed(View view) {
        if (firstTime) {
            firstTime = false;
            nextButton.setText("Next");
        }else{
            //Registrar la pregunta
            HapticPatternsController.getInstance().setCurrentAnswer(currentProgress);
            HapticPatternsController.getInstance().advanceQuestion();
        }

        nextButton.setEnabled(false);
        mSeekArc.setProgress(0);
        mSeekArc.setEnabled(false);

        if(!HapticPatternsController.getInstance().hasFinished()){
            ivDrawable.setVisibility(View.VISIBLE);
            currentAnimation = prepareStyle1Animation();
            currentAnimation.start();

            talker.sendDS(HapticPatternsController.getInstance().getQuestions()[HapticPatternsController.getInstance().getCurrentQuestion()].getRightAnswer());
        }else{
            questionNumber.setText("Thank you!");
            logger.setLevel(Level.INFO);
            logger.addAppender(new LogCatAppender());
            FileAppender fileAppender = new FileAppender();
            fileAppender.setAppend(true);
            fileAppender.setFormatter(new Formatter() {
                @Override
                public String format(String s, String s2, long l, Level level, Object o, Throwable throwable) {
                    return (String)o;
                }

                @Override
                public String[] getPropertyNames() {
                    return new String[0];
                }

                @Override
                public void setProperty(String s, String s2) {

                }
            });
            DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = "DSTest_"+name.getText()+"_"+df.format(today)+".dat";
            fileAppender.setFileName(reportDate);
            logger.addAppender(fileAppender);
            logger.info(HapticPatternsController.getInstance().toString());

        }
        HapticPatternsController.getInstance().logStatus();



    }

}