package client;

import center.Center;

public class Timer implements Runnable {

    private final RequestEntity entity;

    public Timer(RequestEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(Center.getClientConfig().getTimeout());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (entity) {
            entity.notifyAll();
        }
    }
}
