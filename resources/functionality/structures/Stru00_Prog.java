package functionality.structures;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.TextOnly;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Stru00_Prog extends Functionality {

	public static int POSITION = 0;
	public static String NAME = "Programmatical";
	public static String IDENTIFIER = "StruProgNode";
	public static String DESCRIPTION = "Structuring elements for the programm.";
	
	
	public static ProgramElement create_StComEv()
	{
		return(new FunctionalityContent("StComEv", Comment, 1));
	}
	public static ProgramElement visualize_StComEv(FunctionalityContent content)
	{
		VisualizableProgramElement StCom;
		StCom = new VisualizableProgramElement(content, "Comment Block", "An effectless element with the purpose of organizing the program into blocks and commenting.\nDoubleclick to change type a comment.\nBy the way, to achive executable blocks (like 'functions'), look at the 'Labeled Event'.");
		StCom.setArgumentDescription(0, new TextOnly(), "Comment Block");
		return(StCom);
	}
	

	
	public static ProgramElement create_StQuitEv()
	{
		return(new FunctionalityContent("StQuitEv", EventQuitter, 0));
	}
	public static ProgramElement visualize_StQuitEv(FunctionalityContent content)
	{
		return(new VisualizableProgramElement(content, "Quit Event", "Causes the program to instantly stop the current event."));
	}


	
}


