# Makefile


DraftPost.html: src/main/tut/DraftPost.md
	make tut
	pandoc -t html5 target/scala-2.13/tut/DraftPost.md -o DraftPost.html

tut:
	sbt tut

edit:
	emacs Makefile build.sbt *.md src/main/scala/*.scala src/main/tut/*.md &


# eof
