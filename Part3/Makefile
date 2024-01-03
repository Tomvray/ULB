
default: build

jflex:
	jflex src/LexicalAnalyzer.flex

build: jflex
	javac -d more -cp src/ src/Main.java
	jar cfe dist/part2.jar Main -C more .
	javadoc -private src/*.java -d doc/javadoc

testing:
#	java -jar dist/part2.jar -wt /tmp/EuclidParseTree.tex test/00-euclid.pmp
#	pdflatex -output-directory /tmp /tmp/EuclidParseTree.tex
	for testFile in test/*.pmp ; do \
		echo "\nTest file:" $$testFile ; \
		java -jar dist/part2.jar -wt /tmp/$$(basename $$testFile .pmp).tex $$testFile ; \
		echo "\tCompiling tree figure $$(basename $$testFile .pmp).tex... \c"; \
		pdflatex -interaction=nonstopmode -output-directory /tmp /tmp/$$(basename $$testFile .pmp).tex $$testFile > /dev/null ; \
		mv /tmp/$$(basename $$testFile .pmp).pdf test-out/ ; \
		echo "Done"; \
		echo "" ; \
	done

all: build testing
