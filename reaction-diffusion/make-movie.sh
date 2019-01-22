#!/bin/sh
# make-movie.sh

rm -f siv-??????-s.png

for name in siv-??????.png
do
  short="${name%.*}"
  echo $short
  #pngtopnm "$name" | pnmscale 20 | pnmtopng > "${short}-s.png"
  convert "$name" -scale 360x300 -define png:color-type=2 "${short}-s.png"
done

rm -f movie.mp4

#avconv -r 20 -i siv-%06d-s.png movie.mp4
ffmpeg -f image2 -r 10 -pattern_type glob -i 'siv-*-s.png' movie.mp4


# eof



