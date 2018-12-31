# Makefile
# On a system with make, latex, pdflatex, R and knitr, just typing "make" should build the PDF

FILE=smfsb

FORCE:
	make $(FILE).pdf

$(FILE).md: $(FILE).Rmd
	Rscript -e "library(knitr); knit('$(FILE).Rmd')"

$(FILE).pdf: $(FILE).md
	pandoc $(FILE).md -o $(FILE).pdf

view: $(FILE).pdf
	xdg-open $(FILE).pdf &

edit:
	emacs Makefile *.Rmd &

update:
	git pull
	git log | less

commit:
	git commit -a
	git push
	make update

clean:
	rm -f *~ core $(FILE).md $(FILE).tex $(FILE).pdf *.ps *.eps *.dvi *.log *.aux *.out *.flc *.idx

# eof
