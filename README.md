# My Search Engine built with Java (In Progress)

Please check [here](https://drive.google.com/file/d/1Zlxn_YCOOWqQxtyLCbNelaaYJoCOIfuD/view?usp=sharing) for the progress report by March 2023.

"Phase1" class is the current entry point of the program, which crawls 30 pages using BFS strategy starting from  http://www.cse.ust.hk and outputs their information.

For a step-by-step guide on running the program with Intellij IDE, please refer to the following:

***
Please open the "Project" folder with Intellij IDE. 

1. Setup external library dependency
	⋅⋅⋅1.1 Click [File]->[Project Structure]->[Libraries], or press "Ctrl+Alt+shift+s"
	
	⋅⋅⋅1.2 Click the "+" button at the left-top of the middle section of the panel, or press "Alt+Insert"
	
	⋅⋅⋅1.3 Under the options, click "Java"
	
	⋅⋅⋅1.4 You will be prompted to select jar files by parsing through a hierarchy of directories.
	
	    ⋅⋅⋅⋅⋅⋅Locate and click on "src\external\jsoup-1.15.4.jar" , click "Ok"
	    
	    ⋅⋅⋅⋅⋅⋅Locate and click on "src\external\jdbm-1.0\lib\jdbm-1.0.jar", click "Ok"
	    
      ⋅⋅⋅1.5 Click "Ok" on the panel to save the changes. The two external packages are now integrated with the project.
      

2. Run the project
	⋅⋅⋅2.1 On the right-top part of the IDE panel, click the drawdown bar next to the "Build Project" button
	
	⋅⋅⋅2.2 Select "Edit Configuration", click "Add new" on the left half of the panel
	
	⋅⋅⋅2.3 In the drawdown list, select "Application"
	
	⋅⋅⋅2.4 On the right half of the panel, under "Build and run" , select a SDK (java 18/19 are both fine), and fill in "Phase1" in the "Main class" blank.
	
	2.5 Click "Ok" to save all changes. 
	3.
	4.6 Click the "Run" button (a green triangle icon) to run the project, or press Shift+f10
	5.
	    ⋅⋅⋅All the files will be produced, and the output in spider_result.txt will also be shown in the terminal.
	    
 ***
