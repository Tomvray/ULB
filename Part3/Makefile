
default: build

jflex: jflex src/LexicalAnalyzer.flex

build: jflex
	javac -d more -cp src/ src/Main.java
	jar cfe dist/part3.jar Main -C more .
	javadoc -private src/*.java -d doc/javadoc


