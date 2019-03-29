package main.functionality.helperControlers.openCVrelated;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import execution.Execution;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageInput { //extends JPanel{

	static private boolean loadedLib = false;
	static private PixelFormat<ByteBuffer> pixelFormat;
	
    int w = 1920;
    int h = 1080;
    
    WritableImage image = new WritableImage(w, h);
    PixelWriter imageWriter = image.getPixelWriter();

	ByteBuffer buf;
	byte[] arr = new byte[w*h*3];
    
	
	private int cameraIndex;
	private int identifierIndex;
	
	public ImageInput(Integer index)
	{
		load();
		
		cameraIndex = index;
	}
    
	
    //TODO: Set the static functions private if possible
    public static native void start_video_stream_file(boolean toFile, String fileName);
    public void startVideoToFile(String fileName)
    {
    	start_video_stream_file(true, fileName);
    }
    public void startVideoFromFile(String fileName)
    {
    	start_video_stream_file(false, fileName);
    }
    
    public static native int EXTstartJFXbufferedVideoStream(int index, ByteBuffer buf);
    public static native boolean EXTupdateJFXbufferedVideoStream(int index);
    
    public void startStreaming()
    {
    	if (buf == null)
    		buf = ByteBuffer.allocateDirect(w*h*3);

    	identifierIndex = EXTstartJFXbufferedVideoStream(cameraIndex, buf);
    	
    	if (identifierIndex < 0)
    	{
    		Execution.setError("Opening the camera with index " + cameraIndex + " failed.", false);
    		return;
    	}
    	
    	long frameRate = 60;
    	
    	new Thread(() -> {
	    	long frameDelay = 1000/frameRate;
	    	while(Execution.isRunning())
	    	{
	    		Execution.sleepIfPaused();
	
	    		if (!EXTupdateJFXbufferedVideoStream(identifierIndex))
	    			return; // stop if the stream ended
	        	buf.rewind();
	        	buf.get(arr, 0, arr.length);
	        	
	        	imageWriter.setPixels(0, 0, w, h, pixelFormat, arr, 0, w * 3);
	    		
	    		Execution.checkedSleep(frameDelay);
	    	}
    	}).start();
    	
    }


    public static native void crop_video_stream(int index, int x, int y, int w, int h);
    public void cropVideoStream(int x, int y, int w, int h)
    {
    	crop_video_stream(identifierIndex, x, y, w, h);
    }    
    
    
    public static native void get_current_video_frame_to_file(int index, String fileName);
    public void getCurrentVideoFrameToFile(String fileName)
    {
    	get_current_video_frame_to_file(identifierIndex, fileName);
    }
    
    public static native void end_video_stream(int index);
    public void endVideoStream()
    {
    	end_video_stream(identifierIndex);
    }


    public static native void take_photo_to_buffer(int index, ByteBuffer buf);
    public void takePhotoToBuffer(ByteBuffer buf)
    {
    	if (buf == null)
    		buf = ByteBuffer.allocateDirect(w*h*3);
    	
    	take_photo_to_buffer(identifierIndex, buf);
    }
    
    
    public static native void take_photo_to_file(int index, String fileName);
    public void takePhotoToFile(String fileName)
    {
    	take_photo_to_file(identifierIndex, fileName);
    }
    
    public static native void crop_image_file(String fileName, int x, int y, int w, int h);
    public void cropImageFile(String fileName, int x, int y, int w, int h)
    {
    	crop_image_file(fileName, x, y, w, h);
    }
    
	
	public Image getImage()
	{
		return(image);
	}
    
	
	public String toString()
	{
		return("Stream to"); // TODO
	}
	
	
    public static native boolean EXTcameraIndexExists(int index);
	
	public static Integer[] getExistingCameraIndices()
	{
		load();
		
		List<Integer> indices = new ArrayList<>();
		
		for(int i = 0; i < 20; i++)
		{
			if (EXTcameraIndexExists(i))
				indices.add(i);
		}
		
		Integer[] out = new Integer[indices.size()];
		indices.toArray(out);
		return(out);
	}
	
	
	private static void load()
	{
		if (!loadedLib)
		{
			System.out.println("LOAD OPENCV LIB");
			System.load("C:\\Users\\drarr\\Dropbox\\Projekte\\Resources\\opencv\\build\\x64\\vc15\\bin\\opencv_world341.dll");

			System.out.println("LOAD IMAGE LIB");
			System.load("C:\\Users\\drarr\\Dropbox\\Projekte\\C++\\ImageTransferTest\\x64\\Release\\ImageTransferTest.dll");
			
			System.out.println("LOADED LIB");
			
			pixelFormat = PixelFormat.getByteRgbInstance();
			loadedLib = true;
		}
	}


	public void openCamera() {
		// TODO Auto-generated method stub
		
	}


	public void openIPcamera(String url, String stringParameter, String stringParameter2) {
		// TODO Auto-generated method stub
		
	}


	public void openIPcamera(String url) {
		// TODO Auto-generated method stub
		
	}


	public void saveVideoToFile(String file) {
		// TODO Auto-generated method stub
		
	}


	public void stopStream() {
		// TODO Auto-generated method stub
		
	}
	
    
    /*
    
    
    public static void main (String args[]) throws InterruptedException
    {
    	
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        
        JPanelOpenCV t = new JPanelOpenCV();
        VideoCapture camera = new VideoCapture(0);

        Mat frame = new Mat();
        camera.read(frame); 

        if(!camera.isOpened()){
            System.out.println("Error");
        }
        else {                  
            while(true){        

                if (camera.read(frame)){

                    BufferedImage image = t.MatToBufferedImage(frame);

                    t.window(image, "Original Image", 0, 0);

                    t.window(t.grayscale(image), "Processed Image", 40, 60);

                    //t.window(t.loadImage("ImageName"), "Image loaded", 0, 0);

                    break;
                }
            }   
        }
        camera.release();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    public JPanelOpenCV() {
    }

    public JPanelOpenCV(BufferedImage img) {
        image = img;
    }   

    //Show image on window
    public void window(BufferedImage img, String text, int x, int y) {
        JFrame frame0 = new JFrame();
        frame0.getContentPane().add(new JPanelOpenCV(img));
        frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame0.setTitle(text);
        frame0.setSize(img.getWidth(), img.getHeight() + 30);
        frame0.setLocation(x, y);
        frame0.setVisible(true);
    }

    //Load an image
    public BufferedImage loadImage(String file) {
        BufferedImage img;

        try {
            File input = new File(file);
            img = ImageIO.read(input);

            return img;
        } catch (Exception e) {
            System.out.println("error");
        }

        return null;
    }

    //Save an image
    public void saveImage(BufferedImage img) {        
        try {
            File outputfile = new File("Images/new.png");
            ImageIO.write(img, "png", outputfile);
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    //Grayscale filter
    public BufferedImage grayscale(BufferedImage img) {
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                Color c = new Color(img.getRGB(j, i));

                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);

                Color newColor =
                        new Color(
                        red + green + blue,
                        red + green + blue,
                        red + green + blue);

                img.setRGB(j, i, newColor.getRGB());
            }
        }

        return img;
    }

    public BufferedImage MatToBufferedImage(Mat frame)
    {
    	ByteBuffer buf = ByteBuffer.allocateDirect(w*h*3);
   	 	byte[] arr = new byte[buf.remaining()];
    	buf.get(arr, 0, arr.length);
    	
    	//byte[] arr = new byte[w * h * 3];
    	//frame.get(0, 0, arr);
    	imageWriter.setPixels(0, 0, w, h, PixelFormat.getByteRgbInstance(), arr, 0, w * 3);
    	
    	
    	
    	
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);

        return image;
    }
*/
}
