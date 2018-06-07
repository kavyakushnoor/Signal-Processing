

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 *  This class provides methods for manipulating individual pixels of
 *  an image. The original image can be read from a file in JPEG, GIF,
 *  or PNG format, or the user can create a blank image of a given size.
 *  This class includes methods for displaying the image in a window on
 *  the screen or saving to a file.
 *  <p>
 *  Pixel (x, y) is column x, row y, where (0, 0) is upper left.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://www.cs.princeton.edu/introcs/31datatype">Section 3.1</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i>
 *  by Robert Sedgewick and Kevin Wayne.
 */
public final class fastCorr implements ActionListener {
    private BufferedImage image;    // the rasterized image
    private JFrame frame;           // on-screen view
    private String filename;        // name of file

   /**
     * Create an empty w-by-h picture.
     */
    public fastCorr(int w, int h) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        // set to TYPE_INT_ARGB to support transparency
        filename = w + "-by-" + h;
    }

   /**
     * Create a picture by reading in a .png, .gif, or .jpg from
     * the given filename or URL name.
     */
    public fastCorr(String filename) {
        this.filename = filename;
        try {
            // try to read from file in working directory
            File file = new File(filename);
            if (file.isFile()) {
                image = ImageIO.read(file);
            }

            // now try to read from file in same directory as this .class file
            else {
                URL url = getClass().getResource(filename);
                if (url == null) { url = new URL(filename); }
                image = ImageIO.read(url);
            }
        }
        catch (IOException e) {
            // e.printStackTrace();
            throw new RuntimeException("Could not open file: " + filename);
        }

        // check that image was read in
        if (image == null) {
            throw new RuntimeException("Invalid image file: " + filename);
        }
    }

   /**
     * Create a picture by reading in a .png, .gif, or .jpg from a File.
     */
    public fastCorr(File file) {
        try { image = ImageIO.read(file); }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open file: " + file);
        }
        if (image == null) {
            throw new RuntimeException("Invalid image file: " + file);
        }
    }

   /**
     * Return a JLabel containing this Picture, for embedding in a JPanel,
     * JFrame or other GUI widget.
     */
    public JLabel getJLabel() {
        if (image == null) { return null; }         // no image available
        ImageIcon icon = new ImageIcon(image);
        return new JLabel(icon);
    }

   /**
     * Display the picture in a window on the screen.
     */
    public void show() {

        // create the GUI for viewing the image if needed
        if (frame == null) {
            frame = new JFrame();

            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("File");
            menuBar.add(menu);
            JMenuItem menuItem1 = new JMenuItem(" Save...   ");
            menuItem1.addActionListener(this);
            menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            menu.add(menuItem1);
            frame.setJMenuBar(menuBar);



            frame.setContentPane(getJLabel());
            // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setTitle(filename);
            frame.setResizable(false);
            frame.pack();
            frame.setVisible(true);
        }

        // draw
        frame.repaint();
    }

   /**
     * Return the height of the picture (in pixels).
     */
    public int height() {
        return image.getHeight(null);
    }

   /**
     * Return the width of the picture (in pixels).
     */
    public int width() {
        return image.getWidth(null);
    }

   /**
     * Return the Color of pixel (i, j).
     */
    public Color get(int i, int j) {
        return new Color(image.getRGB(i, j));
    }
    
    /**
     * Return the Color of pixel (i, j).
     */
    public Color[][] getColorArray() {
    	Color[][] c = new Color[height()][width()];
    	for(int i = 0; i < c[0].length; i++)
    		for(int j = 0; j < c.length; j++)
    			c[j][i] = new Color(image.getRGB(i, j));
        return c;
    }

   /**
     * Set the Color of pixel (i, j) to c.
     */
    public void set(int i, int j, Color c) {
        if (c == null) { throw new RuntimeException("can't set Color to null"); }
        image.setRGB(i, j, c.getRGB());
    }

   /**
     * Save the picture to a file in a standard image format.
     * The filetype must be .png or .jpg.
     */
    public void save(String name) {
        save(new File(name));
    }

   /**
     * Save the picture to a file in a standard image format.
     */
    public void save(File file) {
        this.filename = file.getName();
        if (frame != null) { frame.setTitle(filename); }
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        suffix = suffix.toLowerCase();
        if (suffix.equals("jpg") || suffix.equals("png")) {
            try { ImageIO.write(image, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }
        else {
            System.out.println("Error: filename must end in .jpg or .png");
        }
    }

   /**
     * Opens a save dialog box when the user selects "Save As" from the menu.
     */
    public void actionPerformed(ActionEvent e) {
        FileDialog chooser = new FileDialog(frame,
                             "Use a .png or .jpg extension", FileDialog.SAVE);
        chooser.setVisible(true);
        if (chooser.getFile() != null) {
            save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }

 // compute the FFT of x[], assuming its length is a power of 2
    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[n/2];
        for (int k = 0; k < n/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < n/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + n/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }


    // compute the inverse FFT of x[], assuming its length is a power of 2
    public static Complex[] ifft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        // take conjugate
        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < n; i++) {
            y[i] = y[i].conjugate();
        }

        // divide by n
        for (int i = 0; i < n; i++) {
            y[i] = y[i].scale(1.0 / n);
        }

        return y;

    }

    public static Complex[] cconvolve(Complex[] x, Complex[] y) {

        if (x.length != y.length) {
            throw new IllegalArgumentException("Dimensions don't agree");
        }

        int n = x.length;

        Complex[] a = fft(x);
        Complex[] b = fft(y);

        Complex[] c = new Complex[n];
        for (int i = 0; i < n; i++) {
            c[i] = a[i].times(b[i]);
        }

        return ifft(c);
    }


    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex Zero = new Complex(0, 0);

        Complex[] a = new Complex[2*x.length];
        for (int i = 0;        i <   x.length; i++) a[i] = x[i];
        for (int i = x.length; i < 2*x.length; i++) a[i] = Zero;

        Complex[] b = new Complex[2*y.length];
        for (int i = 0;        i <   y.length; i++) b[i] = y[i];
        for (int i = y.length; i < 2*y.length; i++) b[i] = Zero;

        return cconvolve(a, b);
    }


    public static void show(Complex[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < x.length; i++) {
        	System.out.println(x[i]);
        }
        System.out.println();
    }
    

   /**
     * Test client. Reads a picture specified by the command-line argument,
     * and shows it in a window on the screen.
     */
    public static void main(String[] args) {
        rendImage image1 = new rendImage("image1.png" );
        rendImage image2 = new rendImage("image2.png" );
        System.out.printf("%d-by-%d\n", image1.width(), image1.height());
        
        Color testImage;
        
        int imageWidth, imageHeight;
        imageWidth = image1.width();
        imageHeight = image1.height();
        
        Complex[][] Return = new Complex[512][512];
        Complex[][] Pulse = new Complex[512][512];
        
        /* Color to greyscale conversion */

        int i =0;
        int j =0;
        
        do
        {
        	j=0;
        	do
        	{
        		testImage = image1.get(i,j);
        		
        		int red = (int) (testImage.getRed());
				
				Complex c = new Complex(red, 0); 
				Return[j][i] = c;
        		
        		j++;
        	}while(j<imageHeight);
        	
        	i++;
        }while(i<imageWidth);
        
        
        
        for(i=0;i<imageWidth;i++)
        {
        	for(j=0;j<imageHeight;j++)
        	{
        		testImage = image2.get(i,j);
        		
        		int red = (int) (testImage.getRed());
			
				Complex c = new Complex(red, 0);
				Pulse[j][i] = c;
        	}
        	
        }
        
        
        
        rendImage greenSpotImage = new rendImage(imageWidth, imageHeight);
        
        
        Complex[][] fftRet = new Complex[512][512];
        Complex[][] fftPul = new Complex[512][512];
        
        Complex[] a = new Complex[512];
        
        
        for(j=0;j<512;j++)
        {
        	for(i=0;i<512;i++)
        	{
        		a[i] = Return[j][i];
        	}
        	
        	Complex[] b = fft(a);
        	
        	for(i=0;i<512;i++)
        	{
        		fftRet[j][i] = b[i];
        	}
        }
        
        for(j=0;j<512;j++)
        {
        	for(i=0;i<512;i++)
        	{
        		a[i] = Pulse[j][i];
        	}
        	
        	Complex[] b = fft(a);
        	
        	for(i=0;i<512;i++)
        	{
        		fftPul[j][i] = b[i];
        	}
        }
        
        
        Complex[][] twodfftRet = new Complex[512][512];
        Complex[][] twodfftPul = new Complex[512][512];
        
        for(i=0;i<512;i++)
        {
        	for(j=0;j<512;j++)
        	{
        		a[j] = fftRet[j][i];
        	}
        	
        	Complex[] b = fft(a);
        	
        	for(j=0;j<512;j++)
        	{
        		twodfftRet[j][i] = b[j];
        	}
        }
        
        for(i=0;i<512;i++)
        {
        	for(j=0;j<512;j++)
        	{
        		a[j] = fftPul[j][i];
        	}
        	
        	Complex[] b = fft(a);
        	
        	for(j=0;j<512;j++)
        	{
        		twodfftPul[j][i] = b[j];
        	}
        }
        
        
        for(i=0;i<512;i++)
        {
        	for(j=0;j<512;j++)
        	{
        		twodfftPul[i][j] = twodfftPul[i][j].conjugate();
        	}
        }
        
      
        
        Complex[][] starfft = new Complex[512][512];
        for(i=0;i<512;i++)
        {
        	for(j=0;j<512;j++)
        	{
        		starfft[i][j] = twodfftRet[i][j].times(twodfftPul[i][j]);
        	}
        }
        
        Complex[][] inversefft = new Complex[512][512];
        Complex[][] twodinversefft = new Complex[512][512];
        
        for(j=0;j<512;j++)
        {
        	for(i=0;i<512;i++)
        	{
        		a[i] = starfft[j][i];
        	}
        	
        	Complex[] b = fft(a);
        	
        	for(i=0;i<512;i++)
        	{
        		inversefft[j][i] = b[i];
        	}
        }
        
        
        for(i=0;i<512;i++)
        {
        	for(j=0;j<512;j++)
        	{
        		a[j] = inversefft[j][i];
        	}
        	
        	Complex[] b = fft(a);
        	
        	for(j=0;j<512;j++)
        	{
        		twodinversefft[j][i] = b[j];
        	}
        }
        
        float[][] newImage = new float[512][512];

        
        for(i=0;i<512;i++)
        {
        	for(j=0;j<512;j++)
        	{
        		newImage[i][j] = (float) twodinversefft[i][j].re();
        	}
        }
        
        
        float maximum = newImage[0][0];
        
        for(i=0;i<512;i++)
        {
        	for(j=0;j<512;j++)
        	{
        		if(maximum < newImage[i][j])
        		{
        			maximum = newImage[i][j];
        		}
        	}
        }
        
        
        int counti = 0;
        int countj = 0;
        
        double sumi=0;
        double sumj=0;
        
        
        for(j=0;j<512;j++)
        {
        	for(i=0;i<512;i++)
        	{
        		if(newImage[i][j] >= (0.9*maximum))
        		{
        			Color newColor = new Color(255,0,0);
        			greenSpotImage.set((511-j), (511-i), newColor);
        			
        			counti++;
        			countj++;
        			sumi=sumi+(511-i);
        			sumj=sumj+(511-j);
        			
        			
        			
        		}
        		else if(newImage[i][j] > 0)
        		{
        			float c = newImage[i][j]/maximum;
        			Color newColor = new Color((int)(c*255),(int)(c*255),(int)(c*255));
        			greenSpotImage.set((511-j), (511-i), newColor);
        		}
        		else
        		{
        			Color newColor = new Color(0,0,0);
        			greenSpotImage.set((511-j), (511-i), newColor);
        		}
        	}
        }
        
        System.out.println("average i for green spot is "+ (sumi/counti));
        System.out.println("average j for green spot is "+ (sumj/countj));        
        
        image1.show();
        image2.show();
        greenSpotImage.show();
        

        
    }

}
