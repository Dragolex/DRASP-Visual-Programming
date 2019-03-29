package staticHelpers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import productionGUI.ProductionGUI;

/**
 * This class allows to inject templates, that means using an FXML file and insert its contents as the child of an element in the scene.
 *
 * @author Alexander Georgescu
 */
public class TemplateHandler {


	public static Parent injectTemplateDirect(String templatePath, Object controler, Pane pane)
	{
		return(injectTemplate(templatePath, controler, pane, -1));
	}
	
	public static void injectTemplate(String templatePath, Object controler, Pane pane)
	{
		if (Platform.isFxApplicationThread())
			injectTemplate(templatePath, controler, pane, -1);
		else
			Platform.runLater(() -> injectTemplate(templatePath, controler, pane, -1)); 
	}

	private static Parent injectTemplate(String templatePath, Object controler, Pane pane, int position)
	{
		Parent newElement = getFxmlRoot(templatePath, controler);

		if (newElement == null)
			return(null);
		
		if (pane != null)
			if (position < 0)
				pane.getChildren().add(newElement);
			else
				pane.getChildren().add(position, newElement);
		
		return(newElement);
	}


	
	/*
	public static Parent injectTemplate(String templatePath, Object controler, Pane pane, String lookupString)
	{
		Parent newElement = getFxmlRoot(templatePath, controler);

		if (pane != null)
			pane.getChildren().add(newElement);

		return (Parent) (newElement.lookup(lookupString));
	}*/


	public static Parent injectTemplate(String templatePath, Object controler, SplitPane pane)
	{
		try
		{			
			Parent newElement = getFxmlRoot(templatePath, controler);
	
			if (newElement == null)
				return(null);
			
			if (pane != null)
				pane.getItems().add(newElement);
			
			
			return(newElement);
			
		} catch (Exception e)
		{
			System.out.println("Exception at injecting template.\nError message: " +  e.getClass() + " -> " + e.getMessage());
			e.printStackTrace();
			return(null);
		}

	}
	

	/**
	 * Bind the size of an element to its section
	 *
	 * @param mach
	 * @param scrollPane
	 * @param sectionBox
	 * @param additionalOffset
	 */
	/*
	public static void bindSectionSize(AnchorPane mach, ScrollPane scrollPane, HBox sectionBox, int additionalOffset)
	{
		int padding = (int) (sectionBox.getPadding().getLeft() + sectionBox.getPadding().getRight());

		ScrollBar scrollBar = (ScrollBar) scrollPane.lookup("*.scroll-bar:vertical");
		if (scrollBar != null)
		if (scrollBar.visibleProperty().getValue())
			padding += 14;//scrollBar.getWidth();

		padding += additionalOffset;

		((Pane)mach.lookup(".elementContainer")).prefWidthProperty().bind(scrollPane.widthProperty().add(-padding));
	}

	public static void bindSectionSize(AnchorPane mach, Pane scrollPane, HBox sectionBox, int additionalOffset)
	{
		int padding = (int) (sectionBox.getPadding().getLeft() + sectionBox.getPadding().getRight());

		ScrollBar scrollBar = (ScrollBar) scrollPane.lookup("*.scroll-bar:vertical");
		if (scrollBar != null)
		if (scrollBar.visibleProperty().getValue())
			padding += 14;//scrollBar.getWidth();

		padding += additionalOffset;

		((Pane)mach.lookup(".elementContainer")).prefWidthProperty().bind(scrollPane.widthProperty().add(-padding));
	}
	*/

	//private static Map<String, FXMLLoader> loaders = new HashMap<>();
	

	private static Parent getFxmlRoot(String templatePath, Object controler)
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(ProductionGUI.class.getResource(templatePath));

			if (controler != null)
				fxmlLoader.setController(controler);
			
			Parent res = fxmlLoader.load();
			
			return(res);
			
		} catch (IOException e)
		{
			System.out.println("Exception at preparing FXML root.\nError message: " +  e.getClass() + " -> " + e.getMessage());
			e.printStackTrace();
		}

		return(null);
	}

}
