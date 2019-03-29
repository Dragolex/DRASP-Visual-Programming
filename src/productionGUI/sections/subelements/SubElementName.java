package productionGUI.sections.subelements;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import settings.GlobalSettings;
import staticHelpers.StringHelpers;

public class SubElementName extends SubElement
{
	String name = null;
	int extraWidth = 0;
	
	@FXML private ImageView raspiSymbol;	

	
	// Create a simple sub-element only consisting of a string (used to display the name)
	public SubElementName(String name, boolean raspiActive)
	{
		this.name = name;
		this.raspiActive = raspiActive;
	}
	
	public String getName()
	{
		return(name);
	}
	
	public void setExtraWidth(int extraWidth)
	{
		this.extraWidth = extraWidth;
	}
	
	@FXML
	protected void initialize()
	{
		// Hide the textfield
		contentField.setVisible(false);
		contentField.setManaged(false);
		
		// Show text on the label
		contentLabel.setText(name);
		
		if (raspiActive)
		{
			raspiSymbol.setVisible(true);
			raspiSymbol.setManaged(true);
		}
		else
			((HBox) raspiSymbol.getParent()).getChildren().remove(raspiSymbol);
	}
	
	
	public int getContentWidth()
	{
		if (contentLabel.getWidth() < 30)
			return(contentLabel.getText().length()*GlobalSettings.textfieldLetterWidth + extraWidth);
		
		return (int) (StringHelpers.computeTextWidth(contentLabel.getFont(),
			contentLabel.getText(), 0.0D) + extraWidth);
	}
	

}
