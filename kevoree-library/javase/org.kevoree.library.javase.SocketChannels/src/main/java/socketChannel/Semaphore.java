package socketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 21/10/11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class Semaphore {
  private int valeur;
  public Semaphore() {
    valeur = 1;
  }
  public synchronized void P() {
    while(valeur == 0) {
      try{
	wait();
      } catch(InterruptedException e) {}
    }
    valeur = 0;
  }
  public synchronized void V() {
    valeur = 1;
    notify();
  }
}
