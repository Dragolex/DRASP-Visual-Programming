package staticHelpers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import dataTypes.FunctionalityContent;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import execution.handlers.InfoErrorHandler;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.SplitPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import settings.GlobalSettings;

public class OtherHelpers {
	
	
	
	public static <T> void printList(List<T> list, String name)
	{
		InfoErrorHandler.printExecutionInfoMessage("---- Printing list for " + name + ":");
		
		for(Object l: list)
			InfoErrorHandler.printDirectMessage(l.toString());

		InfoErrorHandler.printExecutionInfoMessage("---- End list for " + name);
	}

	public static void printProgramContentList(List<FunctionalityContent> list, String name)
	{
		InfoErrorHandler.printExecutionInfoMessage("---- Printing list for " + name + ":");
		
		for(FunctionalityContent l: list)
			if (l == null)
				InfoErrorHandler.printDirectMessage("null");
			else
				InfoErrorHandler.printDirectMessage(l.getFunctionalityName());

		InfoErrorHandler.printExecutionInfoMessage("---- End list for " + name);
	}

	
	
	public static Object perform(FutureTask<Object> query)
	{
		if (Platform.isFxApplicationThread())
			query.run();
		else
			Platform.runLater(query);
		
		try {
			return(query.get());
		} catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
		
		
		return(null);
	}
	
	
	public static void sleepNonException(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			//e.printStackTrace();
		}
	}

	public static Double convDouble(String string)
	{
		try
		{
			return(Double.parseDouble(string));
		}
		catch(NumberFormatException nfe) // otherwise add as is being a string (keyword)
		{
			return(null);
		}
	}
	public static Integer convNumber(String string)
	{
		try
		{
			return(Integer.parseInt(string));
		}
		catch(NumberFormatException nfe) // otherwise add as is being a string (keyword)
		{
			return(null);
		}
	}

	public static Polygon createPolygonArrow(double x, double y, double endX, double endY, double s)
	{
        Polygon triangle = new Polygon(endX-s, endY-s, endX + s, endY, endX - s, endY + s);
        
        triangle.setRotate(Math.toDegrees(Math.atan2(endY-y, endX-x)));
        
        return(triangle);
	}
	
    
    public static double lengthdirX(double dir, double len)
    {
    	return(Math.cos(dir * (Math.PI/180)) * len);
    }
    public static double lengthdirY(double dir, double len)
    {
    	return(Math.sin(dir * (Math.PI/180)) * len);
    }
    
    
    public static double round(double value, int precision)
    {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
    
	public static String listArrayHorizontally(String string, int start, Object[] argumentValues)
	{
		StringBuilder str = new StringBuilder();
		for(Object obj: argumentValues)
		{
			str.append(string.replace("#", String.valueOf(start)));
			if (obj instanceof String)
			{
				str.append('"');
				str.append(obj);
				str.append('"');
			}
			else
				str.append(obj.toString());
			str.append(" ");
			start++;
		}
		
		return(str.toString());
	}
	
	public static Hyperlink createActivateableHyperLink(String url)
	{
		Hyperlink link = new Hyperlink();
		link.setText(url);
    	if (Desktop.isDesktopSupported())
    		link.setOnAction((event) -> {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException e)
				{ e.printStackTrace(); }
			});
    	return(link);
	}

	public static void setAbsoluteDividerPosition(SplitPane splitPane, int ind, int size, boolean fromEnd)
	{
		double width = splitPane.getWidth();
		double pos = size/width;
		
		//for(int i = 0; i < ind; i++)
			//pos += splitPane.getDividerPositions()[i];
		
		splitPane.setDividerPosition(ind, fromEnd ? (1-pos) : pos);
	}
	
	public static void applyOptimizations(Node node)
	{
		if (GlobalSettings.cacheOptimizations)
		{
			node.setCache(true);
			node.setCacheHint(GlobalSettings.chacheOptimizationType);
		}
	}

	public static void replaceMapKeyMaintainingOrder(Map map, String oldKey,
			String newKey)
	{
		List<Object> keys = new ArrayList<>();
		List<Object> values = new ArrayList<>();
		
		for(Object dat: map.entrySet())
		{
			Entry data = (Entry) dat;
			
			Object key = data.getKey();
			if (key.equals(oldKey))
				keys.add(newKey);
			else
				keys.add(key);
			
			values.add(data.getValue());			
		}
		
		map.clear();
		
		int ind = 0;
		for(Object key: keys)
		{
			map.put(key, values.get(ind));
			ind++;
		}
		
	}

	
	// Countdown the latch and await for it
	public static void LatchBarrier(CountDownLatch latch)
	{
		if (latch != null)
		{
			latch.countDown();
			try {
				latch.await();
			} catch (InterruptedException e) { e.printStackTrace();  InfoErrorHandler.callBugError("A latch failed!");	}
		}
	}

	
	
	public static boolean[] largenArray(boolean[] arr, int size, boolean allowEnlarge)
	{
		boolean[] newArr = arr;
		
		if ((arr == null) || (arr.length == 0))
			newArr = new boolean[1];
		else
			if (allowEnlarge)
				newArr = java.util.Arrays.copyOf(arr, size); // Resize
		
		return newArr;
	}
	
	public static Object[] largenArray(Object[] arr, int size, boolean allowEnlarge)
	{
		Object[] newArr = arr;
		
		if ((arr == null) || (arr.length == 0))
			newArr = new Object[1];
		else
			if (allowEnlarge)
				newArr = java.util.Arrays.copyOf(arr, size); // Resize
		
		return newArr;
	}
	
	public static void checkedAwait(CountDownLatch latch)
	{
		while(latch.getCount() > 0 && Execution.isRunning())
		try {
			latch.await(GlobalSettings.maxTaskDurationTilCheck, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {}
		
	}

	
	
	public static Variable[] listToArray(List<Variable> neededVariables)
	{
		Variable[] stockArr = new Variable[neededVariables.size()];
		stockArr = neededVariables.toArray(stockArr);
		return(stockArr);
	}
	
	
	// returns the index negatively if replacing
	public static int addSorted(List<Float> arr, float val)
	{
		int len = arr.size();
		for(int i = 0; i < len; i++)
		{
			float v = arr.get(i);
			if (v > val)
			{
				arr.add(i, val);
				return(i);
			}
			else
				if (v == val)
				{
					arr.set(i, val);
					return(-i);
				}
		}
		
		arr.add(len, val);		
		return(len);
	}

	public static Color makeRGBAColorFromRawHexString(String colorStr)
	{
		if (colorStr.startsWith("#"))
			colorStr = colorStr.substring(1);
		double alpha = 1;
		if (colorStr.length() > 6)
			alpha = Integer.parseInt( colorStr.substring(6, 8), 16)/255.0;
		
		try
		{
			Color color = Color.web("#"+colorStr.substring(0,6), alpha);
			return(color);
		}
		catch (Exception e)
		{
			Execution.setError("The color string (" + colorStr + ") could not be interpreted!\nNote that you need to provide it in the hex format of either six letters (RGB) or eight letters (RGBA).\nGoogle 'hex color'. It has a generator for RGB :)", false);
			return Color.BLACK;
		}
			
	}

	public static double lineLength(double x, double y)
	{
		return(Math.sqrt(x*x + y*y));
	}
	
}
