package timer;

import client.RequestEntity;

import java.util.TimerTask;

public class RequestTimer extends TimerTask {
    private final RequestEntity entity;

    public RequestTimer(RequestEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        synchronized (entity) {
            entity.notifyAll();
        }
    }
}
