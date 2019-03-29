package dataTypes;

import java.util.List;

public class BreakPointResponse
{
	public BreakPointResponse(FunctionalityContent element, List<Integer> changedIndices, List<String> changedValues)
	{
		this.element = element;
		this.changedIndices = changedIndices;
		this.changedValues = changedValues;
	}
	public FunctionalityContent element;
	public List<Integer> changedIndices;
	public List<String> changedValues;
	public boolean breakNext = false;
	public boolean skipToTrueEvent = false;
}