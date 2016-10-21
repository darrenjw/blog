# Makefile


TARGETS=DraftPost.html DraftPost.pdf DraftPost.sh DraftPost.scala

FORCE:
	make $(TARGETS)

%.pdf: %.md
	pandoc --from markdown_github $< -o $@

%.html: %.md
	pandoc --from markdown_github --to html --standalone $< --output $@

%.sh: %.md
	sed -n '/^```bash/,/^```/ p' < $< | sed 's/^```.*//g' > $@

%.scala: %.md
	sed -n '/^```scala/,/^```/ p' < $< | sed 's/^```.*//g' > $@


clean:
	rm -rf *~ $(TARGETS)


edit:
	emacs Makefile *.md &




# eof


