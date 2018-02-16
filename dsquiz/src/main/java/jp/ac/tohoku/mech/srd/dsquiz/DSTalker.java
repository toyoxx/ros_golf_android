package jp.ac.tohoku.mech.srd.dsquiz;

/**
 * Created by jose on 2017/06/29.
 */
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;


public class DSTalker extends AbstractNodeMain{
    private Publisher<haptic_base.DSValues> publisher;
    @Override

    public GraphName getDefaultNodeName() { return GraphName.of("ds_values_talker"); }

    @Override
    public void onStart(ConnectedNode connectedNode) {
         publisher = connectedNode.newPublisher("ds_values", haptic_base.DSValues._TYPE);

    }

    public void testSend(){
        haptic_base.DSValues val = publisher.newMessage();
        val.setDsAngle((short)10);
        publisher.publish(val);
    }

    public void sendDS(int direction){
        haptic_base.DSValues val = publisher.newMessage();
        val.setDsAngle((short) direction);
        val.setDsDur(Duration.fromMillis(250*5));
        publisher.publish(val);
    }
}

