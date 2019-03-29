package staticHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataTypes.minor.Pair;
import execution.handlers.InfoErrorHandler;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import settings.GlobalSettings;

public class StringHelpers
{
	static char startChar = "[".charAt(0);
	static char endChar = "]".charAt(0);
	static String repStr = "(\\S+)";
	
	public static Pair<String, List<Integer>> resolveCommandText(String text)
	{
		String resText = "";
		List<Integer> contents = new ArrayList<>();
	
		
		int lastEnd = 0;
		
		int i;
		for (i = 0; i < text.length(); i++)
		{
			if (text.charAt(i) == startChar)
			{
				resText = resText + text.substring(lastEnd, i) + repStr;
				lastEnd = i;
			}
			
			if (text.charAt(i) == endChar)
			{
				
				contents.add(Integer.parseInt(text.substring(lastEnd+1,i)));
				
				
				lastEnd = i+1;
			}
						
		}
		
		resText = resText + text.substring(lastEnd, i);
		
		return(new Pair<String, List<Integer>>(resText, contents));
	}

	
	
	
	
	
	static final Text helper;
    static final double DEFAULT_WRAPPING_WIDTH;
    static final double DEFAULT_LINE_SPACING;
    static final String DEFAULT_TEXT;
    static final TextBoundsType DEFAULT_BOUNDS_TYPE;
    static {
        helper = new Text();
        DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
        DEFAULT_LINE_SPACING = helper.getLineSpacing();
        DEFAULT_TEXT = helper.getText();
        DEFAULT_BOUNDS_TYPE = helper.getBoundsType();
    }

    public static double computeTextWidth(Font font, String text, double help0) {
        // Toolkit.getToolkit().getFontLoader().computeStringWidth(field.getText(),
        // field.getFont());

        helper.setText(text);
        helper.setFont(font);

        helper.setWrappingWidth(0.0D);
        helper.setLineSpacing(0.0D);
        double d = Math.min(helper.prefWidth(-1.0D), help0);
        helper.setWrappingWidth((int) Math.ceil(d));
        d = Math.ceil(helper.getLayoutBounds().getWidth());

        helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
        helper.setLineSpacing(DEFAULT_LINE_SPACING);
        helper.setText(DEFAULT_TEXT);
        return d;
    }
    
    
	public static int countLines(String str)
	{
		   String[] lines = str.split("\r\n|\r|\n");
		   return  lines.length;
	}


	public static String insertAt(String str, int ind, String insert)
	{
		if (ind >= 0)
			return(str.substring(0, ind) + insert + str.substring(ind));
		else
		{
            if (str.length() < (-ind))
            {
                InfoErrorHandler.callBugError("Error in 'insertAt' in StringHelper.");
                return("ERROR!");
            }

			return(insertAt(str, str.length()+ind, insert));
		}
	}


	public static String trimNewlines(String str)
	{
    	while(str.trim().startsWith("\n"))
    		str = str.trim().substring(1);
    	while(str.endsWith("\n"))
    		str = str.trim().substring(0, str.trim().length());
    	return(str.trim());
	}


	public static String firstLine(String description)
	{
		if (!description.contains("\\n"))
			return(description);
		
		return(description.substring(0, description.indexOf("\\n")));
	}


	public static String RGBcolorToInnerString(Color col)
	{
		StringBuilder str = new StringBuilder();
		str.append((int)(col.getRed()*255));
		str.append(", ");
		str.append((int)(col.getGreen()*255));
		str.append(", ");
		str.append((int)(col.getBlue()*255));
		return(str.toString());
	}


	public static String resolveArgNewline(String str)
	{
		if (str != null)
			return(str.replace(GlobalSettings.userusedNewlineSymbol, "\n"));
		return(null);
	}


	public static String replaceNth(String str, String repl, int n, String newStr)
	{
		int pos = str.indexOf(repl);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(repl, pos + 1);
	    
	    return(str.substring(0, pos-1)
	    		+ newStr
	    		+ str.substring(pos+repl.length()));
	}


	public static String evalueSpecialSymbols(String sep)
	{
		return(sep.replace("\\n", "\n").replace("\\r", "\r"));
	}
	
	
	
	private static String filter(String scan, String regex, String replace) {
	    StringBuffer sb = new StringBuffer();

	    Pattern pt = Pattern.compile(regex);
	    Matcher m = pt.matcher(scan);

	    while (m.find()) {
	        m.appendReplacement(sb, replace);
	    }

	    m.appendTail(sb);

	    return sb.toString();
	}
	
	public static String takeFirstCollumn(String line)
	{
		line = filter(line, " [\\s]+", " ").trim();
		return(line.split(" ")[0]);
	}


	public static List<String> extractIPaddresses(String str)
	{
		List<String> ips = new ArrayList<>();
		str = str.replaceAll("[^0-9| |\\.]","");
		str = str.replace(" .", "");
		str = filter(str, " [\\s]+", " ").trim();
		
		for(String substr: str.split(" "))
		{
			substr = substr.trim();
			if (substr.split("\\.").length == 4)
			if (!ips.contains(substr))
				ips.add(substr.trim());
		}
		return(ips);
	}
	
	public static String smoothDouble(double value)
	{
		if ((value - Math.round(value)) == 0) // has no value past the comma
			return(String.valueOf((int) value));
		else
		{
			String str = String.valueOf(value);
			while(str.endsWith("0"))
				str = str.substring(0, str.length()-2);
			return(str);
		}
			
	}
	
}
