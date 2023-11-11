# FADERIC - LongEval CLEF 2023 Lab, Task 1. LongEval-Retrieval

Project of group FADERIC for the "Search Engines" course, A.Y. 2022/2023. The group is participating in the LongEval Lab at CLEF 2023, [Task 1 - LongEval-Retrieval](https://clef-longeval-2023.github.io/). The task aims at evaluating the temporal persistence of information retrieval (IR) systems and text classifiers.

"Search Engines" is a course for the master's degree in "Computer Engineering" and master's degree in "Data Science" at the University of Padua, Italy.

This is a copy of the [original repository](https://bitbucket.org/upd-dei-stud-prj/seupd2223-faderic/src/master/) on BitBucket.

### Features ###
* **Analyzer**, custom analyzers for English and French documents. Both use tokenizing, stopwords removal and stemming
* **Searcher**
   * word N-grams
   * fuzzyness
   * synonyms with fixed synonyms dictionary or [OpenNLP](https://opennlp.apache.org/) POS tagging (English only)
* **Reranker**, document reranking with [pytorch](https://pytorch.org/) and [pygaggle](https://github.com/castorini/pygaggle). Three models have been tested:
   * [BERT-based model trained on MS MARCO dataset](https://huggingface.co/Luyu/bert-base-mdoc-bm25)
   * Mono-T5
   * [custom trained model based on BERT](https://huggingface.co/enricobolzonello/clef_longeval)

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

### Results ###
Our system achived the best overall performance of all submitted systems, as can be seen in the [overview paper](https://ceur-ws.org/Vol-3497/paper-184.pdf) of the LongEval task.

Full results and description of the system can be found in the [published paper](https://ceur-ws.org/Vol-3497/paper-188.pdf).

### License ###

All the contents of this repository are shared using the [Creative Commons Attribution-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-sa/4.0/).

![CC logo](https://i.creativecommons.org/l/by-sa/4.0/88x31.png)

### Contributors ###

- Enrico Bolzonello - 2087644
- Christian Marchiori - 2078343
- Daniele Moschetta - 2087640
- Riccardo Trevisiol - 2095666
- Fabio Zanini - 2088628
