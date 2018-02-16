package jp.ac.tohoku.mech.srd.golftraining;

/**
 * Created by jose on 2017/06/29.
 */
import com.jjoe64.graphview.series.DataPoint;

import org.ros.android.RosActivity;
import org.ros.message.Duration;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.Vector;

import sensor_msgs.Imu;


public class GolfFeedbackTalker extends AbstractNodeMain{
    private Publisher<std_msgs.String> publisher;
    private Subscriber<sensor_msgs.Imu> subscriber;
    private MainActivity activity;
    int counter = 0;
    double gyroBias=81.28525;
    double gyroScaling=0.0175;
    double desiredAngle = -20;

    //Offsets to force data to start from 0
    double offsetTime2=-1;
    //Keep track of previous time for integrating over the Angular velocity
    double currentAngle= 0 ;
    double prevAngle=0;
    double prevTime = -1;
    boolean update = true;




    public GraphName getDefaultNodeName() { return GraphName.of("golf_feedback_talker"); }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher("golf_fb_commands", std_msgs.String._TYPE);
        subscriber = connectedNode.newSubscriber("imu", Imu._TYPE);
        subscriber.addMessageListener(new MessageListener<Imu>() {
            @Override
            public void onNewMessage(Imu imu){
                if (update) {
                    if(offsetTime2<0)
                        offsetTime2 = imu.getHeader().getStamp().toSeconds();

                    double currentTime = imu.getHeader().getStamp().toSeconds()-offsetTime2;
                    if (prevTime>0){
                        currentAngle = prevAngle + (imu.getAngularVelocity().getX()-gyroBias)*(currentTime-prevTime)*gyroScaling;

                    }
                    activity.addPointToVectors(currentTime, imu.getLinearAcceleration().getY()/Math.sqrt(Math.pow(imu.getLinearAcceleration().getX(),2)+Math.pow(imu.getLinearAcceleration().getY(),2)+Math.pow(imu.getLinearAcceleration().getZ(),2)), currentTime, currentAngle);
                    prevTime = currentTime;
                    prevAngle = currentAngle;
                    counter ++;

                    if (counter == 20){
                        synchronized (activity){
                            activity.addPointToSeries(currentTime, imu.getLinearAcceleration().getY()/Math.sqrt(Math.pow(imu.getLinearAcceleration().getX(),2)+Math.pow(imu.getLinearAcceleration().getY(),2)+Math.pow(imu.getLinearAcceleration().getZ(),2)), currentTime, currentAngle);
                            counter = 0;
                        }
                    }
                }
            }
        });

    }

    public void setGyroBias(double g){
        gyroBias=g;
        std_msgs.String val = publisher.newMessage();
        val.setData("set_bias#"+g);
        publisher.publish(val);
    }

    public void setDesiredAngle(double g){
        desiredAngle=g;
        std_msgs.String val = publisher.newMessage();
        val.setData("set_feedback_angle#"+g);
        publisher.publish(val);
    }

    public void setEngaged(){
        std_msgs.String val = publisher.newMessage();
        val.setData("engage_feedback#1");
        publisher.publish(val);
    }




    public void resetData(){
        offsetTime2=-1;
        //Keep track of previous time for integrating over the Angular velocity
        currentAngle= 0 ;
        prevAngle=0;
        prevTime = -1;
        update = true;
        std_msgs.String val = publisher.newMessage();
        val.setData("reset_angle#");
        publisher.publish(val);

    }

    public void stopData(){
        update = false;
    }



    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public void startAccBasedPuttFeedback(){
        std_msgs.String val = publisher.newMessage();
        val.setData("cmd_start_putt_acc_vibration");
        publisher.publish(val);
    }

}

