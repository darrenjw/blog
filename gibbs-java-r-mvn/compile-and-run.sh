#!/bin/sh

mvn clean compile package
Rscript run-gibbs.R


