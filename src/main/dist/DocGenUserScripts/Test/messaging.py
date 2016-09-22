import inspect
from com.nomagic.magicdraw.core import Application
###########################################################################
# Messaging methods
###########################################################################

#These flags enable/disable messages.
#Functions are either defined or undefined based on these flags
#Setting any of these to False will make all lower priority messages
#also be False, so this is a fast way to control verbosity
ERROR   = True
WARNING = True and ERROR
DEBUG   = True and WARNING
INFO    = True and DEBUG

LOG = Application.getInstance().getGUILog()

def get_trace_info():
    frame=inspect.stack()[2]
    # For linux AND windows
    file=frame[1].split("/")[-1].split("\\")[-1]
    line=frame[2]
    func=frame[3]
    return(file,line,func)

if ERROR:
    def ERR(msg):
        (file,line,func) = get_trace_info()
        msg = "~~~~\nError @ [%s : %d : %s] "%(file,line,func) + msg
        OUT(msg)
else:
    def ERR(msg):
        pass

if WARNING:
    def WARN(msg):
        (file,line,func) = get_trace_info()
        msg = "~~~~\nWarning @ [%s:%d:%s] "%(file,line,func) + msg
        OUT(msg)
else:
    def WARN(msg):
        pass
    
if DEBUG:
    def DBG(msg):
        (file,line,func) = get_trace_info()
        msg = "[%s:%d:%s] "%(file,line,func) + msg
        OUT(msg)
else:
    def DBG(msg):
        pass

if INFO:
    def INFO(msg):
        (file,line,func) = get_trace_info()
        msg = "[%s:%d:%s] "%(file,line,func) + msg
        OUT(msg)
else:
    def INFO(msg):
        pass

def OUT(msg):
    LOG.log(msg)

def clear():
    LOG.clearLog()
