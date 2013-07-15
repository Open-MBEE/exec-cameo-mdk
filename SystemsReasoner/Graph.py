from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mddependencies import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import *
from com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions import *
from com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities import *
from com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities import *
from com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows import *
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import *
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import *

from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

guiLog            = Application.getInstance().getGUILog()


from collections import deque

# For references relating to this code see:
#   [1] T. H. Cormen, C. E. Leiserson, R. L. Rivest, and C. Stein. 
#       Introduction to Algorithms. The MIT Press,
#       Cambridge, Massachusetts, 2nd edition, 2001.

# WHITE = not discovered, GRAY = discovered, and BLACK = finished
(WHITE, GRAY, BLACK) = (None, 0, 1)

class Graph:
    def __init__(self, getVertices = lambda v: None, getEdges = lambda v: None, getChildren = lambda v: None):
        self.getVertices = getVertices
        self.getEdges = getEdges
        self.getChildren = getChildren

# From [1] pg. 541
#
# DFS(G) 
# 1 for each vertex u \in V[G] 
# 2   do color[u] <- WHITE 
# 3      \pi[u] <- NIL 
# 4 time <- 0 
# 5 for each vertex u \in V[G] 
# 6   do if color[u] = WHITE 
# 7       then DFS-VISIT(u) 
# DFS(G) 
#    Modifications:
#      * "vertices" key was added to enable user defined search ordering at the
#        top level.
#      * Added a hash table that keeps track of the children of the search
#        tree.
def dfs(G = Graph(), vertices = None):
    if vertices == None:
        vertices = G.getVertices()
    color = dict() # color of the vertices
    pi    = dict() # parent of the vertices
    d     = dict() # discovery time of the vertices
    f     = dict() # finish time of the vertices
    # 1 for each vertex u \in V[G]
    for u in vertices:
        # 2 do color[u] <- WHITE
        color[u] = WHITE
        # 3 \pi[u] <- NIL
        pi[u] = None
    # 4 time <- 0
    time = [0]
    # 5 for each vertex u \in V[G]
    for u in vertices:
        # 6 do if color[u] = WHITE
        if color.get(u) == WHITE:
            dfs_visit(u = u, color = color, pi = pi, time = time, d = d, f = f, G = G)
    return(pi, d, f)

# DFS-VISIT(u) 
# 1 color[u] <- GRAY #WHITE vertex u has just been discovered. 
# 2 time <- time + 1 
# 3 d[u] <- time 
# 4 for each v \in Adj[u] #Explore edge(u,v). 
# 5   do if color[v] = WHITE 
# 6       then \pi[v] <- u 
# 7            DFS-VISIT(v) 
# 8 color[u] <- BLACK #BLACKen u; i
# DFS-VISIT(u)
def dfs_visit(u = None, color = dict(), pi = dict(), time = [0], d = dict(), f = dict(), G = None):
    # 1 color[u] <- GRAY -> WHITE vertex u has just been discovered. 
    color[u] = GRAY
    # 2 time <- time + 1
    time[0] = time[0] + 1
    # 3 d[u] <- time
    d[u] = time
    #guiLog.log("Discovered: " + u.getName() + " , " + str(d[u]))
    #guiLog.log("Children(" + u.getName() + "): " + str(map(lambda x: x.getName(), G.getChildren(u))))
    # 4 for each v \in Adj[u] -> Explore edge(u,v).
    for v in G.getChildren(u):
        # 5 do if color[v] = WHITE
        if color.get(v) == WHITE:
            # 6 then \pi[v] <- u
            pi[v] = u
            # 7 DFS-VISIT(v)
            dfs_visit(u = v, color = color, pi = pi, time = time, d = d, f = f, G = G)
    # 8 color[u] <- BLACK -> BLACKen u; it is finished.
    color[u] = BLACK
    # 9 f[u] <- time <- time + 1
    f[u] = time[0] = time[0] + 1
    #guiLog.log("Finished: " + u.getName() + ":" + str(f[u]))

    
# TOPOLOGICAL-SORT(G)
# 1. call DFS(G) to compute finishing times f [v] for each vertex v
# 2. as each vertex is finished, insert it onto the front of a linked list
# 3. return the linked list of vertices
def topological_sort(G = Graph(), vertices = None):
    # 1. call DFS(G) to compute finishing times f [v] for each vertex v
    pi, d, f = dfs(G, vertices)
    # 2. as each vertex is finished, insert it onto the front of a linked list
    # 3. return the linked list of vertices
    return map(lambda x: x[0], sorted(f.items(), key = lambda x: x[1]))

def topological_sort_using_finished_time(f):
    return map(lambda x: x[0], sorted(f.items(), key = lambda x: x[1]))


