# FADERIC - LongEval CLEF 2023 Lab, Task 1. LongEval-Retrieval

Project of group FADERIC for the "Search Engines" course, A.Y. 2022/2023. The group is participating in the LongEval Lab at CLEF 2023, Task 1 - LongEval-Retrieval. 

"Search Engines" is a course for the master's degree in "Computer Engineering" and master's degree in "Data Science" at the University of Padua, Italy.

### Organisation of the repository ###

The repository is organised as follows:

* `code`: this folder contains the source code of the developed system.
* `runs`: this folder contains the runs produced by the developed system, in particular:
    * in the main folder you can find the runs submitted to CLEF;
    * in the `runs-fixed` folder you can find the runs that we have fixed after submission because there where some problems in the run execution;
    * in the `runs-train-not-submitted` you can find the runs performed on the training collection that have been used to evaluate the progress of our system during its development.
* `results`: this folder contains the performance scores of the runs.
* `homework-1`: this folder contains the report describing the techniques applied and insights gained.
* `homework-2`: this folder contains the final paper submitted to CLEF.
* `slides`: this folder contains the slides used for presenting the conducted project.

### Instructions ###

To reproduce the runs you can set the preferred properties in the configuration file `code/src/main/resources/config.xml`.

To use the reranking feature you have to follow these instructions:

* Install Microsoft Visual C++ 14.0 (or greater), you can get it with Microsoft C++ Build Tools at https://visualstudio.microsoft.com/visual-cpp-build-tools/ 
* Install Python v3.7.9
* Add the installation directory to the `PATH` environment variable, it has to be the first Python directory to appear in the list otherwise the other one will be chosen
* Set the `PYTHONHOME` environment variable to the installation directory
* Install the following packages in Python v3.7.9: 
    * jep v4.1.1
    * pygaggle v0.0.3.1
    * torch v1.5.0, please follow the right install options for your machine available at https://pytorch.org/get-started/previous-versions/#wheel-17

### License ###

All the contents of this repository are shared using the [Creative Commons Attribution-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-sa/4.0/).

![CC logo](https://i.creativecommons.org/l/by-sa/4.0/88x31.png)

### Contributors ###

- Enrico Bolzonello - 2087644
- Christian Marchiori - 2078343
- Daniele Moschetta - 2087640
- Riccardo Trevisiol - 2095666
- Fabio Zanini - 2088628