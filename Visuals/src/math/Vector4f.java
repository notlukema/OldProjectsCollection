package math;

public class Vector4f extends Vector3f {

    public float w;

    public Vector4f() {
        zero();
    }

    public Vector4f(Vector4f vec) {
        super(vec.getX(), vec.getY(), vec.getZ());
        w = vec.getW();
    }

    public Vector4f(float x, float y, float z, float w) {
        super(x, y, z);
        this.w = w;
    }

    public void zero() {
        super.zero();
        w = 0;
    }

    @Override
    public Vector4f clone() {
        return new Vector4f(x, y, z, w);
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getA() {
        return w;
    }

    public void setA(float a) {
        w = a;
    }

}
