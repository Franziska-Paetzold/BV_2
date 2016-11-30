
// BV Ue2 WS2016/17 Vorgabe
//
// Copyright (C) 2015 by Klaus Jung

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class Perspective extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "<Jacky Lai, Franziska Paetzold>"; // TODO: type in your
														// name here
	private static final String initialFilename = "59009_512.jpg";
	private static final File openPath = new File(".");
	private static final int maxWidth = 920;
	private static final int maxHeight = 920;
	private static final int border = 10;
	private static final double angleStepSize = 5.0; // size used for angle
														// increment and
														// decrement

	private static JFrame frame;

	private ImageView srcView = null; // source image view
	private ImageView dstView = null; // rotated image view

	private JComboBox<String> methodList; // the selected interpolation method
	private JSlider angleSlider; // the selected angle
	private JLabel statusLine; // to print some status text
	private double angle = 0.0; // current angle in degrees

	/**
	 * Constructor. Constructs the layout of the GUI components and loads the
	 * initial image.
	 */
	public Perspective() {
		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initialFilename);

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
		initDstView();

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					initDstView();
					calculatePerspective(false);
				}
			}
		});

		// selector for the rotation method
		String[] methodNames = { "Nearest Neighbour", "Bilinear Interpolation" };

		methodList = new JComboBox<String>(methodNames);
		methodList.setSelectedIndex(0); // set initial method
		methodList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculatePerspective(false);
			}
		});

		// rotation angle minus button
		JButton decAngleButton = new JButton("-");
		decAngleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				angle -= angleStepSize;
				if (angle < 0)
					angle += 360;
				angleSlider.setValue((int) angle);
				calculatePerspective(false);
			}
		});

		// rotation angle plus button
		JButton incAngleButton = new JButton("+");
		incAngleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				angle += angleStepSize;
				if (angle > 360)
					angle -= 360;
				angleSlider.setValue((int) angle);
				calculatePerspective(false);
			}
		});

		// rotation angle slider
		angleSlider = new JSlider(0, 360, (int) angle);
		angleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				angle = angleSlider.getValue();
				calculatePerspective(false);
			}
		});

		// speed test button
		JButton speedTestButton = new JButton("Speed Test");
		speedTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long startTime = System.currentTimeMillis();
				double lastAngle = angle;
				int cnt = 0;
				for (angle = 0; angle < 360; angle += angleStepSize) {
					calculatePerspective(true);
					cnt++;
				}
				long time = System.currentTimeMillis() - startTime;
				statusLine.setText("Speed Test: Calculated " + cnt + " perspcetives in " + time + " ms");
				angle = lastAngle;
			}
		});

		// some status text
		statusLine = new JLabel("   ");

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, border, 0, 0);
		controls.add(load, c);
		controls.add(methodList, c);
		controls.add(decAngleButton, c);
		controls.add(angleSlider, c);
		controls.add(incAngleButton, c);
		controls.add(speedTestButton, c);

		// arrange images
		JPanel images = new JPanel();
		images.add(srcView);
		images.add(dstView);

		// add to main panel
		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);

		// add border to main panel
		setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

		// perform the initial rotation
		calculatePerspective(false);
	}

	/**
	 * Set up and show the main frame.
	 */
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Perspective - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent contentPane = new Perspective();
		contentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(contentPane);

		// display the window
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            - ignored. No arguments are used by this application.
	 */
	public static void main(String[] args) {
		// schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/**
	 * Open file dialog used to select a new image.
	 * 
	 * @return The selected file object or null on cancel.
	 */
	private File openFile() {
		// file open dialog
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png",
				"gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}

	/**
	 * Initialize the destination view giving it the correct size.
	 */
	private void initDstView() {
		// set destination size large enough to view a substantial part of the
		// perspective

		int width = (int) (srcView.getImgWidth() * 1.4);
		int height = (int) (srcView.getImgHeight() * 1.2);

		// create an empty destination image
		if (dstView == null)
			dstView = new ImageView(width, height);
		else
			dstView.resetToSize(width, height);

		// limit viewing dimensions
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));

		frame.pack();
	}

	/**
	 * Calculate image perspective and show result in destination view.
	 * 
	 * @param silent
	 *            - set true when running the speed test (suppresses the image
	 *            view).
	 */
	protected void calculatePerspective(boolean silent) {

		if (!silent) {
			// present some useful information
			statusLine.setText("Angle = " + angle + " degrees.");
		}

		// get dimensions and pixels references of images
		int srcPixels[] = srcView.getPixels();
		int srcWidth = srcView.getImgWidth();
		int srcHeight = srcView.getImgHeight();
		int dstPixels[] = dstView.getPixels();
		int dstWidth = dstView.getImgWidth();
		int dstHeight = dstView.getImgHeight();

		long startTime = System.currentTimeMillis();

		switch (methodList.getSelectedIndex()) {
		case 0: // Nearest Neigbour
			calculateNearestNeigbour(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, angle);
			break;
		case 1: // Bilinear Interpolation
			calculateBilinear(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, angle);
			break;
		}

		if (!silent) {
			// show processing time
			long time = System.currentTimeMillis() - startTime;
			statusLine.setText("Angle = " + angle + " degrees. Processing time = " + time + " ms.");
			// show the resulting image
			dstView.applyChanges();
		}
	}

	/**
	 * Image perspective algorithm using nearest neighbour image rendering
	 * 
	 * @param srcPixels
	 *            - source image pixel array of loaded image (ARGB values)
	 * @param srcWidth
	 *            - source image width
	 * @param srcHeight
	 *            - source image height
	 * @param dstPixels
	 *            - destination image pixel array to be filled (ARGB values)
	 * @param dstWidth
	 *            - destination image width
	 * @param dstHeight
	 *            - destination image height
	 * @param degrees
	 *            - angle in degrees for the perspective
	 */
	void calculateNearestNeigbour(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth,
			int dstHeight, double degrees) {

		/**** TODO: your implementation goes here ****/
		
		double sinus = Math.sin(Math.toRadians(degrees));
		double cosinus = Math.cos(Math.toRadians(degrees));

		// Schleife ¸ber Zielbild um korrespondierende Quellkoordinaten zu ermitteln
		for (int yDst = 0; yDst < dstHeight; yDst++) {
			for (int xDst = 0; xDst < dstWidth; xDst++) {
				
				int dstPos = yDst * dstWidth + xDst; // Aktueller Pixel im Zielbild

				
				// Positionierung des Koordinatenursprungs auf Bildmittelpunkt des Zielbildes
				double xCentered = xDst - (dstWidth/2);
				double yCentered = yDst -(dstHeight/2);
				
				
				// Perspektivische Verzerrung
//				int ySrc = (int) Math.round(yCentered * (0.001 * sinus * yCentered + 1) / cosinus);
				double ySrc = yCentered/(cosinus-0.001*sinus*yCentered);
				double xSrc = xCentered * (0.001 * sinus * ySrc + 1);
				
				// R¸ckpositionierung um Width & Height / 2 des Quellbildes
				xSrc = xSrc + (srcWidth/2);
				ySrc = ySrc + (srcHeight/2);
				
				int srcPos = (int)ySrc * srcWidth + (int)xSrc; // Aktueller Pixel im Quellbild
				

				// Randbehandlung
				int rgb = 255;
				// Wenn aktueller Pixel die Maﬂe des Quellbildes ¸berschreitet -> weiﬂ
				if (((ySrc < 0) || (ySrc >= srcHeight)) || (xSrc < 0) || (xSrc >= srcWidth)) {
					dstPixels[dstPos] = (0xff << 24) | (rgb << 16) | (rgb << 8) | rgb;
				// Sonst: Farbwerte korrespondierender Positionen im Quellbild ¸bernehmen
				} else {
					dstPixels[dstPos] = srcPixels[srcPos];
				}

			}
		}

	}

	/**
	 * Image perspective algorithm using bilinear interpolation
	 * 
	 * @param srcPixels
	 *            - source image pixel array of loaded image (ARGB values)
	 * @param srcWidth
	 *            - source image width
	 * @param srcHeight
	 *            - source image height
	 * @param dstPixels
	 *            - destination image pixel array to be filled (ARGB values)
	 * @param dstWidth
	 *            - destination image width
	 * @param dstHeight
	 *            - destination image height
	 * @param degrees
	 *            - angle in degrees for the perspective
	 */
	void calculateBilinear(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth, int dstHeight,
			double degrees) {
		
		double sinus = Math.sin(Math.toRadians(degrees));
		double cosinus = Math.cos(Math.toRadians(degrees));

		// Schleife ¸ber Zielbild um korrespondierende Quellkoordinaten zu ermitteln
		for (int yDst = 0; yDst < dstHeight; yDst++) {
			for (int xDst = 0; xDst < dstWidth; xDst++) {
				
				int dstPos = yDst * dstWidth + xDst;

				// Positionierung des Koordinatenursprungs auf Bildmittelpunkt des Zielbildes
				double xCentered = xDst - (dstWidth/2);
				double yCentered = yDst -(dstHeight/2);
				
				// Perspektivische Verzerrung
				
				double ySrc = yCentered/(cosinus-0.001*sinus*yCentered);
				double xSrc = xCentered * (0.001 * sinus * ySrc + 1);
				
				// R¸ckpositionierung um Width & Height / 2 des Quellbildes
				xSrc = xSrc + (srcWidth/2);
				ySrc = ySrc + (srcHeight/2);
				
				int srcPos = ((int)ySrc * srcWidth + (int)xSrc);
				
				
				//// Bilineare Interpolation ///////
				
			    double h = xSrc % 1f; // horizintale Distanz
			    double v = ySrc % 1f; // vertikale Distanz
				
				
				int argb[] = new int[4]; // enth‰lt vier umliegende Pixel
				
				
				// Bestimmung der Positionen der umliegenden Punkte
					if(srcPos >= 0 && srcPos < srcPixels.length) // If Anweisung zur Vermeidung von Exceptions
					 argb[0] = srcPixels[srcPos]; // Punkt A
	                if(srcPos >= 0 && srcPos +1 < srcPixels.length)
	                	argb[1] = srcPixels[srcPos +1];	// Punkt B
	                if(srcPos >= 0 && srcPos + (srcWidth) < srcPixels.length)
	                	argb[2] = srcPixels[srcPos + (srcWidth)]; // Punkt C
	                if(srcPos >= 0 && (srcPos+1) + (srcWidth +1) < srcPixels.length)
	                	argb[3] = srcPixels[(srcPos+1) + srcWidth +1]; // Punkt D
				
	            
	            // Enth‰lt die vier Punkte sowie drei Farbkan‰le f¸r RGB
				int[][] neighbors = new int[4][3];
				

				// RGB Werte der Punkte bestimmen
				for (int i = 0; i < neighbors.length; i++) { // index i = Punkte A-D
					for (int j = 0; j < neighbors[i].length; j++) { // index j = RGB Werte
						neighbors[i][j] = ( argb[i] >> (2 - j) *8) & 0xff;
					}
				}
				
				
				//P = A*(1-h)*(1 -v) + B*h*(1-v) + C*(1-h)*v + D*h*v			
				int r = (int) Math.round((neighbors[0][0]*(1-h)*(1-v) + neighbors[1][0]*h*(1-v) + neighbors[2][0]*(1-h)*v + neighbors[3][0]*h*v));
				int g = (int) Math.round((neighbors[0][1]*(1-h)*(1-v) + neighbors[1][1]*h*(1-v) + neighbors[2][1]*(1-h)*v + neighbors[3][1]*h*v));
				int b = (int) Math.round((neighbors[0][2]*(1-h)*(1-v) + neighbors[1][2]*h*(1-v) + neighbors[2][2]*(1-h)*v + neighbors[3][2]*h*v));

				
				// Randbehandlung
				int rgb = 255;
				if (((ySrc < 0) || (ySrc >= srcHeight)) || (xSrc < 0) || (xSrc >= srcWidth)) {
					dstPixels[dstPos] = (0xff << 24) | (rgb << 16) | (rgb << 8) | rgb;
				} else {
					dstPixels[dstPos] = (0xff << 24) | (r << 16) | (g << 8) | b;

				}
			}
		}
	}

}
