import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class Creature {

	double x;
	double y;
	double velX;
	double velY;
	int width = 10;
	int height = 10;
	
	int tester = 0;
	
	int visionSize = 3;
	double displacement = 500.0;
	int disp = 75;
	int numLoops;
	int numLoops2 = 0;
	boolean repulsed = false;
	boolean swapped = false;
	boolean annoying;
	boolean increasing = true;
	boolean red = false;
	boolean green = false;
	boolean blue = false;
	int species;
	
	int repulseCounter = 0;
	
	Color cColor = new Color(0, 0, 0);
	
	ArrayList<Integer> sections = new ArrayList<Integer>();
	ArrayList<Integer> visionSections = new ArrayList<Integer>();
	
	Rectangle cRect;
	Rectangle visionRect;
	Polygon vision;
	Random rand;
	Main main;
	
	double effect = 50.0;
	
	public Creature(Main m) {
		
		main = m;
		
		rand = new Random();
		cRect = new Rectangle();
		vision = new Polygon();
		visionRect = new Rectangle();
		annoying = rand.nextBoolean();
		species = rand.nextInt(3);
		
		if(species == 0) cColor = new Color(249, 0, 0);
		if(species == 1) cColor = new Color(0, 249, 0);
		if(species == 2) cColor = new Color(0, 0, 249);
		
	}
	
	public void reset() {
		
		velX = (rand.nextBoolean() ? rand.nextInt(disp) / displacement : -rand.nextInt(disp) / displacement);
		velY = (rand.nextBoolean() ? rand.nextInt(disp) / displacement : -rand.nextInt(disp) / displacement);
		x = rand.nextInt(main.FRAME_WIDTH);
		y = rand.nextInt(main.FRAME_HEIGHT);
		numLoops = 0;
		species = rand.nextInt(3);
		
	}
	
	public void tick() {
		
		x += velX;
		y += velY;
		numLoops++;
		numLoops2++;
		
		cRect.setBounds((int)x, (int)y, width, height);
		vision.reset();
		
		visionRect.setBounds((int)(cRect.getCenterX() - (cRect.getWidth() * 2)),
				(int)(cRect.getCenterY() - (cRect.getHeight() * 2)),
				(int)cRect.getWidth() * 4,
				(int)cRect.getHeight() * 4);
		
		if(numLoops > 1) {
			velX = velX - (rand.nextBoolean() ? rand.nextInt(disp) / displacement : -rand.nextInt(disp) / displacement);
			velY = velY - (rand.nextBoolean() ? rand.nextInt(disp) / displacement : -rand.nextInt(disp) / displacement);
			numLoops = 0;
		}
		
		if(numLoops2 > 300) {
			
			repulsed = !repulsed;
			if(repulsed) { swapped = false; }
			numLoops2 = 0;
			
		}
		
		sections.clear();
		visionSections.clear();
		
		for(int i = 0; i < main.sectRects.length; i++) {
			
			if(this.cRect.intersects(main.sectRects[i])) {
				
				sections.add(i);
				
			}
			
			if(this.visionRect.intersects(main.sectRects[i])) {
				
				visionSections.add(i);
				
			}
			
		}
		
		for(int i = 0; i < visionSections.size(); i++) {
			
			for(Creature c : main.creaturesInSection[visionSections.get(i)]) {
				
				if(visionRect.intersects(c.cRect)) {
					
					this.velX = ((c.velX) + this.velX * effect * 1) / (effect + 1.0);
					this.velY = ((c.velY) + this.velY * effect * 1) / (effect + 1.0);
					
					if(cColor.getRed() > 250) red = false; else { red = true; }
					if(cColor.getGreen() > 250) green = false; else { green = true; }
					if(cColor.getBlue() > 250) blue = false; else { blue = true; }
						
					if(c.species == 0) {
						
						cColor = new Color(red ? cColor.getRed() + 1 : cColor.getRed(),
								cColor.getGreen(),
								cColor.getBlue() > 10 ? cColor.getBlue() - 1 : cColor.getBlue());
						
					}
					
					if(c.species == 1) {
						
						cColor = new Color(cColor.getRed() > 10 ? cColor.getRed() - 1 : cColor.getRed(),
								green ? cColor.getGreen() + 1 : cColor.getGreen(),
								cColor.getBlue());
						
					}
					
					if(c.species == 2) {
						
						cColor = new Color(cColor.getRed(),
								cColor.getGreen() > 10 ? cColor.getGreen() - 1 : cColor.getGreen(),
								blue ? cColor.getBlue() + 1 : cColor.getBlue());
						
					}
					
				}
				
			}
			
		}
		
		if(y > Main.FRAME_HEIGHT || y < 0) { velY = -velY; }
		if(x > Main.FRAME_WIDTH || x < 0) { velX = -velX; }
		
	}
	
	public void paint(Graphics2D g) {
		
		cColor = new Color(cColor.getRed(), cColor.getGreen(), cColor.getBlue(), 200);
		
		g.setColor(cColor);
		g.fillRect((int)x, (int)y, width, height);
		g.setColor(Color.GREEN);
		
	}
	
	public void drawRadialGradient(Graphics2D g, double pointX, double pointY, double radius, Color innerColor, Color outerColor) {
		
		double numInterp = 200;
		
		Color newColor;
		
		for(int n = 0; n < numInterp; n++) {
			
			double r = radius*2 - ((radius*2 / numInterp)) * n;
			
			int red = (int)(outerColor.getRed() + (innerColor.getRed() / numInterp) * n);
			int green = (int)(outerColor.getGreen() + (innerColor.getGreen() / numInterp) * n);
			int blue = (int)(outerColor.getBlue() + (innerColor.getBlue() / numInterp) * n);
			
			red = red > 255 ? 255 : red;
			green = green > 255 ? 255 : green;
			blue = blue > 255 ? 255 : blue;
			
			newColor = new Color(red, green, blue, (int)(255 / numInterp) * n);
			
			g.setColor(newColor);
			
			g.drawOval((int)(pointX - r/2), (int)(pointY - r/2), (int)(r), (int)(r));
			
			for(int i = 0; i < 360; i++) {
				
				/*
				
				tempX = getCircXPoint(pointX, pointY, radius - ((radius / numInterp) * n), i);
				tempY = getCircYPoint(pointX, pointY, radius - ((radius / numInterp) * n), i);
				
				circXPoints[i] = tempX;
				circYPoints[i] = tempY;
				
				g.fillRect((int)tempX, (int)tempY, 1, 1);
				
				*/
				
			}
		
		}
		
	}
	
	public double getCircXPoint(double pointX, double pointY, double radius, double locale) {
		double point;
		
		point = radius*Math.cos(locale) + pointX;
		return point;
		
	}
	
	public double getCircYPoint(double pointX, double pointY, double radius, double locale) {
		double point;
		
		point = radius*Math.sin(locale) + pointY;
		return point;
		
	}
	
	public void setX(int i) { this.x = i; }
	public void setY(int i) { this.y = i; }
	public void setEffect(double e) { this.effect = e; }
	
	public int getX() { return (int)x; }
	public int getY() { return (int)y; }
	public Color getColor() { return cColor; }
	
}
