library(rJava)
.jinit("./target/gibbs-mvn-1.0-SNAPSHOT.jar")
obj=.jnew("GibbsR")

jgibbs<-function(N=10000,thin=500,seed=trunc(runif(1)*1e6))
{
    result=.jcall(obj,"[[D","gibbs",as.integer(N),as.integer(thin),as.integer(seed))
    mat=sapply(result,.jevalArray)
    mat=cbind(1:N,mat)
    colnames(mat)=c("Iter","x","y")
    mat
}

jgibbs()


