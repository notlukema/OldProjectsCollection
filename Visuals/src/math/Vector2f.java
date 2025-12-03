package math;

public class Vector2f {

    public float x, y;

    public Vector2f() {
        zero();
    }

    public Vector2f(Vector2f vec) {
        x = vec.getX();
        y = vec.getY();
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void zero() {
        x = 0;
        y = 0;
    }

    public Vector2f clone() {
        return new Vector2f(x, y);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

}
