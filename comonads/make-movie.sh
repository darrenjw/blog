#!/bin/sh
# make-movie.sh

rm -f ising????-s.jpg

for name in ising????.png
do
  short="${name%.*}"
  echo $short
  #pngtopnm "$name" | pnmscale 20 | pnmtopng > "${short}-s.png"
  convert -flatten "$name" "${short}-s.jpg"
done

rm -f movie.mp4

avconv -r 20 -i ising%04d-s.jpg movie.mp4

# eof
