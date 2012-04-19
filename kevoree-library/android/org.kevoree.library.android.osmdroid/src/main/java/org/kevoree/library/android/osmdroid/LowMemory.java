package org.kevoree.library.android.osmdroid;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 16/04/12
 * Time: 09:14
 */
public class LowMemory {

    /**
     * Free memory seen when caller indicated an out of
     * memory situation. Becomes a low memory watermark
     * for five seconds that causes isLowMemory to return
     * true if free memory is lower than this value.
     * This allows the JVM a chance to recover memory
     * rather than start new operations that are probably
     * doomed to failure due to the low memory.
     *
     */
    private long lowMemory;

    /**
     * Time in ms corresponding to System.currentTimeMillis() when
     * lowMemory was set.
     */
    private long whenLowMemorySet;

    /**
     * Set a low memory watermark where the owner of this object just hit an
     * OutOfMemoryError. The caller is assumed it has just freed up any
     * references it obtained during the operation, so that the freeMemory call
     * as best as it can reflects the memory before the action that caused the
     * OutOfMemoryError, not part way through the action.
     *
     */
    public void setLowMemory() {

        // Can read lowMemory unsynchronized, worst
        // case is that we force extra garbage collection.
        if (lowMemory == 0L) {

            // The caller tried to dereference any objects it
            // created during its instantation. Try to garbage
            // collect these so that we can a best-guess effort
            // at the free memory before the overall operation we are
            // failing on occurred. Of course in active multi-threading
            // systems we run the risk that some other thread just freed
            // up some memory that throws off our calcuation. This is
            // avoided by clearing lowMemory some time later on an
            // isLowMemory() call.
            boolean interrupted = false;

            for (int i = 0; i < 5; i++) {
                System.gc();
                System.runFinalization();
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                // reinstate flag
                Thread.currentThread().interrupt();
            }
        }
        synchronized (this) {
            if (lowMemory == 0L) {
                lowMemory = Runtime.getRuntime().freeMemory();
                whenLowMemorySet = System.currentTimeMillis();
            }
        }
    }

    /**
     * Return true if a low memory water mark has been set and the current free
     * memory is lower than it. Otherwise return false.
     */
    public boolean isLowMemory() {
        synchronized (this) {
            long lm = lowMemory;
            if (lm == 0)
                return false;

            if (Runtime.getRuntime().freeMemory() > lm)
                return false;

            // Only allow an low memory watermark to be valid
            // for five seconds after it was set. This stops
            // an incorrect limit being set for ever. This could
            // occur if other threads were freeing memory when
            // we called Runtime.getRuntime().freeMemory()

            long now = System.currentTimeMillis();
            if ((now - this.whenLowMemorySet) > 5000L) {
                lowMemory = 0L;
                whenLowMemorySet = 0L;
                return false;
            }
            return true;
        }
    }
}