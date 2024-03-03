# Makefile


FORCE:
	quarto render

preview:
	quarto preview

publish:
	cp -r _site/* ../docs/
	git add ../docs/*
	git commit -a && git push

edit:
	emacs Makefile *.yml *.qmd &


# eof

