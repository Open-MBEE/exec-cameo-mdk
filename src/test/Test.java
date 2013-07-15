import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Pattern;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import gov.nasa.jpl.graphs.DirectedEdge;
import gov.nasa.jpl.graphs.DirectedGraph;
import gov.nasa.jpl.graphs.algorithms.DepthFirstSearch;
import gov.nasa.jpl.graphs.algorithms.TopologicalSort;
import gov.nasa.jpl.graphs.utils.Graphviz;

public class Test {
	
	public static boolean print2File(String fileName, String content) {
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			//Close the output stream
			out.close();
			return true;
		} catch (Exception e) {//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			return false;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	/*	Figure22_2 figure22_2 = new Figure22_2();
		System.out.println(figure22_2.toString());
		DepthFirstSearch dfs = new DepthFirstSearch();
		DirectedGraph<Integer, DirectedEdge<Integer>> depthFirstGraph = dfs.dfs(figure22_2);
		System.out.println(depthFirstGraph.toString());
		
		Figure22_7 figure22_7 = new Figure22_7();
		System.out.println(figure22_7.toString());
		TopologicalSort ts = new TopologicalSort();
		SortedSet<String> tsVertices = ts.topological_sort(figure22_7);
		System.out.println(tsVertices.toString());
		
		System.out.println(Graphviz.directedGraph2DotString(figure22_2));
		System.out.println(Graphviz.directedGraph2DotString(depthFirstGraph));
		System.out.println(Graphviz.directedGraph2DotString(figure22_7));
		
		Graphviz.directedGraph2dotPdf(figure22_2, "figure22_2", "/usr/local/bin/");
		Graphviz.directedGraph2dotPdf(depthFirstGraph, "depthFirstGraph", "/usr/local/bin/");
		Graphviz.directedGraph2dotPdf(figure22_7, "figure22_7", "/usr/local/bin/");
		
		System.out.println("Done!");*/
		
		System.out.println("n<1=0".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));
		System.out.println("<para>absdf</para>".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));
		System.out.println("C&DH".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));
		System.out.println("nC & D".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));
		System.out.println("this i << let than".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));
		System.out.println(" how < this".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));
		System.out.println("C&amp;DH".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));
		System.out.println("blah &#xA0;&#xA0; asdf".replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<(?!/?[A-Za-z]+>)", "&lt;"));

		
		
    	System.out.println(Pattern.matches("^.*Camera", "PayloadCamera"));
    	

		
	}
} 
