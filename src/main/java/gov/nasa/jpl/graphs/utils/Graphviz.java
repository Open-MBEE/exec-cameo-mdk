package gov.nasa.jpl.graphs.utils;

import gov.nasa.jpl.graphs.DirectedEdge;
import gov.nasa.jpl.graphs.DirectedGraph;
import gov.nasa.jpl.graphs.UndirectedEdge;
import gov.nasa.jpl.graphs.UndirectedGraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Graphviz {
    public static <VertexType, EdgeType extends DirectedEdge<VertexType>> String directedGraph2DotString(
            DirectedGraph<VertexType, EdgeType> G) {
        String string = new String("digraph G {");
        for (VertexType v: G.getVertices()) {
            string = string.concat("\n\t" + v.toString());
        }
        for (DirectedEdge<VertexType> e: G.getEdges()) {
            string = string.concat("\n\t" + e.toString() + ";");
        }
        string = string.concat("\n}");
        return string;
    }

    public static <VertexType, EdgeType extends UndirectedEdge<VertexType>> String undirectedGraph2DotString(
            UndirectedGraph<VertexType, EdgeType> G) {
        String string = new String("graph G {");
        for (VertexType v: G.getVertices()) {
            string = string.concat("\n\t" + v.toString());
        }
        for (UndirectedEdge<VertexType> e: G.getEdges()) {
            string = string.concat("\n\t" + e.toString() + ";");
        }
        string = string.concat("\n}");
        return string;
    }

    public static <VertexType, EdgeType extends DirectedEdge<VertexType>> boolean directedGraph2dotPdf(
            DirectedGraph<VertexType, EdgeType> G, String fileName, String dotPath) {
        return dotString2pdf(directedGraph2DotString(G), fileName, dotPath);
    }

    public static <VertexType, EdgeType extends UndirectedEdge<VertexType>> boolean unddirectedGraph2dotPdf(
            UndirectedGraph<VertexType, EdgeType> G, String fileName, String dotPath) {
        return dotString2pdf(undirectedGraph2DotString(G), fileName, dotPath);
    }

    public static boolean dotString2pdf(String dotString, String fileName, String dotPath) {
        Runtime r = Runtime.getRuntime();
        if (print2File(fileName + ".dot", dotString)) {
            try {
                r.exec(dotPath + "dot -v -Tpdf " + fileName + ".dot -o " + fileName + ".pdf");
                r.exec("rm " + fileName + ".dot");
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean print2File(String fileName, String content) {
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(content);
            // Close the output stream
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}
