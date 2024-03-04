# Makefile

CURRENT=py-test

FORCE:
	make render

preview:
	quarto preview

render:
	quarto render

publish:
	make render
	cp -r _site/* ../docs/
	git add ../docs/*
	git commit -a && git push

edit:
	emacs Makefile *.yml *.qmd posts/$(CURRENT)/*.qmd &


# eof

