# Makefile




view: DraftPost.html
	xdg-open DraftPost.html

DraftPost.html: DraftPost.md
	pandoc DraftPost.md -o DraftPost.html

clean:
	sbt clean
	rm -f siv-*.png



# eof

