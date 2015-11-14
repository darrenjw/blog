# R language script for illustrating HOF concepts in R

curve(x*x,-3,3)

myQuad1 = function(x) x*x + 2*x + 3
curve(myQuad1,-3,3)

quadratic = function(a,b,c,x) a*x*x + b*x + c
myQuad2 = function(x) quadratic(-1,2,3,x)
curve(myQuad2,-3,3)

quadFun = function(a,b,c) function(x) quadratic(a,b,c,x)
myQuad3 = quadFun(1,2,3)
curve(myQuad3,-3,3)

# eof


