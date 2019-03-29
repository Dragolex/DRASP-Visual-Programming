package productionGUI.sections.subelements;

import dataTypes.contentValueRepresentations.AbstractContentValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import productionGUI.sections.elements.ProgramElementOnGUI;
import staticHelpers.TemplateHandler;

public abstract class SubElement
{
	
	public static SubElement realizeOnGUI(SplitPane contentPane, String name, boolean raspiActive)
	{
		SubElement controler = new SubElementName(name, raspiActive);
		TemplateHandler.injectTemplate("/productionGUI/sections/subelements/SubElementTemplate.fxml", controler, contentPane);
		return(controler);
	}
	
	public static SubElementField realizeOnGUI(SplitPane contentPane, AbstractContentValue currentValue, boolean editable, ProgramElementOnGUI elOnGui)
	{
		SubElementField controler = new SubElementField(currentValue, editable, elOnGui);
		TemplateHandler.injectTemplate("/productionGUI/sections/subelements/SubElementTemplate.fxml", controler, contentPane);
		return(controler);
	}
	
	
	@FXML HBox container;
	@FXML Label contentLabel;
	@FXML TextField contentField;
	
	boolean raspiActive = false;
	
	abstract public int getContentWidth();
	
	public void setWidth(double width)
	{
		if (raspiActive)
			width -= 22;
		
		contentLabel.setMinWidth(width);
		contentLabel.setMaxWidth(width);
	}
	
	public void setLimitedWidth(double minimum, double limit)
	{
		if (raspiActive)
			limit -= 22;
		
		contentLabel.setMinWidth(minimum);
		contentLabel.setMaxWidth(limit);
	}
	
	

	public SubElement alignToRight(boolean align)
	{
		if (align)
		{
			contentLabel.setAlignment(Pos.CENTER_RIGHT);	
			contentField.setAlignment(Pos.CENTER_RIGHT);	
		}
		else
		{
			contentLabel.setAlignment(Pos.CENTER_LEFT);	
			contentField.setAlignment(Pos.CENTER_LEFT);	
		}
		
		return(this);
	}

	public SubElement alignToCenter()
	{
		contentLabel.setAlignment(Pos.CENTER);	
		contentField.setAlignment(Pos.CENTER);
		
		return(this);
	}

	
	public boolean isMarked() // Overridden by SubElementField
	{
		return(false);
	}

	public HBox getContainer()
	{
		return(container);
	}

	public void setSpecialStyle(String style)
	{
		container.setStyle(style);
	}
	
}
