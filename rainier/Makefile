# Makefile


FORCE:
	sbt test mdoc run


edit:
	emacs *.md Makefile build.sbt src/test/scala/*.scala docs/*.md src/main/scala/*.scala &

commit:
	git commit -a && git push

update:
	git pull
	git log|less


# eof


