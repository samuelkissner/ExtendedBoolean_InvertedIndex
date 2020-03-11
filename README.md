# ExtendedBoolean_InvertedIndex
A simple search engine that utilizes the extended boolean model and an inverted index to retrieve a list of relevant documents based on a keywords in a boolean query.

The program takes three arguments:

1. path to the documents folder
2. path to the input file that contains all the search queries
3. path to the output file where the results (list of documents) of each query will be written

The program will first build an inverted index based on keywords from documents in the documents folder. Then it will read each query from the input file, use the inverted index to find a list of relevant documents in the documents folder, and write that list to the output file. A sample documents folder is provided (documents), as well as a sample input file (queryin.txt) and a sample output file (queryout.txt).

(the following assumes the jdk is installed and configured correctly)

To Compile:

- In the Windows command prompt, navigate to the src folder.
- enter the following: javac Main.java

To Run:

- After compiling (and still in the src folder) enter the following: java Main "../documents" "../queryin.txt" "../queryout.txt"