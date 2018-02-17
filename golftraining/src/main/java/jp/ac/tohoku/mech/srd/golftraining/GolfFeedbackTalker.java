package jp.ac.tohoku.mech.srd.golftraining;

/**
 * Created by jose on 2017/06/29.
 */

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import geometry_msgs.Vector3Stamped;


public class GolfFeedbackTalker extends AbstractNodeMain{
    private Publisher<std_msgs.String> publisher;
    private Subscriber<geometry_msgs.Vector3Stamped> subscriber;
    private MainActivity activity;
    int counter = 0;
    double desiredAngle = -20;

    //Offsets to force data to start from 0
    double rpyDataOffsetTime =-1;
    //Keep track of previous time for integrating over the Angular velocity
    double curPuttShaftAngle = 0 ;
    double curPuttFaceAngle= 0 ;
    double zOffset = 0;
    boolean update = true;




    public GraphName getDefaultNodeName() { return GraphName.of("golf_feedback_talker"); }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher("golf_fb_commands", std_msgs.String._TYPE);
        subscriber = connectedNode.newSubscriber("putt_rpy", geometry_msgs.Vector3Stamped._TYPE);
        subscriber.addMessageListener(new MessageListener<Vector3Stamped>() {
            @Override
            public void onNewMessage(Vector3Stamped imu){
                if (update) {
                    if(rpyDataOffsetTime <0)
                        rpyDataOffsetTime = imu.getHeader().getStamp().toSeconds();
                    double currentTime = imu.getHeader().getStamp().toSeconds()- rpyDataOffsetTime;
                    curPuttShaftAngle = imu.getVector().getX();
                    curPuttFaceAngle = (imu.getVector().getZ()-zOffset);
                    activity.addPointToVectors(currentTime, curPuttFaceAngle, currentTime, curPuttShaftAngle);
                    counter ++;

                    if (counter == 20){
                        activity.addPointToSeries(currentTime, curPuttFaceAngle, currentTime, curPuttShaftAngle);
                        counter = 0;

                    }
                }
            }
        });

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
        rpyDataOffsetTime =-1;
        //Keep track of previous time for integrating over the Angular velocity
        curPuttShaftAngle = 0 ;
        zOffset = curPuttFaceAngle+zOffset;
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

