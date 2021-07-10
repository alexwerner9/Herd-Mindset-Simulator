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

	public static int frameWidth = 1000;
	public static int frameHeight = 650;
	
	int numSections = 169;
	int numCreatures = 3000;
	int blurRadius = 3;
	
	double ticks = 180.0;
	
	boolean gridVis = false;
	
	public Creature[] creatures = new Creature[numCreatures];
	public Creature[][] sectCreatures = new Creature[2][numCreatures];
	public Rectangle[] sectRects = new Rectangle[numSections];
	public ArrayList<Creature>[] creaturesInSection = new ArrayList[numSections];
	public ArrayList<Creature>[] visionInSection = new ArrayList[numSections];
	
	public float[] blurArray = new float[(int) Math.pow(blurRadius, 2)];
	BufferedImageOp blur;
	BufferedImageOp op;
	Graphics2D g2;
	Graphics g;
	Kernel kernel;
	
	int numFrames = 3600;
	BufferedImage frames[] = new BufferedImage[numFrames];
	int framesAdded = 0;
	int frameOn = 0;
	boolean makeNewFrames = true;
	
	int[][] pixels = new int[frameWidth][frameHeight];
	int[][] pixels2 = new int[frameWidth][frameHeight];
	
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
		frame.setSize(frameWidth, frameHeight);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		
	}
	
	public void run() {
		
		System.out.println("Started Thread");
		
		rand = new Random();
		
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
	
	public void initSections() {
		
		int count = 0;
		
		for(int y = 0; y + (frameHeight / Math.sqrt(numSections) - 1) < frameHeight; y += frameHeight / Math.sqrt(numSections)) {
			
			for(int x = 0; x + (frameWidth / Math.sqrt(numSections)) - 1 < frameWidth; x += frameWidth / Math.sqrt(numSections)) {
				
				sectRects[count] = new Rectangle();
				sectRects[count].setBounds(x, y, (int)(frameWidth / Math.sqrt(numSections)), (int)(frameHeight / Math.sqrt(numSections)));
				
				count++;
				
			}
			
		}
		
		System.out.println(count);
		
	}
	
	public void initBlur() {
		
		for(int i = 0; i < Math.pow(blurRadius, 2); i++) {
			
			blurArray[i] = 1.0f / 1.0f;
			
		}
		
		graphicsImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		kernel = new Kernel(blurRadius, blurRadius, blurArray);
		op = new ConvolveOp(kernel);
		g2 = (Graphics2D) graphicsImage.getGraphics();
		
	}
	
	public void startLoop() {
		;
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
				//tick();
				render();
				FPScounter++;
				delta--;
			}
			
			if(now - FPStimer >= 1000000000) {
				
				//System.out.println(FPScounter);
				FPScounter = 0;
				FPStimer = System.nanoTime();
				
			}
			
		}
		
	}
	
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
	
	public void blur() {
		for(int y = 1; y < frameHeight - 1; y++) {
			
			for(int x = 1; x < frameWidth - 1; x++) {
				
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
		for(int y = 1; y < frameHeight; y++) {
			
			for(int x = 1; x < frameWidth; x++) {
				
				pixels[x][y] = pixels2[x][y];
				
			}
			
		}
	}
	
	public void render() {
		
		panel.repaint();
		
	}
	
	public void initCreatures() {
		
		for(int i = 0; i < numCreatures; i++) {
			
			creatures[i] = new Creature(this);
			creatures[i].setX(rand.nextInt(frameWidth));
			creatures[i].setY(rand.nextInt(frameHeight));
			
		}
		
	}
	
	public int getSection(int x, int y) {
		
		int bx = (int)Math.ceil(x / sectRects[0].width);
		int by = (int)Math.ceil(y / sectRects[0].height);
		
		return by * (int)(Math.sqrt(numSections)) + bx;
	}
	
	class JPanelClass extends JPanel {
		private static final long serialVersionUID = 1L;
		
		BufferedImage image = new BufferedImage(frameWidth + 100, frameHeight + 100, BufferedImage.TYPE_INT_RGB);
		
		Random rand;
		
		Graphics graphics;
		Main main;
		
		public JPanelClass(Main m, Graphics g) {
			
			rand = new Random();
			
			main = m;
			graphics = g;
			
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
			
			if(makeNewFrames) {
			
				tick();
				
				g2 = (Graphics2D) graphicsImage.getGraphics();
				
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, frameWidth, frameHeight);
				
				//gradientClass.paint(g2);
				for(int i = 0; i < numCreatures; i++) creatures[i].paint(g2);
				
				if(gridVis) for(Rectangle r : sectRects) { g2.draw(r); }
				for(int i = 0; i < sectRects.length; i++) {
					
					//g2.drawString(Integer.toString(i), (int)sectRects[i].getX() + 10, (int)sectRects[i].getY() + 10);
					
				}
				
				//graphicsImage = op.filter(graphicsImage, null);
				
				/*
				
				File imageFile = new File("C:\\Users\\Alex\\Desktop\\Eclipse Workspace\\Patters2\\frames\\image"+framesAdded+".jpg");
				try {
					ImageIO.write(copyImage(graphicsImage), "jpg", imageFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println("added " + framesAdded);
				framesAdded++;
				
				if(framesAdded > numFrames) { makeNewFrames = false; ticks = 30.0; }
				
				*/
				
				g.drawImage(graphicsImage, 0, 0, null);
			
			} else {
				
				if(frameOn > frames.length - 5) frameOn = 0;
				
				try {
					g.drawImage(ImageIO.read(new File("C:\\Users\\Alex\\Desktop\\Eclipse Workspace\\Patters2\\frames\\image"+frameOn+".jpg")), 0, 0, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
				frameOn++;
				
			}
			
		}
		
	}
	
	public static BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return b;
	}
	
}
