'''
This file is in both the AMMOS and DocGen Plugin projects. Try to keep them consistent.
'''

import os, re

# absolute path to descriptor file

DESCRIPTOR_FILE = 'data/resourcemanager/MDR_Plugin_Docgen_91110_descriptor.xml'

# mappings of local to plugin directories
mappings = {
            # depending on if JARs are built from build.xml or build-nojenkins, include both
            'root/': '',
            'build/zipfile/': ''
            }


def getFiles(relpath, extensions=['.py', '.xml', '.mdxml', '.groovy', '.qvto', '.qvtox', '.jar']):
    '''
    Gets all the files in the specified path with the specified extensions. If relpath is
    just a file then it will just specify the 
    @param relpath:        Path to look for files in recursively
    @param extensions:     List of extensions to include
    @return:               List of files in the relpath directory
    '''
    filelist = []
    for dirpath, dirnames, filenames in os.walk(relpath):
        for filename in filenames:
            for extension in extensions:
                if filename.endswith(extension):
                    # just get the relative path of the file (dirname has the absolute path)
                    filelist.append(os.path.join(dirpath, filename).replace(relpath,''))
                    break
    if os.path.isfile(relpath):
        filelist.append(os.path.basename(relpath))
    return filelist


def generateInstallation():
    '''
    Method for generating the XML installation strings for the resource descriptor
    @return:    String of the XML installation
    '''
    result = ''
    for srcpath, dstpath in mappings.items():
        files = getFiles(srcpath)
        template = '\t<file from="%s" to="%s"/>'
        if not dstpath:
            dstpath = ''
        for file in files:
            dstfilename = os.path.join(dstpath, file)
            tag = template % (dstfilename, dstfilename)
            result += tag + '\n'
        result += '\n'
    return result


def injectInstallation():
    '''
    Method for finding all the installation files and injecting them into the template.
    '''
    installation = generateInstallation()
    srcfile = open(DESCRIPTOR_FILE.replace('.xml', '_template.xml'), 'r')
    dstfile = open(DESCRIPTOR_FILE, 'w')
    
    for line in srcfile:
        if line.find('@installation@') >= 0:
            dstfile.write(installation)
        else:
            dstfile.write(line)
    
    srcfile.close()
    dstfile.close()


def getInstallationFilesFromDescriptor():
    '''
    Method to get the installation files in the descriptor file.
    @return:        Sorted list of installation files
    '''
    f = open(DESCRIPTOR_FILE, 'r')
    files = []
    
    for line in f:
        m = re.search(r'from=\"(.+?)\"', line)
        if m:
            files.append(m.group(0))
    
    f.close()
    files.sort()
    
    return files


def diffInstallations(original, final):
    '''
    Saves the differences between the original and final installations so the changes can be identified.
    @param original:        List of the original installation files
    @param final:           List of the final installation files
    '''
    f = open(DESCRIPTOR_FILE + '.diff', 'w')
    f.write('In original, missing from final:\n')
    for file in original:
        if file not in final:
            f.write('\t%s\n' % (file))
    f.write('In final, missing from original:\n')
    for file in final:
        if file not in original:
            f.write('\t%s\n' % (file))
    f.close()


if __name__ == '__main__':
    original = getInstallationFilesFromDescriptor()
    injectInstallation()
    final = getInstallationFilesFromDescriptor()
    diffInstallations(original, final)