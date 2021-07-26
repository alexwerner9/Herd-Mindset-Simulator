import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main implements Runnable {

	public static int FRAME_WIDTH = 1000;
	public static int FRAME_HEIGHT = 650;
	public static int NUM_SECTIONS = 169;
	public static int numCreatures = 3000;
	
	static final double ticks = 180.0;
	
	Creature[] creatures = new Creature[numCreatures];
	Creature[][] sectCreatures = new Creature[2][numCreatures];
	Rectangle[] sectRects = new Rectangle[NUM_SECTIONS];
	ArrayList<Creature>[] creaturesInSection = new ArrayList[NUM_SECTIONS];
	ArrayList<Creature>[] visionInSection = new ArrayList[NUM_SECTIONS];
	
	int blurRadius = 3;
	public float[] blurArray = new float[(int) Math.pow(blurRadius, 2)];
	BufferedImageOp blur;
	BufferedImageOp op;
	Graphics2D g2;
	Graphics g;
	Kernel kernel;
	
	int[][] pixels = new int[FRAME_WIDTH][FRAME_HEIGHT];
	int[][] pixels2 = new int[FRAME_WIDTH][FRAME_HEIGHT];
	
	JFrame frame;
	JPanel panel;
	BufferedImage graphicsImage;
	Thread mainThread;
	Random rand;
	
	boolean running = true;
	
	public static void main(String[] args) {
		
		Main m = new Main();
		new Thread(m).start();
		
	}
	
	public Main() {
		frame = new JFrame("3D");
		panel = new JPanelClass(this, g);
		
		frame.setVisible(true);
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		
		rand = new Random();
		
	}
	
	//Start loop
	public void run() {
		
		initCreatures();
		initSections();
		initBlur();
		
		for(int i = 0; i < creaturesInSection.length; i++) {
			creaturesInSection[i] = new ArrayList<Creature>();
		}
		for(int i = 0; i < visionInSection.length; i++) {
			visionInSection[i] = new ArrayList<Creature>();
		}
		
		startLoop();
		
	}
	
	//Main loop
	public void startLoop() {
		
		long lastTime = System.nanoTime();
		double delta = 0;
		double ns = 1000000000 / ticks;
		long now = 0;
		
		int FPScounter = 0;
		long FPStimer = System.nanoTime();
		
		while(running) {
			now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			
			if(delta >= 1) {
				tick();
				render();
				FPScounter++;
				delta--;
			}
			
			if(now - FPStimer >= 1000000000) {
				FPScounter = 0;
				FPStimer = System.nanoTime();
			}
			
		}
		
	}
	
	//Ticks
	private void tick() {
		
		for(int i = 0; i < creaturesInSection.length; i++) {
			try {
				creaturesInSection[i].clear();
			} catch(NullPointerException e) {}
		}
		
		for(int i = 0; i < numCreatures; i++) {
			creatures[i].tick();
			for(int n = 0; n < creatures[i].sections.size(); n++) {
				creaturesInSection[creatures[i].sections.get(n)].add(creatures[i]);
			}
		}
		
	}
	
	public void render() {
		
		panel.repaint();
		
	}
	
	//Creates creature arrays
	public void initCreatures() {
		
		for(int i = 0; i < numCreatures; i++) {
			
			creatures[i] = new Creature(this);
			creatures[i].setX(rand.nextInt(FRAME_WIDTH));
			creatures[i].setY(rand.nextInt(FRAME_HEIGHT));
			
		}
		
	}
	
	//Splits screen into sections for faster performance
	public void initSections() {
		
		int count = 0;
		
		for(int y = 0; y + (FRAME_HEIGHT / Math.sqrt(NUM_SECTIONS) - 1) < FRAME_HEIGHT; y += FRAME_HEIGHT / Math.sqrt(NUM_SECTIONS)) {
			
			for(int x = 0; x + (FRAME_WIDTH / Math.sqrt(NUM_SECTIONS)) - 1 < FRAME_WIDTH; x += FRAME_WIDTH / Math.sqrt(NUM_SECTIONS)) {
				
				sectRects[count] = new Rectangle();
				sectRects[count].setBounds(x, y, (int)(FRAME_WIDTH / Math.sqrt(NUM_SECTIONS)), (int)(FRAME_HEIGHT / Math.sqrt(NUM_SECTIONS)));
				
				count++;
				
			}
			
		}
		
	}
	
	//Experiment in blurring screen to see effect
	public void initBlur() {
		
		for(int i = 0; i < Math.pow(blurRadius, 2); i++) {
			
			blurArray[i] = 1.0f / 1.0f;
			
		}
		
		graphicsImage = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
		kernel = new Kernel(blurRadius, blurRadius, blurArray);
		op = new ConvolveOp(kernel);
		g2 = (Graphics2D) graphicsImage.getGraphics();
		
	}
	
	public void blur() {
		for(int y = 1; y < FRAME_HEIGHT - 1; y++) {
			for(int x = 1; x < FRAME_WIDTH - 1; x++) {
				
				pixels2[x][y] = (int)((
						
						pixels[x-1][y-1] +
						pixels[x][y-1] +
						pixels[x+1][y-1] +
						pixels[x-1][y] +
						pixels[x][y] +
						pixels[x+1][y] +
						pixels[x-1][y+1] +
						pixels[x][y+1] +
						pixels[x+1][y+1]
						
						)/9.0);
				
			}
		}
		
		for(int y = 1; y < FRAME_HEIGHT; y++) {
			for(int x = 1; x < FRAME_WIDTH; x++) {
				pixels[x][y] = pixels2[x][y];
			}
		}
	}
	
	
	//Returns section based on point
	public int getSection(int x, int y) {
		int bx = (int)Math.ceil(x / sectRects[0].width);
		int by = (int)Math.ceil(y / sectRects[0].height);
		
		return by * (int)(Math.sqrt(NUM_SECTIONS)) + bx;
	}
	
	class JPanelClass extends JPanel {
		private static final long serialVersionUID = 1L;
		
		Main main;
		BufferedImage image = new BufferedImage(FRAME_WIDTH + 100, FRAME_HEIGHT + 100, BufferedImage.TYPE_INT_RGB);
		Graphics graphics;
		
		Random rand;
		boolean gridVis = false;
		
		public JPanelClass(Main m, Graphics g) {
			
			rand = new Random();
			main = m;
			graphics = g;
			
			//JComponents for live control over results
			JButton reset = new JButton();
			reset.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					
					for(Creature c : creatures) {
						
						c.reset();
						
					}
					
				}
				
			});
			reset.setText("RESET");
			this.add(reset);
			
			JButton showGrid = new JButton();
			showGrid.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					gridVis = !gridVis;
					
				}
			});
			showGrid.setText("Show Grid");
			this.add(showGrid);
			
			JSlider effectSlider = new JSlider(0, 150);
			effectSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					
					for(Creature c : creatures) {
						c.setEffect(effectSlider.getValue());
					}
					
				}
				
			});
			effectSlider.setName("Effect");
			this.add(effectSlider);
			
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			boolean gridVis = false;
			
			g2 = (Graphics2D) graphicsImage.getGraphics();
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
			
			for(int i = 0; i < numCreatures; i++) creatures[i].paint(g2);
			
			if(gridVis) for(Rectangle r : sectRects) { g2.draw(r); }
			
			g.drawImage(graphicsImage, 0, 0, null);
			
		}
		
	}
	
}
