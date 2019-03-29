package functionality.structures;

import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.TermTextVarCalc;
import dataTypes.contentValueRepresentations.TermValueVarCalc;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.specialContentValues.DataList;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import main.functionality.Functionality;
import productionGUI.sections.elements.VisualizableProgramElement;
import staticHelpers.StringHelpers;

public class Stru05_Lists extends Functionality {

	public static int POSITION = 5;
	public static String NAME = "Lists";
	public static String IDENTIFIER = "StruListNode";
	public static String DESCRIPTION = "Various conditions for purposes not justifying their own category.";
	
	

	
	
	public static ProgramElement create_StSetListNum()
	{
		Object[] input = new Object[3];
		return( new FunctionalityContent("StSetListNum",
				input,
				() -> {}
				,
				() -> {
					
					DataList.externalSet((Variable) input[0], getINTparam( input[1] ), (Term) input[2], Variable.doubleType);
					
				}));		
	}
	public static ProgramElement visualize_StSetListNum(FunctionalityContent content)
	{		
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Set List Value", "Set a value into a list element.\nIf the list does not exist yet, it will be created.\nThe 'Value' can be everything that's allowed for 'Set Number-Variable',\nincluding terms which will be applied to the current value of the row in the list.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(true, true), "List Identifier");
		StSetNumVar.setArgumentDescription(1, new ValueOrVariable(), "Row");
		StSetNumVar.setArgumentDescription(2, new TermValueVarCalc(true), "Value");
		return(StSetNumVar);		
	}
	
	
	public static ProgramElement create_StSetListText()
	{
		Object[] input = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent("StSetListText",
				input,
				() -> {}
				,
				() -> {
					
					if (!cont[0].hasOptionalArgument(0)) // no splitting
						DataList.externalSet((Variable) input[0], getINTparam( input[1] ), (Term) input[2], Variable.textType); // direct add
					else
					{
						int ind = getINTparam( input[1] );
						
						Term baseTerm = (Term) input[2];
						
						String splitter = (String) cont[0].getOptionalArgumentValue(0);
						splitter = StringHelpers.evalueSpecialSymbols(splitter);
						
						
						Object dat = baseTerm.getRightSide();
						if (dat instanceof Variable)
							dat = ((Variable) dat).getUnchecked();
						
						
						// Execute for every element
						
						for(String subStr: ((String) dat).split(splitter))
						{
							Term subTerm = new Term(((Term) input[2]).termType, subStr, Variable.textType);
							DataList.externalSet((Variable) input[0], ind, subTerm, Variable.textType); // direct add
							ind++;
						}
					}
					
					
				}));
	}
	public static ProgramElement visualize_StSetListText(FunctionalityContent content)
	{		
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Set List Text", "Set a text into a list element.\nIf the list does not exist yet, it will be created.\nThe 'Text' can be everything that's allowed for 'Set Text-Variable',\nincluding terms which will be applied to the current value of the row in the list.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(true, true), "List Identifier");
		StSetNumVar.setArgumentDescription(1, new ValueOrVariable(), "Row");
		StSetNumVar.setArgumentDescription(2, new TermTextVarCalc(true), "Text");
		StSetNumVar.addOptionalParameter(0, new TextOrVariable(), "Splitter", "If this argument is set, the text will be split\nby the given symbol or text and thus multiple entries\nadded to the list one after another to the given position.\nFor example input string: A|#|b|#|C and Splitter |#| will result in the entries A, B and C.\nThe splitter '\\n' will split by the new-lines for example from files.");
		return(StSetNumVar);
	}
	
	public static ProgramElement create_StListToText()
	{
		Object[] params = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		return(cont[0] = new FunctionalityContent("StListToText",
				params,
				() -> {}
				,
				() -> {
					StringBuilder str = new StringBuilder();
					
					String sep = (String) cont[0].getOptionalArgumentValueOR(0, "");
					sep = StringHelpers.evalueSpecialSymbols(sep);
					
					DataList list = (DataList) params[0];
					int i = getINTparam(cont[0].getOptionalArgumentValueOR(1, 0.0));
					int len = getINTparam(cont[0].getOptionalArgumentValueOR(2, -1.0));
					int endI = Math.min(list.getSize(), (len >= 0) ? i+len : list.getSize());
					
					for(; i < endI-1; i++)
					{
						str.append(list.get(i));
						str.append(sep);
					}
					if (i < endI)
						str.append(list.get(i));
					
					initVariableAndSet(params[1], Variable.textType, str.toString());
					
				}));
	}
	public static ProgramElement visualize_StListToText(FunctionalityContent content)
	{		
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "List to Text", "Concats a part or all entries of a list into a single text.\nOptionally you can use a splitter text that will be inserted inbetween.\nNote that this only works well with text and value lists.\nIf you want to save special data types like identifiers,\nuse the 'Write Variables' action!");
		StSetNumVar.addParameter(0, new VariableOnly(), "List Identifier", "Existing list to use.");
		StSetNumVar.setArgumentDescription(1, new VariableOnly(true, true), "Output Text");
		StSetNumVar.addOptionalParameter(0, new TextOrVariable(), "Splitter", "If this argument is set, it will be inserted like a separator.");
		StSetNumVar.addOptionalParameter(1, new ValueOrVariable(), "Start Index", "First index to concat.");
		StSetNumVar.addOptionalParameter(2, new ValueOrVariable(), "Length", "Number of entries to concat.\nUse a negative number to include all to the end.");
		return(StSetNumVar);
	}

	
	
	
	
	public static ProgramElement create_StRemList()
	{
		Object[] input = new Object[2];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent("StRemList",
				input,
				() -> {}
				,
				() -> {
					DataList list = ((DataList) input[0]);
					
					int ind = getINTparam( input[1] );

					if (ind < 0)
						list.clear();
					else
					{
						if (content[0].hasOptionalArgument(0))
							initVariableAndSet(content[0].getOptionalArgumentValue(0), list.getType(), list.get( ind ));
						list.remove( ind );
					}
				}));		
	}
	public static ProgramElement visualize_StRemList(FunctionalityContent content)
	{		
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Remove List Row", "Remove and optionally get the entry of a row.\nUse '-1' for the row to clear the entire list.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(), "List Identifier");
		StSetNumVar.setArgumentDescription(1, new ValueOrVariable(), "Row");
		StSetNumVar.addOptionalParameter(0, new VariableOnly(true, true), "Output Var", "The value from the removed row will be placed here.");
		return(StSetNumVar);
	}
	
	
	
	
	
	public static ProgramElement create_StGetList()
	{
		Object[] input = new Object[3];
		return( new FunctionalityContent("StGetList",
				input,
				() -> {}
				,
				() -> {
					
					setVariable(input[2], ((DataList) input[0]).get( getINTparam( input[1] )));
					
				}));		
	}
	public static ProgramElement visualize_StGetList(FunctionalityContent content)
	{		
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Get List Row", "Get the value of a row inside a given list and place it in a variable.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(), "List Identifier");
		StSetNumVar.setArgumentDescription(1, new ValueOrVariable(), "Row");
		StSetNumVar.setArgumentDescription(2, new VariableOnly(true, true), "Output Var");
		return(StSetNumVar);		
	}

	
	
	public static ProgramElement create_StGetListSize()
	{
		Object[] input = new Object[2];
		return( new FunctionalityContent("StGetListSize",
				input,
				() -> {}
				,
				() -> {
					
				 	initVariableAndSet(input[1], Variable.doubleType, (double) ((DataList) input[0]).getSize());
				 	
				}));		
	}
	public static ProgramElement visualize_StGetListSize(FunctionalityContent content)
	{		
		VisualizableProgramElement StSetNumVar;
		StSetNumVar = new VisualizableProgramElement(content, "Get List Size", "Get the number of entries in a list.");
		StSetNumVar.setArgumentDescription(0, new VariableOnly(), "List Identifier");
		StSetNumVar.setArgumentDescription(1, new VariableOnly(true, true), "Output Var");
		return(StSetNumVar);		
	}

	
	
	
}


