package math;

public class Vector2i {

    public int x, y;

    public Vector2i() {
        zero();
    }

    public Vector2i(Vector2i vec) {
        x = vec.getX();
        y = vec.getY();
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void zero() {
        x = 0;
        y = 0;
    }

    public Vector2i clone() {
        return new Vector2i(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

}
