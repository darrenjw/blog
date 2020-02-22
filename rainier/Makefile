# Makefile


FORCE:
	make doc

doc: docs/DraftPost.md docs/Tutorial.md
	sbt mdoc
	cd target/mdoc ; pandoc DraftPost.md -o DraftPost.html
	cd target/mdoc ; pandoc Tutorial.md -o Tutorial.html

edit:
	emacs *.md Makefile build.sbt src/test/scala/*.scala docs/*.md src/main/scala/*.scala &

commit:
	git commit -a && git push

update:
	git pull
	git log|less


# eof


