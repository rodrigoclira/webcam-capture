package com.github.sarxos.webcam;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;


/**
 * Paint troll smile on all detected faces.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class FacePainter extends JFrame implements Runnable, WebcamPanel.Painter {

	private static final long serialVersionUID = 1L;

	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
	private static final HaarCascadeDetector detector = new HaarCascadeDetector();
	private static final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f }, 0.0f);

	private Webcam webcam = null;
	private WebcamPanel.Painter painter = null;
	private List<DetectedFace> faces = null;
	private BufferedImage troll = null;
	
	List<BufferedImage> images;
	
	HashMap<Double, BufferedImage> imgsbyPerimetrs = new HashMap<Double, BufferedImage>();
	
	private List<BufferedImage> getResources()
	throws IOException {
		LinkedList<BufferedImage> images  = new LinkedList<BufferedImage>();
		String filenames[] = {
				"cute-giraffe.png", 
				"cute-pinguim.png",
				"cute-santa-face.png",
				"rudolph.png",
				"santa-happy.png",
				"santa-claus-cool.png",
				"santa-claus-movie.png",
				"santa-claus-nerd.png",
				"santa-claus-sleepy.png",
				"santa-claus-stars.png",
				"santa-sunglasses.png",
		};
		BufferedImage img = null;
		for (String filename : filenames){
			img = ImageIO.read(getClass().getResourceAsStream("/" + filename));
			images.add(img);
		}

		return images;
		
	}
	
	private BufferedImage selectImage(){
		Random r = new Random();
		int selected = r.nextInt(images.size()-1);
		return images.get(selected);
	}
	
	public FacePainter() throws IOException {

		super();
		images = getResources();
		//troll = ImageIO.read(getClass().getResourceAsStream("/troll-face.png"));		
			
		troll = ImageIO.read(getClass().getResourceAsStream("/santa-claus-cool.png"));

		webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.open(true);

		WebcamPanel panel = new WebcamPanel(webcam, false);
		panel.setPreferredSize(WebcamResolution.VGA.getSize());
		panel.setPainter(this);
		panel.setFPSDisplayed(false);
		panel.setFPSLimited(true);
		panel.setFPSLimit(20);
		panel.setPainter(this);
		panel.start();

		painter = panel.getDefaultPainter();

		add(panel);

		setTitle("Face Detector");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		EXECUTOR.execute(this);
	}

	@Override
	public void run() {
		while (true) {
			if (!webcam.isOpen()) {
				return;
			}
			faces = detector.detectFaces(ImageUtilities.createFImage(webcam.getImage()));
		}
	}

	@Override
	public void paintPanel(WebcamPanel panel, Graphics2D g2) {
		if (painter != null) {
			painter.paintPanel(panel, g2);
		}
	}

	@Override
	public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {

		if (painter != null) {
			painter.paintImage(panel, image, g2);
		}

		if (faces == null) {
			return;
		}

		Iterator<DetectedFace> dfi = faces.iterator();
		while (dfi.hasNext()) {

			DetectedFace face = dfi.next();
			
			if (face.getConfidence() >= 4.0){
				System.out.println(face.getShape().calculateCentroid());
				System.out.println(face.getShape().calculatePerimeter());
				Rectangle bounds = face.getBounds();
				Double perimeter = face.getShape().calculatePerimeter();
				//int dx = (int) (0.1 * bounds.width);
				//int dy = (int) (0.2 * bounds.height);
				
				int dx = (int) (0.1 * bounds.width);
				int dy = (int) (0.2 * bounds.height);
				
				int x = (int) bounds.x - dx;
				int y = (int) bounds.y - dy;
				int w = (int) bounds.width + 2 * dx;
				int h = (int) bounds.height + 3 * dy;
				//int h = (int) bounds.height + dy;
				
				
				/*
				  BufferedImage img = null;
				if (imgsbyPerimetrs.containsKey(perimeter)){
					img = imgsbyPerimetrs.get(perimeter);
					System.out.println("Contains Image");
				}else{
					img = selectImage();
					imgsbyPerimetrs.put(perimeter, img);
					System.out.println("Sorting Image");
				}
				*/
				g2.drawImage(troll, x, y, w, h, null);
				//g2.drawImage(troll, x, y, w, h, null);
				g2.setStroke(STROKE);
				//g2.setColor(Color.RED);
				//g2.drawRect(x, y, w, h); Removing rectangle
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new FacePainter();
	}
}
