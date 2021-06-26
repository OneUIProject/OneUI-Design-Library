package com.samsung.android.sdk.pen.util.color;


public class SpenColorMatching {
    public static final double INVALID_DISTANCE = 1200.0d;
    private final float[][] mHSV2 = {new float[]{0.0f, 0.0f, 0.99f}, new float[]{0.0f, 0.11f, 1.0f}, new float[]{25.0f, 0.13f, 1.0f}, new float[]{43.0f, 0.14f, 1.0f}, new float[]{57.0f, 0.14f, 1.0f}, new float[]{98.0f, 0.09f, 1.0f}, new float[]{143.0f, 0.1f, 0.99f}, new float[]{172.0f, 0.11f, 1.0f}, new float[]{212.0f, 0.14f, 1.0f}, new float[]{228.0f, 0.14f, 1.0f}, new float[]{272.0f, 0.14f, 1.0f}, new float[]{302.0f, 0.11f, 1.0f}, new float[]{337.0f, 0.11f, 1.0f}, new float[]{0.0f, 0.0f, 0.9f}, new float[]{0.0f, 0.21f, 1.0f}, new float[]{26.0f, 0.23f, 1.0f}, new float[]{44.0f, 0.25f, 1.0f}, new float[]{57.0f, 0.25f, 1.0f}, new float[]{98.0f, 0.15f, 1.0f}, new float[]{144.0f, 0.18f, 0.98f}, new float[]{172.0f, 0.2f, 1.0f}, new float[]{211.0f, 0.25f, 1.0f}, new float[]{228.0f, 0.25f, 1.0f}, new float[]{272.0f, 0.21f, 1.0f}, new float[]{302.0f, 0.19f, 1.0f}, new float[]{337.0f, 0.2f, 1.0f}, new float[]{0.0f, 0.0f, 0.8f}, new float[]{1.0f, 0.37f, 1.0f}, new float[]{26.0f, 0.36f, 1.0f}, new float[]{45.0f, 0.4f, 1.0f}, new float[]{57.0f, 0.4f, 1.0f}, new float[]{98.0f, 0.24f, 1.0f}, new float[]{144.0f, 0.29f, 0.98f}, new float[]{172.0f, 0.32f, 1.0f}, new float[]{211.0f, 0.4f, 1.0f}, new float[]{227.0f, 0.4f, 1.0f}, new float[]{272.0f, 0.35f, 1.0f}, new float[]{302.0f, 0.3f, 1.0f}, new float[]{337.0f, 0.32f, 1.0f}, new float[]{0.0f, 0.0f, 0.7f}, new float[]{2.0f, 0.51f, 1.0f}, new float[]{26.0f, 0.49f, 1.0f}, new float[]{45.0f, 0.55f, 1.0f}, new float[]{57.0f, 0.55f, 1.0f}, new float[]{98.0f, 0.33f, 1.0f}, new float[]{144.0f, 0.4f, 0.97f}, new float[]{172.0f, 0.44f, 1.0f}, new float[]{211.0f, 0.55f, 1.0f}, new float[]{227.0f, 0.55f, 1.0f}, new float[]{272.0f, 0.48f, 1.0f}, new float[]{302.0f, 0.41f, 1.0f}, new float[]{337.0f, 0.44f, 1.0f}, new float[]{0.0f, 0.0f, 0.6f}, new float[]{0.0f, 0.65f, 1.0f}, new float[]{26.0f, 0.63f, 1.0f}, new float[]{45.0f, 0.66f, 1.0f}, new float[]{57.0f, 0.7f, 1.0f}, new float[]{98.0f, 0.4f, 1.0f}, new float[]{144.0f, 0.49f, 0.96f}, new float[]{172.0f, 0.53f, 1.0f}, new float[]{211.0f, 0.7f, 1.0f}, new float[]{227.0f, 0.7f, 1.0f}, new float[]{270.0f, 0.58f, 1.0f}, new float[]{302.0f, 0.53f, 1.0f}, new float[]{337.0f, 0.56f, 1.0f}, new float[]{0.0f, 0.0f, 0.5f}, new float[]{0.0f, 0.79f, 1.0f}, new float[]{26.0f, 0.76f, 1.0f}, new float[]{45.0f, 0.81f, 1.0f}, new float[]{58.0f, 0.9f, 0.99f}, new float[]{98.0f, 0.6f, 1.0f}, new float[]{144.0f, 0.68f, 0.96f}, new float[]{172.0f, 0.8f, 1.0f}, new float[]{211.0f, 0.8f, 1.0f}, new float[]{227.0f, 0.8f, 1.0f}, new float[]{270.0f, 0.69f, 1.0f}, new float[]{302.0f, 0.6f, 1.0f}, new float[]{337.0f, 0.64f, 1.0f}, new float[]{0.0f, 0.0f, 0.4f}, new float[]{0.0f, 0.93f, 1.0f}, new float[]{26.0f, 0.9f, 1.0f}, new float[]{45.0f, 1.0f, 0.97f}, new float[]{57.0f, 1.0f, 0.97f}, new float[]{97.0f, 0.6f, 0.97f}, new float[]{143.0f, 0.7f, 0.93f}, new float[]{172.0f, 0.8f, 0.97f}, new float[]{210.0f, 0.94f, 1.0f}, new float[]{227.0f, 0.92f, 1.0f}, new float[]{270.0f, 0.8f, 1.0f}, new float[]{302.0f, 0.75f, 1.0f}, new float[]{337.0f, 0.73f, 1.0f}, new float[]{0.0f, 0.0f, 0.3f}, new float[]{0.0f, 0.93f, 0.91f}, new float[]{26.0f, 0.9f, 0.92f}, new float[]{45.0f, 1.0f, 0.92f}, new float[]{57.0f, 1.0f, 0.92f}, new float[]{98.0f, 0.6f, 0.92f}, new float[]{144.0f, 0.75f, 0.87f}, new float[]{172.0f, 0.8f, 0.92f}, new float[]{211.0f, 1.0f, 0.92f}, new float[]{227.0f, 0.97f, 0.92f}, new float[]{270.0f, 0.91f, 1.0f}, new float[]{302.0f, 0.75f, 0.92f}, new float[]{337.0f, 0.75f, 0.93f}, new float[]{0.0f, 0.0f, 0.25f}, new float[]{0.0f, 0.92f, 0.83f}, new float[]{26.0f, 0.9f, 0.85f}, new float[]{45.0f, 1.0f, 0.85f}, new float[]{57.0f, 1.0f, 0.85f}, new float[]{98.0f, 0.6f, 0.85f}, new float[]{144.0f, 0.75f, 0.81f}, new float[]{172.0f, 0.8f, 0.85f}, new float[]{211.0f, 1.0f, 0.85f}, new float[]{227.0f, 1.0f, 0.85f}, new float[]{270.0f, 1.0f, 0.92f}, new float[]{302.0f, 0.75f, 0.85f}, new float[]{337.0f, 0.8f, 0.85f}, new float[]{0.0f, 0.0f, 0.2f}, new float[]{0.0f, 0.93f, 0.72f}, new float[]{26.0f, 0.9f, 0.72f}, new float[]{45.0f, 1.0f, 0.72f}, new float[]{57.0f, 1.0f, 0.72f}, new float[]{98.0f, 0.6f, 0.72f}, new float[]{143.0f, 0.75f, 0.69f}, new float[]{172.0f, 0.81f, 0.72f}, new float[]{211.0f, 1.0f, 0.72f}, new float[]{227.0f, 1.0f, 0.72f}, new float[]{270.0f, 1.0f, 0.8f}, new float[]{302.0f, 0.75f, 0.72f}, new float[]{337.0f, 0.8f, 0.72f}, new float[]{0.0f, 0.0f, 0.145f}, new float[]{0.0f, 0.93f, 0.6f}, new float[]{26.0f, 1.0f, 0.6f}, new float[]{45.0f, 1.0f, 0.6f}, new float[]{57.0f, 1.0f, 0.6f}, new float[]{98.0f, 0.6f, 0.6f}, new float[]{144.0f, 0.75f, 0.57f}, new float[]{171.0f, 0.8f, 0.58f}, new float[]{211.0f, 1.0f, 0.6f}, new float[]{227.0f, 1.0f, 0.6f}, new float[]{270.0f, 1.0f, 0.64f}, new float[]{302.0f, 0.75f, 0.65f}, new float[]{337.0f, 0.8f, 0.62f}, new float[]{0.0f, 0.0f, 0.1f}, new float[]{0.0f, 0.93f, 0.43f}, new float[]{26.0f, 1.0f, 0.43f}, new float[]{39.0f, 1.0f, 0.43f}, new float[]{57.0f, 1.0f, 0.43f}, new float[]{98.0f, 0.6f, 0.42f}, new float[]{144.0f, 0.75f, 0.41f}, new float[]{172.0f, 0.8f, 0.39f}, new float[]{211.0f, 1.0f, 0.43f}, new float[]{227.0f, 1.0f, 0.43f}, new float[]{270.0f, 1.0f, 0.5f}, new float[]{302.0f, 0.75f, 0.43f}, new float[]{338.0f, 0.8f, 0.49f}, new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.94f, 0.25f}, new float[]{26.0f, 1.0f, 0.32f}, new float[]{40.0f, 1.0f, 0.32f}, new float[]{57.0f, 1.0f, 0.32f}, new float[]{99.0f, 0.61f, 0.29f}, new float[]{144.0f, 0.75f, 0.28f}, new float[]{172.0f, 0.81f, 0.29f}, new float[]{211.0f, 1.0f, 0.32f}, new float[]{228.0f, 1.0f, 0.32f}, new float[]{270.0f, 1.0f, 0.38f}, new float[]{302.0f, 0.76f, 0.29f}, new float[]{337.0f, 0.81f, 0.37f}};
    private double mLastMatchedDistanceValue;
    private int mLastMatchedIndex;

    public boolean matchColor(float[] fArr, float[] fArr2) {
        if (fArr == null || fArr2 == null) {
            return false;
        }
        int matchedColorIndex = getMatchedColorIndex(fArr);
        if (matchedColorIndex <= -1) {
            return false;
        }
        System.arraycopy(this.mHSV2[matchedColorIndex], 0, fArr2, 0, 3);
        return true;
    }

    public int getMatchedColorIndex(float[] fArr) {
        int matchHsvColor = matchHsvColor(fArr, false);
        return matchHsvColor;
    }

    private int matchHsvColor(float[] fArr, boolean z) {
        Point3 point3 = new Point3(fArr);
        Point3 point32 = new Point3();
        double d = 1200.0d;
        int i = -1;
        int i2 = 0;
        while (true) {
            float[][] fArr2 = this.mHSV2;
            if (i2 >= fArr2.length) {
                i2 = i;
                break;
            }
            point32.setColor(fArr2[i2][0], fArr2[i2][1], fArr2[i2][2]);
            double distance = point3.getDistance(point32);
            if (distance != 0.0d) {
                if (d > distance) {
                    if (z) {
                        this.mLastMatchedDistanceValue = distance;
                    }
                    i = i2;
                    d = distance;
                }
                i2++;
            } else if (z) {
                this.mLastMatchedDistanceValue = 0.0d;
            }
        }
        if (z) {
            this.mLastMatchedIndex = i2;
        }
        return i2;
    }

    public void clearMatchedData() {
        this.mLastMatchedIndex = -1;
        this.mLastMatchedDistanceValue = 1200.0d;
    }

    public double getResultValue() {
        return this.mLastMatchedDistanceValue;
    }

    public int getResultIndex() {
        return this.mLastMatchedIndex;
    }

    public void getResultColor(float[] fArr) {
        int i;
        if (fArr != null && fArr.length == 3 && (i = this.mLastMatchedIndex) != -1) {
            System.arraycopy(this.mHSV2[i], 0, fArr, 0, 3);
        }
    }

    public boolean matchColor(float[] fArr) {
        clearMatchedData();
        return matchHsvColor(fArr, true) != -1;
    }

    /* access modifiers changed from: private */
    public class Point3 {
        private static final double PI = 3.14159d;
        private double x;
        private double y;
        private double z;

        Point3() {
            this.z = 0.0d;
            this.y = 0.0d;
            this.x = 0.0d;
        }

        Point3(float[] fArr) {
            setColor(fArr[0], fArr[1], fArr[2]);
        }

        /* access modifiers changed from: package-private */
        public void setColor(float f, float f2, float f3) {
            double d = f2;
            double d2 = (float) ((((double) f) * PI) / 180.0d);
            this.x = (float) (Math.cos(d2) * d);
            this.y = (float) (d * Math.sin(d2));
            this.z = f3;
        }

        /* access modifiers changed from: package-private */
        public double getDistance(Point3 point3) {
            return Math.sqrt(Math.pow(Math.abs(this.x - point3.x), 2.0d) + Math.pow(Math.abs(this.y - point3.y), 2.0d) + Math.pow(Math.abs(this.z - point3.z), 2.0d));
        }
    }
}
