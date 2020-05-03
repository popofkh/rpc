package timer;

import client.RequestEntity;

import java.util.TimerTask;

public class RequestTimerTask extends TimerTask {
    private final RequestEntity entity;

    public RequestTimerTask(RequestEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        synchronized (entity) {
            entity.notifyAll();
        }
    }
}
