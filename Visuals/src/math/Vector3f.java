package math;

public class Vector3f extends Vector2f {

    public float z;

    public Vector3f() {
        zero();
    }

    public Vector3f(Vector3f vec) {
        super(vec.getX(), vec.getY());
        z = vec.getZ();
    }

    public Vector3f(float x, float y, float z) {
        super(x, y);
        this.z = z;
    }

    public void zero() {
        super.zero();
        z = 0;
    }

    @Override
    public Vector3f clone() {
        return new Vector3f(x, y, z);
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getR() {
        return x;
    }

    public float getG() {
        return y;
    }

    public float getB() {
        return z;
    }

    public void setR(float r) {
        x = r;
    }

    public void setG(float g) {
        y = g;
    }

    public void setB(float b) {
        z = b;
    }

}
