package net.ko.db;


public class KDataBaseConnectionReaper extends Thread {
    private KDbConnectionPool pool;
    private final long delay=10000;

    KDataBaseConnectionReaper(KDbConnectionPool pool) {
        this.pool=pool;
    }

    public void run() {
        while(true) {
           try {
              sleep(delay);
           } catch( InterruptedException e) { }
           pool.reapConnections();
        }
    }

}
