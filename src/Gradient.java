import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

public class Gradient {

	int numPoints = 16;
	public HashMap<Integer, Color> points = new HashMap<Integer, Color>();
	public Rectangle[] colorRects = new Rectangle[numPoints];
	
	Main main;
	
	public Gradient(Main m) {
		
		this.main = m;
		init();
		
	}
	
	public void init() {
		
		for(int i = 0; i < numPoints; i++) {
			
			points.put(i, Color.BLACK);
			
		}
		
		int counter = 0;
		
		for(int y = 0; y + (Main.frameHeight / Math.sqrt(numPoints)) - 1 < Main.frameHeight; y += (Main.frameHeight / Math.sqrt(numPoints))) {
			
			for(int x = 0; x + (Main.frameWidth / Math.sqrt(numPoints)) - 1 < Main.frameWidth; x += (Main.frameWidth / Math.sqrt(numPoints))) {
				
				colorRects[counter] = new Rectangle();
				colorRects[counter].setBounds(x, y, (int)(Main.frameWidth / Math.sqrt(numPoints)), (int)(Main.frameHeight / Math.sqrt(numPoints)));
				
				counter++;
			}
			
		}
		
	}
	
	public void tick() {
		
		int section = 0;
		
		for(int i = 0; i < numPoints; i++) {
			
			points.put(i, Color.BLACK);
			
		}
		
		for(int i = 0; i < numPoints; i++) {
			
			section = main.getSection((int)colorRects[i].getCenterX(), (int)colorRects[i].getCenterY());
			
			for(int n = 0; n < main.creaturesInSection[section].size(); n++) {
				
				if(main.creaturesInSection[section].get(n).cRect.intersects(colorRects[i].getCenterX(), colorRects[i].getCenterY(), 1, 1)) {
					
					points.put(i, main.creaturesInSection[section].get(n).getColor());
					
				}
				
			}
			
			if(points.size() == 0) {
				
				points.put(i, new Color(0, 0, 0));
				
			}
			
		}
		
	}
	
	public void paint(Graphics2D g) {
		
		for(int i = 0; i < colorRects.length; i++) {
			
			g.setColor(points.get(i));
			g.fill(colorRects[i]);
			
		}
		
	}
	
}
