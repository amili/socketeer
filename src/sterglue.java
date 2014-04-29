import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * @author Alen Milincevic
 *
 * Ment to be used for protocol other than STOMP (TBD).
 *
 */

public class sterglue extends TimerTask {

	Timer timer = new Timer(true);
	sterconfig conf = new sterconfig();
	
	boolean shouldBeStopped = false;
	
	void setConfig(sterconfig conf) {
		this.conf = conf;
	}
	
	void startTimer() {
		timer.schedule(this, 1000);
		shouldBeStopped = false;
	}

	void shouldStop() {
		shouldBeStopped = true;
	}
	
	public void run() {
		// do the job, check all channels for messages, trigger and send pending messages
		
		// reshedule (TODO: some breaking)
		if (shouldBeStopped == false) {timer.schedule(this, 1000);}

	}


}
