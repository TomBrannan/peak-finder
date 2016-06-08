# Peak-Finder
A graphical tool for finding peaks in a continuous set of data

![Peak Finder](http://i.imgur.com/EkmTvfc.png)

# About
This program was created to help out with some research in the Exercise Science department at Bloomsburg University.  
The particular application involved a study of burn victims and flexibility, but this application could be used for other peak-finding purposes :)

# Usage
The Peak Finder application will parse a text file containing a list of points and, given certain constraints, find the local peaks in the data (along with other metadata) and export them to an Excel file.
Multiple files can be graphed/exported at once.  Since the peak finding algorithm isn't perfect or may not be exactly suited to a given dataset, the graphical component is given as an easy
way to check if the program found the correct peaks.  The format for the text file and the panel controls are discussed in the instructions.txt file.

Libraries used for this project include [GRAL](https://github.com/eseifert/gral) and [JXL](http://jexcelapi.sourceforge.net/).
