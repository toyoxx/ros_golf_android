package jp.ac.tohoku.mech.srd.golftraining;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.sefford.circularprogressdrawable.CircularProgressDrawable;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
/*import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.format.Formatter;
*/



public class MainActivity extends RosActivity {
    private CircularProgressDrawable drawable;
    //private static final Logger logger = LoggerFactory.getLogger();
    enum State {BUSY, FREE};
    State currentState = State.FREE;
    GolfFeedbackTalker golfTalker;
    GraphView graph;
    GraphView graph2;
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;

    private Vector<DataPoint> desiredPuttFaceAngle, desiredPuttShaftAngle, currentPuttFaceAngle, currentPuttShaftAngle;

    double offsetTime = -1;

    public enum PuttPhase {IDLE, BS, DS, FT};
    public class PointOfInterest {
        public int index;
        public double value;
        public PointOfInterest(int i, double v) {
            this.index = i;
            this.value = v;
        }
    }

    public class PuttPhasePoints{
        public PointOfInterest start = new PointOfInterest(-1, Double.MAX_VALUE) ;
        public PointOfInterest min= new PointOfInterest(-1, Double.MAX_VALUE);
        public PointOfInterest hit= new PointOfInterest(-1, Double.MAX_VALUE);
        public PointOfInterest max = new PointOfInterest(-1, Double.MIN_VALUE);
    }


    public void setupDataSeries(){
        desiredPuttFaceAngle = new Vector<DataPoint>();
        desiredPuttShaftAngle = new Vector<DataPoint>();
        currentPuttFaceAngle = new Vector<DataPoint>();
        currentPuttShaftAngle = new Vector<DataPoint>();

        graph = (GraphView) findViewById(R.id.chart);
        readProcessedFile("rpy4mMod.csv", desiredPuttShaftAngle, desiredPuttFaceAngle);
        LineGraphSeries<DataPoint> acc = vectorToDataPoint(desiredPuttFaceAngle);
        LineGraphSeries<DataPoint> gyro = vectorToDataPoint(desiredPuttShaftAngle);


        acc.setTitle("Putter Face Angle alpha");
        acc.setColor(Color.BLUE);
        acc.setDrawDataPoints(true);
        acc.setDataPointsRadius(1);
        acc.setThickness(3);
        acc.setDrawAsPath(true);
        gyro.setTitle("Putter angle Theta");
        gyro.setColor(Color.RED);
        gyro.setDrawDataPoints(true);
        gyro.setDataPointsRadius(1);
        gyro.setThickness(3);
        gyro.setDrawAsPath(true);
        gyro.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(nodeMainExecutorService, "Series1: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
        graph.addSeries(acc);
        graph.getSecondScale().addSeries(gyro);
        graph.getSecondScale().setMinY(-50);
        graph.getSecondScale().setMaxY(50);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-50);
        graph.getViewport().setMaxY(50);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        graph.getGridLabelRenderer().setHighlightZeroLines(false);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);

        graph2 = (GraphView) findViewById(R.id.chart2);
        mSeries2 = new LineGraphSeries<>();
        mSeries2.setColor(Color.BLUE);
        mSeries2.setTitle("Putter Face Angle alpha");
        mSeries2.setDataPointsRadius(1);
        mSeries2.setThickness(3);
        mSeries2.setDrawAsPath(true);
        mSeries3 = new LineGraphSeries<>();
        mSeries3.setColor(Color.RED);
        mSeries3.setTitle("Putter angle Theta");
        mSeries3.setDataPointsRadius(1);
        mSeries3.setThickness(3);
        mSeries3.setDrawAsPath(true);
        graph2.addSeries(mSeries2);
        graph2.getSecondScale().addSeries(mSeries3);
        graph2.getSecondScale().setMinY(-50);
        graph2.getSecondScale().setMaxY(50);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(10);
        graph2.getViewport().setMinY(-50);
        graph2.getViewport().setMaxY(50);
        graph2.getGridLabelRenderer().setHighlightZeroLines(false);
        graph2.getLegendRenderer().setVisible(true);
        graph2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        //graph2.getViewport().setScrollable(true);
        //graph2.getViewport().setScalableY(true);

        /*Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        acc.setCustomPaint(paint);
        gyro.setCustomPaint(paint);*/


        PuttPhasePoints p = markPoints(desiredPuttShaftAngle, 40);
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        double bsTime = (desiredPuttShaftAngle.get(p.min.index).getX() - desiredPuttShaftAngle.get(p.start.index).getX());
        double dsTime = (desiredPuttShaftAngle.get(p.hit.index).getX() - desiredPuttShaftAngle.get(p.min.index).getX());
        ((TextView)findViewById(R.id.statisticsDesired)).setText("Backswing time "+ numberFormat.format(bsTime *1000)+ " Downswing time "+ numberFormat.format(dsTime*1000) + " Tempo ratio : 1:"+numberFormat.format(bsTime/dsTime) + "Angle: "+numberFormat.format(p.min.value)+" deg");
        //System.out.println("Values "+p.start.index+"/"+p.min.index+"/"+p.hit.index+"/"+p.max.index+"/");

    }

    public MainActivity() {
        super("Golf Feedback", "Golf Feedback");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setupDataSeries();



    }

    protected void init(NodeMainExecutor nodeMainExecutor) {
        golfTalker = new GolfFeedbackTalker();
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(golfTalker, nodeConfiguration);
        golfTalker.setActivity(this);

    }

    public void startVibration(View view){
        if (currentState == State.FREE){
            golfTalker.startAccBasedPuttFeedback();
        }
    }

    public void resetMeasurement(View view){
        DataPoint data[] = new DataPoint[1];
        data[0]=new DataPoint(0,0);
        mSeries2.resetData(data);
        mSeries3.resetData(data);
        golfTalker.resetData();
        graph2.getViewport().setScrollable(false);
        graph2.getViewport().setScalable(false);
        mSeries3.setOnDataPointTapListener(null);
        currentPuttFaceAngle.clear();
        currentPuttShaftAngle.clear();
        ((TextView)findViewById(R.id.statisticsCurrent)).setText("Current: ");
    }

    public void stopMeasurement(View view){
        golfTalker.stopData();
        graph2.getViewport().setScrollable(true);
        graph2.getViewport().setScalable(true);
        mSeries3.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(nodeMainExecutorService, "Series1: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setAngle(View view){
        EditText t = (EditText) findViewById(R.id.angleValue);
        ((TextView)findViewById(R.id.angleText)).setText("Desired angle: "+Double.valueOf(t.getText().toString()));
        double b = Double.valueOf(t.getText().toString());
        golfTalker.setDesiredAngle(b);
    }

    public void setEngaged(View view){
        golfTalker.setEngaged();
    }

    public void analyzePutt(View view){
        PuttPhasePoints p = markPoints(currentPuttShaftAngle, 40);
        double bsTime = 0;
        double dsTime = 0;
        if (p.min.index > 0){
            //((TextView)findViewById(R.id.statisticsCurrent)).setText("Current: "+p.start.index+"/"+p.min.index+"/"+p.hit.index+"/"+p.max.index+"/");
            bsTime = (currentPuttShaftAngle.get(p.min.index).getX() - currentPuttShaftAngle.get(p.start.index).getX());
            dsTime = (currentPuttShaftAngle.get(p.hit.index).getX() - currentPuttShaftAngle.get(p.min.index).getX());
            DecimalFormat numberFormat = new DecimalFormat("#.00");
            ((TextView)findViewById(R.id.statisticsCurrent)).setText("Backswing time "+ numberFormat.format(bsTime *1000)+ " Downswing time "+ numberFormat.format(dsTime*1000) + " Tempo ratio : 1:"+numberFormat.format(bsTime/dsTime)+" Putt Angle: "+numberFormat.format(p.min.value)+" deg");

        }else{
            ((TextView)findViewById(R.id.statisticsCurrent)).setText("Current: "+p.start.index+"/"+p.min.index+"/"+p.hit.index+"/"+p.max.index+"/");
        }
        boolean writeToFile = ((CheckBox)findViewById(R.id.saveFile)).isChecked();
        if (writeToFile)
            logDataToFile(p, bsTime, dsTime);

    }

    public void logDataToFile(PuttPhasePoints pts, double bst, double dst){
        Writer writer;
        File root = Environment.getExternalStorageDirectory();
        File outDir = new File(root.getAbsolutePath() + File.separator + "LogIROS2018");
        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }

        DateFormat df = new SimpleDateFormat("MM_dd_HH_mm_ss");
        Date today = Calendar.getInstance().getTime();
        TextView name = (TextView) findViewById(R.id.userText);
        String reportDate = "IMULog_"+name.getText()+"_"+df.format(today)+".csv";
        //fileAppender.setFileName(reportDate);

        try {
            if (!outDir.isDirectory()) {
                throw new IOException(
                        "Unable to create directory LogIROS. Maybe the SD card is mounted?");
            }
            File outputFile = new File(outDir, reportDate);

            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write("%Index of Start/MinPeak/Hit/Max\n");
            writer.write("%"+pts.start.index+","+pts.min.index+","+pts.hit.index+","+pts.max.index+"\n");
            writer.write("%BS Time, DS Time, ratio\n");
            writer.write("%"+bst+","+dst+","+(bst/dst)+"\n");
            writer.write("Desired Angle "+golfTalker.curPuttShaftAngle +"/Current angle"+ currentPuttShaftAngle.get(pts.min.index).getY());

            for (int i = 0; i< currentPuttShaftAngle.size(); i++)
            {
                writer.write(currentPuttShaftAngle.get(i).getX()+","+ currentPuttShaftAngle.get(i).getY()+","+ currentPuttFaceAngle.get(i).getY()+"\n");
            }
            writer.close();
        } catch (IOException e) {
            Log.w("eztt", e.getMessage(), e);
            Toast.makeText(nodeMainExecutorService, e.getMessage() + " Unable to write to external storage.",
                    Toast.LENGTH_LONG).show();
        }
    }

    //Deprecated: Used to read old custom CSV. From now on, processed CSV only read in the readProcessedFile Funciton
    private LineGraphSeries<DataPoint> readData(String fileName, int index, Vector <DataPoint> vec){
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(fileName)));
            String line;

            while((line = reader.readLine()) != null)
            {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    if (offsetTime < 0)
                        offsetTime= Double.valueOf(parts[0]);
                    vec.add(new DataPoint((Double.valueOf(parts[0])-offsetTime)/1000000000.0, Double.valueOf(parts[index])));
                }
            }
            for (DataPoint p : vec){
                series.appendData(p, true, vec.size());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return series;
    }

    private void readProcessedFile(String fileName, Vector <DataPoint> gyro, Vector <DataPoint> acc ){
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(fileName)));
            String line;
            while((line = reader.readLine()) != null)
            {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    if (offsetTime < 0)
                        offsetTime= Double.valueOf(parts[0]);
                    gyro.add(new DataPoint((Double.valueOf(parts[0])-offsetTime)/1000000000.0, Double.valueOf(parts[1])));
                    acc.add(new DataPoint((Double.valueOf(parts[0])-offsetTime)/1000000000.0, Double.valueOf(parts[2])));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LineGraphSeries<DataPoint> vectorToDataPoint(Vector<DataPoint> v){
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        for (DataPoint p : v){
            series.appendData(p, true, v.size());
        }
        return series;
    }

    public PuttPhasePoints markPoints(Vector<DataPoint> v, int countThreshold){
        PuttPhasePoints points = new PuttPhasePoints();
        double previousValuePos =0;
        int numSamples = 0;

        PuttPhase phase = PuttPhase.IDLE;
        int currentSign = 0;
        int prevSign = 0;
        int counterSign = 0;

        for (DataPoint p : v){

            double diff = p.getY() - previousValuePos;
            if (diff == 0)
                currentSign = 0;
            else if (diff<0)
                currentSign = -1;
            else if (diff > 0)
                currentSign = 1;
            switch(phase)
            {
                case IDLE:
                    if (numSamples > 0){
                        //Conditions: 1. Same sign, less than zero (decreasing)
                        //&& ( fabs((diff)*100.0/(float)previousValuePos) > 5.0)
                        if (currentSign == prevSign && currentSign < 0 )
                        {
                            counterSign++;
                        }
                        else{
                            counterSign=0;
                        }

                        //Conditions met 20 times -> change phase
                        //numSamples-counterSign > 10 &&
                        if ( counterSign >=countThreshold)
                        {
                            //ROS_INFO(" %d %f",numSamples,fabs(v[numSamples-counterSign].second.y*100/par.second.y));
                            if (Math.abs(v.get(numSamples-counterSign).getY() - p.getY()) > 4.0){
                                points.start.index = numSamples-counterSign;
                                points.start.value = v.get(numSamples-counterSign).getY();
                                phase = PuttPhase.BS;
                                System.out.println("ID->BS at "+ numSamples);
                            }else{
                                counterSign = 0;
                            }
                        }
                    }
                    break;
                case BS:
                    if (currentSign != prevSign && currentSign > 0 )
                    {
                        points.min.index = numSamples;
                        points.min.value = p.getY();
                        phase = PuttPhase.DS;
                        System.out.println("BS->DS at "+ numSamples);
                    }
                    break;
                case DS:
                    if (previousValuePos <0 && p.getY() > 0)
                    {
                        points.hit.index = numSamples;
                        points.hit.value = p.getY();
                        phase = PuttPhase.FT;
                        //ROS_INFO("DS->FT at %d", numSamples);
                        System.out.println("DS->FT at "+ numSamples);
                    }
                    break;
                case FT:
                {
                    if ((points.max.index == -1 || points.max.value <  p.getY()) ){
                        points.max.index = numSamples;
                        points.max.value = p.getY();
                    }
                }

            }


            previousValuePos = p.getY();
            prevSign = currentSign;
            numSamples++;
        }
        return points;
    }

    public void addPointToSeries(double x1, double y1, double x2, double y2){
            synchronized (mSeries2) {
                mSeries2.appendData(new DataPoint(x1, y1), true, 12000);
            }
            synchronized (mSeries3) {
                mSeries3.appendData(new DataPoint(x2, y2), true, 12000);
            }
    }

    public void addPointToVectors(double x1, double y1, double x2, double y2){
        currentPuttFaceAngle.add(new DataPoint(x1, y1));
        currentPuttShaftAngle.add(new DataPoint(x2, y2));

    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            //talker.testSend();
        }
        return true;
    }

}