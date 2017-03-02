/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
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
        String string = "digraph G {";
        for (VertexType v : G.getVertices()) {
            string = string.concat("\n\t" + v.toString());
        }
        for (DirectedEdge<VertexType> e : G.getEdges()) {
            string = string.concat("\n\t" + e.toString() + ";");
        }
        string = string.concat("\n}");
        return string;
    }

    public static <VertexType, EdgeType extends UndirectedEdge<VertexType>> String undirectedGraph2DotString(
            UndirectedGraph<VertexType, EdgeType> G) {
        String string = "graph G {";
        for (VertexType v : G.getVertices()) {
            string = string.concat("\n\t" + v.toString());
        }
        for (UndirectedEdge<VertexType> e : G.getEdges()) {
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
