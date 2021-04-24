package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;

import java.util.ArrayList;


// TODO Implement this class which represents an axis aligned box
public class AxisAlignedBox extends Shape{
    private final static int NDIM=3; // Number of dimensions
    private Point a = null;
    private Point b = null;
    private double[] aAsArray;
    private double[] bAsArray;

    public AxisAlignedBox(Point a, Point b){
        this.a = a;
        this.b = b;
        // We store the points as Arrays - this could be helpful for more elegant implementation.
        aAsArray = a.asArray();
        bAsArray = b.asArray();
        assert (a.x <= b.x && a.y<=b.y && a.z<=b.z);
    }

    @Override
    public String toString() {
        String endl = System.lineSeparator();
        return "AxisAlignedBox:" + endl +
                "a: " + a + endl +
                "b: " + b + endl;
    }

    public AxisAlignedBox initA(Point a){
        this.a = a;
        aAsArray = a.asArray();
        return this;
    }

    public AxisAlignedBox initB(Point b){
        this.b = b;
        bAsArray = b.asArray();
        return this;
    }

    @Override
    public Hit intersect(Ray ray) {
        // TODO Implement:
        Plain[] plains = new Plain[6];

        Vec volume = b.sub(a);
        Vec volX = new Vec(volume.x , 0 , 0);
        Vec volY = new Vec(0 , volume.y , 0);
        Vec volZ = new Vec(0 , 0 , volume.z);

        plains[0] = new Plain(volX, a);
        plains[1] = new Plain(volY, a);
        plains[2] = new Plain(volZ, a);
        plains[3] = new Plain(volX, b);
        plains[4] = new Plain(volY, b);
        plains[5] = new Plain(volZ, b);

        Hit minHit = null;

        for (Plain p : plains ) {
            if (minHit != null) {
                Hit currHit = p.intersect(ray);
                if (currHit == null) continue;

                minHit = minHit.compareTo(currHit) < 0 ? minHit : currHit;
            }
            else {
                minHit = p.intersect(ray);
            }
        }

        return minHit;
    }
}

