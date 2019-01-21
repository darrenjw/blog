# Stochastic reaction-diffusion modelling

There is a fairly large literature on reaction-diffusion modelling using partial differential equations (PDEs). There is also a fairly large literature on stochastic modelling of coupled reactions, which account for the discreteness of reacting species at low concentrations. There is some literature on combining the two, to form stochastic reaction-diffusion systems, but this literature is much smaller.

In this post we will look at one approach to this problem, based on an underlying stochastic process often described by the *reaction diffusion master equation* (RDME). We will start by generating exact realisations from this process using the *spatial Gillespie algorithm*, before switching to a continuous stochastic approximation often known as the *spatial chemical Langevin equation* (spatial CLE). For fine discretisations, this spatial CLE is just an explicit numerical scheme for an associated reaction-diffusion stochastic partial differential equation (SPDE), and we can easily contrast such SPDE dynamics with their deterministic PDE approximation.






#### eof

