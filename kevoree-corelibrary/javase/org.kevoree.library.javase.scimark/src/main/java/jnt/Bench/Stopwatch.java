/*****************************************************************************
jnt.Bench.Stopwatch
 *****************************************************************************/
package jnt.Bench;
/**
   Provides a stopwatch to measure elapsed time.
<DL>
<DT><B>Example of use:</B></DT>
<DD>
<pre>
	Stopwatch Q = new Stopwatch;
	Q.start();
	//
	// code to be timed here ...
	//
	Q.stop();
	System.out.println("elapsed time was: " + Q.read() + " seconds.");
</pre>	

@author Roldan Pozo
@version 14 October 1997
*/
public class Stopwatch {
  private boolean running;
  private long last_time;
  private long total;

  public Stopwatch() {
    reset(); }
		
  /** 
    * Return system time (in seconds)
    */
  public void reset() { 
    running = false; 
    last_time = 0; 
    total=0; }

  /** 
    * Resume timer.
    */
  public void resume() { 
    if (!running) { 
      last_time = System.currentTimeMillis(); 
      running = true; }}

  /** 
    * Start (and reset) timer
    */
  public void start() { 
    total=0;
    last_time = System.currentTimeMillis(); 
    running = true; }
   
  /** 
    * Stop timer
    */
  public double stop() { 
    if (running) {
      total += System.currentTimeMillis() - last_time; 
      running = false; }
    return total*0.001; }
 
  /** 
    * Return the elapsed time (in seconds)
    */
  public double read() {  
    if (running) {
      long now = System.currentTimeMillis();
      total += now - last_time;
      last_time = now; }
    return total*0.001; }
}

    

            
