package execution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import dataTypes.DataNode;
import dataTypes.ProgramElement;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.specialContentValues.FuncLabel;
import execution.handlers.ExecutionErrorHandler;
import execution.handlers.InfoErrorHandler;
import execution.handlers.LabelHandler;
import main.functionality.Events;
import main.functionality.Functionality;
import staticHelpers.OtherHelpers;

@SuppressWarnings("deprecation")
public class Program extends Thread
{
	// Data (getter and setter)
	
	Map<String, DataNode<ProgramElement>> pages = new HashMap<>();
	
	public void addPage(String pageName, DataNode<ProgramElement> rootNode)
	{
		if (pageName == null || pageName.isEmpty())
			callBug("Trying to add a null or empty name as a page to a program!");
		else
			pages.put(pageName, rootNode);
	}
	
	public Set<String> getPages()
	{
		return(pages.keySet());
	}
	public Collection<DataNode<ProgramElement>> getPageRoots()
	{
		return(pages.values());
	}
	
	public DataNode<ProgramElement> getPageRoot(String page)
	{
		if (pages.containsKey(page))
			return(pages.get(page));
		else
			callBug("Trying to get data for a nonexisting page!\nAttempted page: '" + page + "'.");
		
		return(null);
	}

	public boolean containsPage(String page)
	{
		return(pages.containsKey(page));
	}

	public void clear()
	{
		pages.clear();
	}

	public void replaceData(String page, DataNode<ProgramElement> newNode)
	{
		if (pages.containsKey(page))
			pages.put(page, newNode);
		else
			callBug("Trying to replace data for a nonexisting page!\nAttempted page: '" + page + "'.");
	}

	public String getPageNamebyRoot(DataNode<ProgramElement> nodeRoot)
	{
		for(Entry<String, DataNode<ProgramElement>> ent: pages.entrySet())
			if (ent.getValue() == nodeRoot)
				return(ent.getKey());
		return(null);
	}

	public void removePage(String pageName)
	{
		pages.remove(pageName);
	}

	public void replacePageName(String oldPageName, String newPageName)
	{
		DataNode<ProgramElement> dat = pages.get(oldPageName);
		pages.remove(oldPageName);
		pages.put(newPageName, dat);
	}

	
	private void callBug(String msg)
	{
		String keys = "";
		for(String key: pages.keySet())
			keys += key+", ";
		
		keys = keys.substring(0, keys.length()-2);
		
		InfoErrorHandler.callBugError(msg+"\nExisting pages: " + keys);
	}

	
	
	
	// For execution
	
	private CountDownLatch precompilationLatch, initializationFirstLatch, preInitEventsLatch;
	
	public void prepareAndStart(List<DataNode<ProgramElement>> activePageRoots, Integer eventsCount)
	{		
		Execution.start();
		
		
		lastCurrentInitializationIndex = Double.MAX_VALUE;
		initLatches.clear();
		
		
		precompilationLatch = new CountDownLatch(eventsCount); // A CountDownLatch set to the total number of events used to be able to wait until precompilation has finished
		initializationFirstLatch = new CountDownLatch(eventsCount); // A CountDownLatch set to the total number of events used to cause all events to wait until the initialization events have fired
		
		
		int initEventsCount = 0;
		for(DataNode<ProgramElement> root: activePageRoots)
			initEventsCount += startEvents(root, null);
		
		preInitEventsLatch = new CountDownLatch(initEventsCount);
		
		
		OtherHelpers.checkedAwait(precompilationLatch); // Wait for precompilation to finish
		
		
		Entry<FuncLabel, FunctionalityContent> possibleErrorCause = LabelHandler.verifyLabelMatches();
		if (possibleErrorCause != null)
		{
			ExecutionErrorHandler.showError("The label named '" +possibleErrorCause.getKey().getName() + "' does not exist.\nYou need a 'Labeled Event' of the same name.", null, possibleErrorCause.getValue(), true, true);
			return;
		}
		
		
		OtherHelpers.checkedAwait(preInitEventsLatch);
		
		currentlyActiveInitEvents = 0;
		execNextInit();
		

	}
	

	private int startEvents(DataNode<ProgramElement> root, EventInstance possibleParentEvent)
	{
		int initEventsCount = 0;
		
		EventInstance ev = null;
		
		if (root.getData() != null)
		if (root.getData().isEvent())
		if (!root.getData().getContent().isOutcommented())
		{
			ev = new EventInstance(root, this, possibleParentEvent);
			
			if (((FunctionalityContent) root.getData().getContent()).isSpecial(Functionality.InitEvent)) // if it is an init event
				initEventsCount++;
			
			ev.activateEventThread();
		}
		
		if ((root.getData() == null) || (!root.getData().getContent().isOutcommented()))
			for(DataNode<ProgramElement> subNode: root.getChildrenAlways())
			{
				initEventsCount += startEvents(subNode, ev);
			}
		
		return(initEventsCount);
	}
	
	
	
	public CountDownLatch getPprecompileLatch()
	{
		return(precompilationLatch);
	}
	public CountDownLatch getInitializationLatch()
	{
		return(initializationFirstLatch);
	}
	
	


	private volatile int currentlyActiveInitEvents = 0;
	private Map<EventInstance, CountDownLatch> initLatches = new HashMap<>(); // map of init events and a latch for each
	
	
	
	double lastCurrentInitializationIndex = Double.MAX_VALUE;
	
	
	private void execNextInit()
	{
		synchronized(this)
		{
			if (!Execution.isRunning())
				return;
			
			double currentInitializationIndex = Double.MAX_VALUE;

			for (EventInstance item: initLatches.keySet())
			{
				double order = 0;
				if (item.eventContent.hasOptionalArgument(0))
				{
					item.eventContent.resolveVariableArgumentsWithoutErrorButSet(Double.MAX_VALUE);
					order = (double) item.eventContent.getOptionalArgumentValue(0); // retrieve the order (possibly changed);
				}
				if (order < currentInitializationIndex)
					currentInitializationIndex = order;				
			}
			
			
			for (Iterator<Map.Entry<EventInstance, CountDownLatch>> itemIt = initLatches.entrySet().iterator(); itemIt.hasNext(); )
			{
				EventInstance item = itemIt.next().getKey();
				
				double order = 0;
				if (item.eventContent.hasOptionalArgument(0))
					order = (double) item.eventContent.getOptionalArgumentValue(0); // retrieve the order (possibly changed);
				
				//System.out.println("Order: " + order + "    : " + currentInitializationIndex + " last: " + last);
				//if (last != 99999)
				
				if (lastCurrentInitializationIndex != Double.MAX_VALUE && (order < lastCurrentInitializationIndex))
				{
					ExecutionErrorHandler.showError("Order-index '" + order + "' is lower than\nan already executed initialization '" + lastCurrentInitializationIndex + "'.\nCheck the variable for the 'order' parameter!", null, item.getContent(), true, false);
				   	currentlyActiveInitEvents += 1;
					initLatches.get(item).countDown();
					itemIt.remove();
				}
				else
				if (order == currentInitializationIndex)
				{
				   	currentlyActiveInitEvents += 1;
				   	initLatches.get(item).countDown();
				   	itemIt.remove();
				}
			}
			
			lastCurrentInitializationIndex = currentInitializationIndex;
			
		}
	}
	
	public void waitForInitTurn(EventInstance eventInstance)
	{
		CountDownLatch latch;
		synchronized(this)
		{
			latch = new CountDownLatch(1);
			initLatches.put(eventInstance, latch);
		}
		
		preInitEventsLatch.countDown();
		OtherHelpers.checkedAwait(latch);
	}
	
	public void finishedAnInitEvent(EventInstance eventInstance)
	{
		synchronized(this)
		{
			currentlyActiveInitEvents--;
		}
		
		if (currentlyActiveInitEvents <= 0) // all events ended
			execNextInit();
	}
	
}
