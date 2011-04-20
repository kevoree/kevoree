package org.kevoree.library.javase.kinect.osc;

import java.util.Calendar;

public class Debug {

    /**
     * Writes a message to System.out.println in the format
     * [mm/dd/yy hh:mm:ss] message.
     * @param   activity    The message.
    */
    public static void writeActivity(String activity) {

	// comment this line to turn on the debug output
	if (true) return;

        // --- get the current date and time
        Calendar cal = Calendar.getInstance();
        activity = "[" + cal.get(Calendar.MONTH) 
                 + "/" + cal.get(Calendar.DAY_OF_MONTH) 
                 + "/" + cal.get(Calendar.YEAR) 
                 + " " 
                 + cal.get(Calendar.HOUR_OF_DAY) 
                 + ":" + cal.get(Calendar.MINUTE) 
                 + ":" + cal.get(Calendar.SECOND) 
                 + "] " + activity + "\n";

        // --- display the activity
        System.out.print(activity);
    }

}
