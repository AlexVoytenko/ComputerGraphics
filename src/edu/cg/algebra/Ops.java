package edu.cg.algebra;

public class Ops {
	public static final double epsilon = 1e-5;
	public static final double infinity = 1e8;
	
	public static double dot(Vec u, Vec v) {
		return u.x*v.x + u.y*v.y + u.z*v.z;
	}
	
	public static Vec cross(Vec u, Vec v) {
		return new Vec((u.y*v.z - u.z*v.y), (u.z*v.x - u.x*v.z), (u.x*v.y - u.y*v.x));
	}
	
	public static Vec mult(double a, Vec v) {
		return mult(new Vec(a), v);
	}
	
	public static Vec mult(Vec u, Vec v) {
		return new Vec(u.x*v.x, u.y*v.y, u.z*v.z);
	}
	
	public static Point mult(double a, Point p) {
		return mult(new Point(a), p);
	}
	
	public static Point mult(Point p1, Point p2) {
		return new Point(p1.x*p2.x, p1.y*p2.y, p1.z*p2.z);
	}
	
	public static double normSqr(Vec v) {
		return dot(v, v);
	}
	
	public static double norm(Vec v) {
		return Math.sqrt(normSqr(v));
	}
	
	public static double lengthSqr(Vec v) {
		return normSqr(v);
	}
	
	public static double length(Vec v) {
		return norm(v);
	}
	
	public static double dist(Point p1, Point p2) {
		return length(sub(p1, p2));
	}
	
	public static double distSqr(Point p1, Point p2) {
		return lengthSqr(sub(p1, p2));
	}
	
	public static Vec normalize(Vec v) {
		return mult(1.0/norm(v), v);
	}
	
	public static Vec neg(Vec v) {
		return mult(-1, v);
	}
	
	public static Vec add(Vec u, Vec v) {
		return new Vec(u.x+v.x, u.y+v.y, u.z+v.z);
	}
	
	public static Point add(Point p, Vec v) {
		return new Point(p.x+v.x, p.y+v.y, p.z+v.z);
	}
	
	public static Point add(Point p1, Point p2) {
		return new Point(p1.x+p2.x, p1.y+p2.y, p1.z+p2.z);
	}
	
	public static Point add(Point p, double t, Vec v) {
		//returns p + tv;
		return add(p, mult(t, v));
	}
	
	public static Vec sub(Point p1, Point p2) {
		return new Vec(p1.x-p2.x, p1.y-p2.y, p1.z-p2.z);
	}
	
	public static boolean isFinite(Vec v) {
		return Double.isFinite(v.x) & Double.isFinite(v.y) & Double.isFinite(v.z);
	}

	public static boolean isFinite(Point p) {
		return Double.isFinite(p.x) & Double.isFinite(p.y) & Double.isFinite(p.z);
	}
	
	public static Vec reflect(Vec u, Vec normal) {
		return add(u, mult(-2*dot(u, normal), normal));
	}
	
	public static Vec refract(Vec u, Vec normal, double n1, double n2) {
		//TODO: Bonus implementation
		// Snell's law: n1*sin(theta1) = n2*sin(theta2)
		return null;
	}
}
