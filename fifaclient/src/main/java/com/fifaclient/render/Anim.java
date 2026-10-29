package com.fifaclient.render;

/** Frame-rate independent smoothing value. Call to() once per frame. */
public final class Anim {
    public float value;
    private long last = System.nanoTime();

    public Anim() {
    }

    public Anim(float start) {
        this.value = start;
    }

    public float to(float target, float speed) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1_000_000_000f);
        last = now;
        value += (target - value) * (1f - (float) Math.exp(-speed * dt));
        if (Math.abs(target - value) < 0.001f) value = target;
        return value;
    }

    public void snap(float v) {
        value = v;
        last = System.nanoTime();
    }
}
