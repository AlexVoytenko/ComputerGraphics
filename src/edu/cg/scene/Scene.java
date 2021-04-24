package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Plain;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; //gets the values of 1, 2 and 3
	private boolean renderRefractions = false;
	private boolean renderReflections = false;
	
	private PinholeCamera camera;
	private Vec ambient = new Vec(0.1, 0.1, 0.1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); //blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();
	
	
	//MARK: initializers
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec,  double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec,  distanceToPlain);
		return this;
	}

	public Scene initCamera(PinholeCamera pinholeCamera) {
		this.camera = pinholeCamera;
		return this;
	}
	
	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}
	
	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}
	
	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}
	
	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}
	
	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}
	
	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}
	
	public Scene initName(String name) {
		this.name = name;
		return this;
	}
	
	public Scene initRenderRefractions(boolean renderRefractions) {
		this.renderRefractions = renderRefractions;
		return this;
	}
	
	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}
	
	//MARK: getters
	public String getName() {
		return name;
	}
	
	public int getFactor() {
		return antiAliasingFactor;
	}
	
	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}
	
	public boolean getRenderRefractions() {
		return renderRefractions;
	}
	
	public boolean getRenderReflections() {
		return renderReflections;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator(); 
		return "Camera: " + camera + endl +
				"Ambient: " + ambient + endl +
				"Background Color: " + backgroundColor + endl +
				"Max recursion level: " + maxRecursionLevel + endl +
				"Anti aliasing factor: " + antiAliasingFactor + endl +
				"Light sources:" + endl + lightSources + endl +
				"Surfaces:" + endl + surfaces;
	}
	
	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	// TODO: add your fields here with the transient keyword
	//  for example - private transient Object myField = null;

	private void initSomeFields(int imgWidth, int imgHeight, double planeWidth, Logger logger) {
		this.logger = logger;
		// TODO: initialize your fields that you added to this class here.
		//      Make sure your fields are declared with the transient keyword
	}
	
	
	public BufferedImage render(int imgWidth, int imgHeight, double planeWidth ,Logger logger)
			throws InterruptedException, ExecutionException, IllegalArgumentException {
		
		initSomeFields(imgWidth, imgHeight, planeWidth, logger);
		
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, planeWidth);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Initialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);
		
		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][])(new Future[imgHeight][imgWidth]);
		
		this.logger.log("Starting to shoot " +
			(imgHeight*imgWidth*antiAliasingFactor*antiAliasingFactor) +
			" rays over " + name);
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);
		
		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}
		
		executor.shutdown();
		
		this.logger.log("Ray tracing of " + name + " has been completed.");
		
		executor = null;
		this.logger = null;
		
		return img;
	}
	
	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			Point pointOnScreen = camera.transform(x, y);
			Vec color = new Vec(0.0);

			Ray ray = new Ray(camera.getCameraPosition(), pointOnScreen);
			color = color.add(calcColor(ray, 0));

			return color.toColor();
			// TODO: change this method for AntiAliasing bonus
			//		You need to shoot antiAliasingFactor-1 additional rays through the pixel return the average color of
			//      all rays.
		});
	}
	
	private Vec calcColor(Ray ray, int recursionLevel) {
		// TODO: implement this method to support ray tracing
		// 		This is the first call to ray ray-tracing
		Hit minHit = findClosestIntersection(ray);
		if (minHit == null) return backgroundColor;

		// ambient calc
		Vec color = minHit.getSurface().Ka().mult(ambient);
		Point hitPoint = ray.getHittingPoint(minHit);



		for (Light light : lightSources) {
			Ray rayToLight = light.rayToLight(hitPoint);

			Vec diffuseColor = calcDiffuseColor(minHit,ray,light);
			Vec specularColor = calcSpecularColor(minHit,ray,light);
			Vec lightIntensity = light.intensity(hitPoint,rayToLight);
			Vec lightColorToAdd = (diffuseColor.add(specularColor)).mult(lightIntensity);
			color = color.add(lightColorToAdd);
		}

		// recursion
		if (recursionLevel == maxRecursionLevel) {
			return null; // ?
		}
		recursionLevel++;

		// reflective and refractive calcs

		return color;
	}

	private Vec calcDiffuseColor(Hit hit, Ray ray, Light light){
		Vec N = hit.getNormalToSurface().normalize();
		Point hitPoint = ray.getHittingPoint(hit);
		Ray rayToLight = light.rayToLight(hitPoint);
		Vec L = rayToLight.direction().normalize();

		double dot = N.dot(L);
		return hit.getSurface().Kd().mult(dot);
	}

	private Vec calcSpecularColor(Hit hit, Ray ray, Light light){
		Vec N = hit.getNormalToSurface().normalize();
		Point hitPoint = ray.getHittingPoint(hit);
		Ray rayToLight = light.rayToLight(hitPoint);
		Vec V = camera.getCameraPosition().sub(hitPoint).normalize();
		Vec reflection = Ops.reflect(rayToLight.direction(), N);

		int n = hit.getSurface().shininess();
		double viewerPointOfView = V.dot(reflection);
		double shiniesLevel = Math.pow(viewerPointOfView , n);

		return hit.getSurface().Ks().mult(shiniesLevel);
	}

	private Vec calcSpecular(Hit minHit, Ray rayToLight, Vec ks, Vec V, Surface hittingSurface) {
		Vec R = Ops.reflect(rayToLight.direction(), minHit.getNormalToSurface());



		int n = hittingSurface.shininess();
		double val = V.dot(R);

		if (val > 0) return ks.mult(Math.pow(val, n));
		else return new Vec();
	}


	private Hit findClosestIntersection(Ray ray) {
		Hit minHit = null;

		for (Surface surface : surfaces) {
			if (minHit != null) {
				Hit currHit = surface.intersect(ray);
				if (currHit == null) continue;

				minHit = minHit.compareTo(currHit) < 0 ? minHit : currHit;
			}
			else
			{
				minHit = surface.intersect(ray);
			}
		}

		return minHit;
	}
}
