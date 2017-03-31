# MCMC as a Stream

## Rough draft of post in advance of publishing on WordPress


https://darrenjw.wordpress.com/2010/08/15/metropolis-hastings-mcmc-algorithms/


```r
metrop3<-function(n=1000,eps=0.5) 
{
        vec=vector("numeric", n)
        x=0
        oldll=dnorm(x,log=TRUE)
        vec[1]=x
        for (i in 2:n) {
                can=x+runif(1,-eps,eps)
                loglik=dnorm(can,log=TRUE)
                loga=loglik-oldll
                if (log(runif(1)) < loga) { 
                        x=can
                        oldll=loglik
                        }
                vec[i]=x
        }
        vec
}

plot.mcmc<-function(mcmc.out)
{
    op=par(mfrow=c(2,2))
    plot(ts(mcmc.out),col=2)
    hist(mcmc.out,30,col=3)
    qqnorm(mcmc.out,col=4)
    abline(0,1,col=2)
    acf(mcmc.out,col=2,lag.max=100)
    par(op)
}
 
metrop.out<-metrop3(10000,1)
plot.mcmc(metrop.out)

```




### eof


