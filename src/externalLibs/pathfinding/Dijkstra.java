package externalLibs.pathfinding;

import java.util.Scanner; //Scanner Function to take in the Input Values 

public class Dijkstra 
{ 
    static Scanner scan; // scan is a Scanner Object 
  
    public Dijkstra(byte[][] matrix)
    {
    	int w = matrix[0].length;
    	
    	
    	int[] preD = new int[w]; 
        int min = 999, nextNode = 0; // min holds the minimum value, nextNode holds the value for the next node.
        int[] distance = new int[w]; // the distance matrix 
        int[] visited = new int[w]; // the visited array 
  
        System.out.println("Enter the cost matrix"); 
  
        for (int i = 0; i < distance.length; i++) 
        { 
            visited[i] = 0; //initialize visited array to zeros 
            preD[i] = 0; 
        }
        
        int k = 0;
        for (byte b: matrix[0])
        	distance[k++] = b;
        visited[0] = 1; //set the source node as visited 
        distance[0] = 0; //set the distance from source to source to zero which is the starting point 
  
        for (int counter = 0; counter < w; counter++) 
        { 
            min = 128; 
            for (int i = 0; i < w; i++) 
            { 
                if (min > distance[i] && visited[i]!=1) 
                { 
                    min = distance[i]; 
                    nextNode = i; 
                } 
            } 
  
            visited[nextNode] = 1; 
            for (int i = 0; i < w; i++) 
            { 
                if (visited[i]!=1) 
                { 
                    if (min+matrix[nextNode][i] < distance[i]) 
                    { 
                        distance[i] = min+matrix[nextNode][i]; 
                        preD[i] = nextNode; 
                    } 
  
                } 
  
            } 
  
        } 
  
        for(int i = 0; i < w; i++) 
            System.out.print("|" + distance[i]); 
  
        System.out.println("|"); 
  
        int j; 
        for (int i = 0; i < w; i++) 
        { 
            if (i!=0) 
            { 
  
                System.out.print("Path = " + i); 
                j = i; 
                do
                { 
                    j = preD[j]; 
                    System.out.print(" <- " + j); 
                } 
                while(j != 0); 
            } 
            System.out.println(); 
        } 
    } 
} 